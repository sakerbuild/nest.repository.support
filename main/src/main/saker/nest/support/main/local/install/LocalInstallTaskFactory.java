/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.nest.support.main.local.install;

import java.io.Externalizable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.dependencies.FileCollectionStrategy;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.task.utils.dependencies.WildcardFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.api.local.install.LocalInstallTaskOutput;
import saker.nest.support.api.local.install.LocalInstallWorkerTaskOutput;
import saker.nest.support.api.property.RepositoryPropertyUtils;
import saker.nest.support.impl.NestSupportImpl;
import saker.nest.support.main.TaskDocs.DocLocalInstallTaskOutput;
import saker.nest.support.main.TaskDocs.DocWildcardPath;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocLocalInstallTaskOutput.class))
@NestInformation("Installs the specified saker.nest bundles to the specfied local bundle storage.\n"
		+ "The task will install the specified bundles to the local bundle storage of the repository configuration.\n"
		+ "The bundle installation will have no effect on the current build execution. Any contained tasks will be visible only "
		+ "in the next execution.\n"
		+ "If multiple local storages are configured for the current build execution, the StorageName parameter can be used "
		+ "to specify which storage is the installation target.")
@NestParameterInformation(value = "Bundle",
		aliases = { "", "Bundles" },
		required = true,
		type = @NestTypeUsage(value = Collection.class, elementTypes = DocWildcardPath.class),
		info = @NestInformation("Specifies one or more bundles to be installed.\n"
				+ "The option accepts simple paths or wildcards to the bundles that should be installed."))
@NestParameterInformation(value = "StorageName",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("Specifies the storage name of the local bundle storage to where the bundles should be installed.\n"
				+ "The storage name should be one of the local storages defined in the current Nest repository storage configuration.\n"
				+ "If not specified, the task will attempt to determine the default local bundle storage name. If no local "
				+ "storages, or more than 1 is configured, the task will fail."))
public class LocalInstallTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "nest.local.install";

	private static final class LocalInstallTask implements ParameterizableTask<Object> {
		@SakerInput(value = { "", "Bundle", "Bundles" }, required = true)
		public Collection<WildcardPath> bundles = Collections.emptyList();

		@SakerInput("StorageName")
		public String storageName;

		@Override
		public Object run(TaskContext taskcontext) throws Exception {
			if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
				BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
			}
			Collection<FileCollectionStrategy> collectionstrategies = new HashSet<>();
			for (WildcardPath bundlewc : bundles) {
				collectionstrategies.add(WildcardFileCollectionStrategy.create(bundlewc));
			}
			NavigableMap<SakerPath, SakerFile> files = taskcontext.getTaskUtilities()
					.collectFilesReportAdditionDependency(null, collectionstrategies);
			if (files.isEmpty()) {
				SakerLog.warning().taskScriptPosition(taskcontext)
						.println("No bundles found for wildcards: " + bundles);
				return null;
			}
			taskcontext.getTaskUtilities().reportInputFileDependency(null,
					ObjectUtils.singleValueMap(files.navigableKeySet(), CommonTaskContentDescriptors.PRESENT));

			String actualstoragename = this.storageName;
			if (actualstoragename == null) {
				actualstoragename = taskcontext.getTaskUtilities().getReportExecutionDependency(
						RepositoryPropertyUtils.getDefaultLocalStorageNameExecutionProperty());
			}

			List<TaskIdentifier> workertaskids = new ArrayList<>();
			for (SakerPath path : files.keySet()) {
				TaskIdentifier installertaskid = NestSupportImpl
						.createLocalInstallWorkerTaskIdentifier(actualstoragename, path);
				TaskFactory<? extends LocalInstallWorkerTaskOutput> installertask = NestSupportImpl
						.createLocalInstallWorkerTaskFactory(actualstoragename, path);
				taskcontext.startTask(installertaskid, installertask, null);
				workertaskids.add(installertaskid);
			}

			LocalInstallTaskOutput result = NestSupportImpl.createLocalInstallTaskOutput(workertaskids);
			taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
			return result;
		}
	}

	/**
	 * For {@link Externalizable}.
	 */
	public LocalInstallTaskFactory() {
	}

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new LocalInstallTask();
	}
}
