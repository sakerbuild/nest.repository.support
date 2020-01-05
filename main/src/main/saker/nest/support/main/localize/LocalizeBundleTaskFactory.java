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
package saker.nest.support.main.localize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.StructuredListTaskResult;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.IOUtils;
import saker.build.util.data.DataConverterUtils;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.api.dependency.DependencyResolutionTaskOutput;
import saker.nest.support.api.localize.BundleLocalizeUtils;
import saker.nest.support.api.localize.LocalizeBundleWorkerTaskOutput;
import saker.nest.support.impl.localize.LocalizeBundleTaskOutputImpl;
import saker.nest.support.impl.util.BundleIdentifierBundleKeyExecutionProperty;
import saker.nest.support.main.TaskDocs.DocLocalizeBundleTaskOutput;
import saker.nest.support.main.dependency.ResolveBundleDependencyTaskFactory;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocLocalizeBundleTaskOutput.class))
@NestInformation("Localizes the specified bundles.\n"
		+ "Bundle localization is the process of making the bundle references available for use on the local machine. "
		+ "This may involve downloading bundles over the network.\n"
		+ "Unlike bundle downloading, this task doesn't make the bundles available for the current build execution file system. "
		+ "The bundles will only be accessible using paths on the local file system.\n"
		+ "Generally speaking, this task will retrieve the bundles to the backing bundle storage of the repository.\n"
		+ "Take care when using the result of this task, as it may cause unexpected results when mixed with execution paths.")
@NestParameterInformation(value = "Bundles",
		aliases = { "", "Bundle" },
		required = true,
		info = @NestInformation("Specifies one or more bundles that should be localized.\n"
				+ "The parameter accepts a list of bundle identifiers, or the output from the "
				+ ResolveBundleDependencyTaskFactory.TASK_NAME + "() task.\n"
				+ "If a specified bundle identifier doesn't have a version number, the most recent bundle version will be downloaded."))
public class LocalizeBundleTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "nest.bundle.localize";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Bundle", "Bundles" }, required = true)
			public Object bundles;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				//XXX abort executions where possible instead of throwing
				if (bundles instanceof StructuredTaskResult) {
					if (bundles instanceof StructuredListTaskResult) {
						StructuredListTaskResult bundlesstructuredlist = (StructuredListTaskResult) bundles;
						Iterator<? extends StructuredTaskResult> it = bundlesstructuredlist.resultIterator();
						List<TaskIdentifier> bundlepaths = new ArrayList<>();
						while (it.hasNext()) {
							Object taskres = it.next().toResult(taskcontext);
							TaskIdentifier localizetaskid = handleBundleObject(taskcontext, taskres);
							bundlepaths.add(localizetaskid);
						}
						return new LocalizeBundleTaskOutputImpl(bundlepaths);
					}
					StructuredTaskResult structuredbundles = (StructuredTaskResult) bundles;
					bundles = structuredbundles.toResult(taskcontext);
				}
				if (bundles instanceof Object[]) {
					bundles = ImmutableUtils.makeImmutableList((Object[]) bundles);
				}
				if (bundles instanceof Iterable<?>) {
					Iterable<?> bundlesiterable = (Iterable<?>) bundles;
					List<TaskIdentifier> bundlepaths = new ArrayList<>();
					for (Object o : bundlesiterable) {
						if (o == null) {
							continue;
						}
						if (o instanceof StructuredTaskResult) {
							o = ((StructuredTaskResult) o).toResult(taskcontext);
						}
						TaskIdentifier localizetaskid = handleBundleObject(taskcontext, o);
						bundlepaths.add(localizetaskid);
					}
					return new LocalizeBundleTaskOutputImpl(bundlepaths);
				}

				Exception adaptexc = null;
				try {
					Object adapted = DataConverterUtils.adaptInterface(this.getClass().getClassLoader(), bundles);
					if (adapted instanceof DependencyResolutionTaskOutput) {
						DependencyResolutionTaskOutput depoutput = (DependencyResolutionTaskOutput) adapted;
						List<TaskIdentifier> bundlepaths = new ArrayList<>();
						for (BundleKey bundlekey : depoutput.getBundles()) {
							TaskIdentifier localizetaskid = handleBundleObject(taskcontext, bundlekey);
							bundlepaths.add(localizetaskid);
						}
						return new LocalizeBundleTaskOutputImpl(bundlepaths);
					}
				} catch (Exception e) {
					adaptexc = e;
				}
				try {
					TaskIdentifier singlelocalizetaskid = handleBundleObject(taskcontext, bundles);
					return new LocalizeBundleTaskOutputImpl(Collections.singletonList(singlelocalizetaskid));
				} catch (Exception e) {
					throw IOUtils.addExc(e, adaptexc);
				}
			}

			private BundleKey getBundleKeyOfObject(TaskContext taskcontext, Object obj)
					throws IllegalArgumentException {
				if (obj == null) {
					return null;
				}
				Exception adaptexc = null;
				try {
					Object adapted = DataConverterUtils.adaptInterface(this.getClass().getClassLoader(), obj);
					if (adapted instanceof BundleKey) {
						return (BundleKey) adapted;
					}
				} catch (Exception e) {
					adaptexc = e;

				}
				String str = Objects.toString(obj, null);
				if (str == null) {
					return null;
				}
				BundleIdentifier bundleid;
				try {
					bundleid = BundleIdentifier.valueOf(str);
				} catch (IllegalArgumentException e) {
					if (adaptexc != null) {
						e.addSuppressed(adaptexc);
					}
					throw new IllegalArgumentException("Failed to parse bundle identifier: " + str, e);
				}
				return taskcontext.getTaskUtilities()
						.getReportExecutionDependency(new BundleIdentifierBundleKeyExecutionProperty(bundleid));
			}

			private TaskIdentifier handleBundleObject(TaskContext taskcontext, Object obj) throws Exception {
				return handleBundleObject(taskcontext, getBundleKeyOfObject(taskcontext, obj));
			}

			private TaskIdentifier handleBundleObject(TaskContext taskcontext, BundleKey bundlekey) {
				TaskIdentifier taskid = BundleLocalizeUtils.createBundleLocalizeWorkerTaskIdentifier(bundlekey);
				TaskFactory<? extends LocalizeBundleWorkerTaskOutput> task = BundleLocalizeUtils
						.createBundleLocalizeWorkerTask(bundlekey);
				taskcontext.startTask(taskid, task, null);
				return taskid;
			}
		};
	}

}
