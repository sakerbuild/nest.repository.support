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
package saker.nest.support.main.dependency;

import java.io.Externalizable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleInformation;
import saker.nest.bundle.DependencyConstraintConfiguration;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.api.dependency.filter.DependencyFilter;
import saker.nest.support.api.property.RepositoryPropertyUtils;
import saker.nest.support.impl.dependency.ResolveBundleDependencyFileWorkerTaskFactory;
import saker.nest.support.impl.dependency.filter.ChainDependencyFilter;
import saker.nest.support.main.TaskDocs.DocBundleIdentifier;
import saker.nest.support.main.TaskDocs.DocDependencyFilter;
import saker.nest.support.main.TaskDocs.DocDependencyResolutionTaskOutput;
import saker.nest.support.main.dependency.filter.CompileDependencyFilterTaskFactory;
import saker.nest.support.main.dependency.filter.KindDependencyFilterTaskFactory;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocDependencyResolutionTaskOutput.class))
@NestInformation("Resolves saker.nest bundle dependencies.\n"
		+ "The task will execute the dependency resolution of the Nest repository for the given parameters.\n"
		+ "It will return the resolved bundle references. The callers are likely to pass it to another task that "
		+ "converts it to an usable input for other tasks. (E.g. classpath, downloading, etc...)\n"
		+ "This task may initiate network requests in order to complete its work.")
@NestParameterInformation(value = "Bundles",
		aliases = { "", "Bundle" },
		type = @NestTypeUsage(value = Set.class, elementTypes = { DocBundleIdentifier.class }),
		info = @NestInformation("Specifies the bundle identifiers for which the dependencies should be resolved.\n"
				+ "The dependencies of the specified bundles will be resolved for the specified dependency Kinds. \n"
				+ "The bundle identifiers may or may not contain version numbers. If they do, they will be pinned "
				+ "for the dependency resolution. If they don't, then the resolution algorithm may freely choose a version "
				+ "that satisfies the dependencies. The attempted versions for satisfaction is descending by version number.\n"
				+ "If used together with DependencyFile, the specified bundles will be effectively added to the set of dependencies defined "
				+ "in the file. This parameter can also be used to pin bundles when a version numbers are specified."))
@NestParameterInformation(value = "DependencyFile",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("Path to a saker.nest bundle dependency file from which the dependencies should be resolved.\n"
				+ "The argument is a path to a file present in Nest repository bundles, which describe the dependencies of an "
				+ "associated bundle. (Entry " + BundleInformation.ENTRY_BUNDLE_DEPENDENCIES + ")\n"
				+ "The file will be parsed and the declared dependencies are resolved with the specified dependency Kinds.\n"
				+ "Any bundles specified in the Bundles argument will be effectively added to the dependencies declared in the file. "
				+ "During the resolution, the dependencies from the Bundles argument will take priority.\n"
				+ "The \"this\" token in the dependency file is resolved using the SelfBundle argument."))
@NestParameterInformation(value = "SelfBundle",
		type = @NestTypeUsage(DocBundleIdentifier.class),
		info = @NestInformation("Specifies the self bundle in relation to the dependency resolution.\n"
				+ "Any \"this\" tokens in the DependencyFile will be substituted with the self bundle argument."))
@NestParameterInformation(value = "DependencyConstraints",
		type = @NestTypeUsage(DependencyConstraintsTaskOption.class),
		info = @NestInformation("Specifies the environmental dependency constraints which should be applied to the dependencies.\n"
				+ "The constraints can be based on the JRE major version, Nest repository version, saker.build system version, or the "
				+ "native architecture. Any dependency that doesn't match the specified restrictions will omitted.\n"
				+ "Not all constraints need to be specified. If any constraint is missing, it will be filled out based on "
				+ "the current build execution constraint configuration for the Nest repository."))
@NestParameterInformation(value = "Filters",
		aliases = { "Filter" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = { DocDependencyFilter.class }),
		info = @NestInformation("Specifies one or more dependency filter to be used when resolving the dependencies.\n"
				+ "The dependency filters determine what kind of dependencies and how they should be applied when "
				+ "executing the dependency resolution. A dependency filter can be retrieved by any of the tasks that "
				+ "are used to configure them. E.g. " + KindDependencyFilterTaskFactory.TASK_NAME + "() task or "
				+ CompileDependencyFilterTaskFactory.TASK_NAME + "() task.\n"
				+ "If multiple dependency filters are specified, they are applied after each other. If no filters are specified, "
				+ "all dependencies are resolved, and they are not filtered.\n"
				+ "Other dependency filters can be applied when it is suitable for a given use-case. Dependency filters can be "
				+ "created by other Nest packages as well."))
public class ResolveBundleDependencyTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "nest.dependency.resolve";

	/**
	 * For {@link Externalizable}.
	 */
	public ResolveBundleDependencyTaskFactory() {
	}

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ResolveBundleDependencyTaskFactoryImpl();
	}

	private static final class ResolveBundleDependencyTaskFactoryImpl implements ParameterizableTask<Object> {
		@SakerInput(value = { "", "Bundle", "Bundles" })
		public Collection<BundleIdentifier> bundles;

		@SakerInput(value = "DependencyFile")
		public SakerPath dependencyFile;
		@SakerInput(value = "SelfBundle")
		public BundleIdentifier thisBundleId;

		@SakerInput(value = { "Filter", "Filters" })
		public Collection<DependencyFilterTaskOption> filtersOption;

		@SakerInput(value = "DependencyConstraints")
		public DependencyConstraintsTaskOption constraintsOption;

		@Override
		public Object run(TaskContext taskcontext) throws Exception {
			if (this.bundles == null && this.dependencyFile == null) {
				taskcontext
						.abortExecution(new IllegalArgumentException("Bundles or DependencyFile parameter missing."));
				return null;
			}
			DependencyConstraintConfiguration constraintconfig;
			if (constraintsOption == null) {
				constraintconfig = taskcontext.getTaskUtilities().getReportExecutionDependency(
						RepositoryPropertyUtils.getRepositoryDependencyConstraingConfigurationExecutionProperty());
			} else {
				constraintconfig = DependencyConstraintConfiguration.builder()
						.setBuildSystemVersion(constraintsOption.getNativeArchitecture())
						.setJreMajorVersion(constraintsOption.getJREMajorVersion())
						.setRepositoryVersion(constraintsOption.getRepositoryVersion())
						.setNativeArchitecture(constraintsOption.getNativeArchitecture()).buildWithDefaults(() -> {
							return taskcontext.getTaskUtilities().getReportExecutionDependency(RepositoryPropertyUtils
									.getRepositoryDependencyConstraingConfigurationExecutionProperty());
						});
			}

			Set<BundleIdentifier> bundleids = ObjectUtils.newLinkedHashSet(this.bundles);
			removeNulls(bundleids);
			SakerPath depfilepath = this.dependencyFile;

			List<DependencyFilter> filters = new ArrayList<>();
			if (!ObjectUtils.isNullOrEmpty(filtersOption)) {
				for (DependencyFilterTaskOption fo : filtersOption) {
					if (fo == null) {
						continue;
					}
					filters.add(fo.getFilter());
				}
			}
			DependencyFilter depfilter = ChainDependencyFilter.create(filters);
			ResolveBundleDependencyFileWorkerTaskFactory workertask = new ResolveBundleDependencyFileWorkerTaskFactory(
					depfilter, constraintconfig, bundleids, depfilepath, thisBundleId);
			TaskIdentifier workertaskid = workertask;

			taskcontext.startTask(workertaskid, workertask, null);

			SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
			taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
			return result;
		}

		private static void removeNulls(Iterable<?> items) {
			if (items == null) {
				return;
			}
			for (Iterator<?> it = items.iterator(); it.hasNext();) {
				Object o = it.next();
				if (o == null) {
					it.remove();
				}
			}
		}
	}
}
