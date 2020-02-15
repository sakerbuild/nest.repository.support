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
package saker.nest.support.main.testing;

import java.util.Set;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.NestBundleStorageConfiguration;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.impl.testing.TestInstrumentWorkerTaskFactory;
import saker.nest.support.impl.testing.TestInstrumentWorkerTaskIdentifier;
import saker.nest.support.impl.util.BundleIdentifierBundleKeyExecutionProperty;
import saker.nest.support.impl.util.BundleVersionsLookupExecutionProperty;
import saker.nest.support.impl.util.BundleVersionsLookupExecutionProperty.PropertyLookupResult;
import saker.nest.support.main.TaskDocs.DocInstrumentedArchivePath;
import saker.nest.support.main.TaskDocs.DocVersionNumber;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocInstrumentedArchivePath.class))
@NestInformation("Adds saker.nest test instrumentation to the specified archive.\n"
		+ "The task will load the saker.nest-test-instrumentation bundle for the specified version and "
		+ "create a new archive that contains the classes from it, in addition with the specified base archive.\n"
		+ "The resulting archive can be used for integration testting with the saker.nest repository.")
@NestParameterInformation(value = "Archive",
		aliases = "",
		required = true,
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("Path to the base archive to which the instrumentation should be added.\n"
				+ "The archive itself is not modified, but a new archive is created with the contents of the base "
				+ "archive and the instrumentation classes."))
@NestParameterInformation(value = "NestVersion",
		type = @NestTypeUsage(DocVersionNumber.class),
		info = @NestInformation("Specifies the version of the saker.nest-test-instrumentation bundle that should be "
				+ "added as the instrumentation to the base archive.\n"
				+ "If not specified, the latest release is used."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("Specifies a relative output path where the instrumented archive should be created.\n"
				+ "The path is the relative path under the " + TestInstrumentTaskFactory.TASK_NAME
				+ " directory in the build directory."))
public class TestInstrumentTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "nest.test.instrument";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Archive" }, required = true)
			public SakerPath archiveOption;

			@SakerInput("NestVersion")
			public String nestVersionOption;

			@SakerInput("Output")
			public SakerPath outputPathOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				String nestversion = this.nestVersionOption;
				if (nestversion != null && !BundleIdentifier.isValidVersionNumber(nestversion)) {
					taskcontext
							.abortExecution(new IllegalArgumentException("Invalid NestVersion number: " + nestversion));
					return null;
				}
				NestBundleStorageConfiguration storageconfig = ((NestBundleClassLoader) this.getClass()
						.getClassLoader()).getBundleStorageConfiguration();
				BundleKey nestbundlekey;
				if (nestversion == null) {
					PropertyLookupResult lookupres = taskcontext.getTaskUtilities().getReportExecutionDependency(
							new BundleVersionsLookupExecutionProperty(storageconfig.getBundleLookup(),
									BundleIdentifier.valueOf("saker.nest-test-instrumentation")));
					if (lookupres == null) {
						taskcontext.abortExecution(
								new IllegalArgumentException("saker.nest-test-instrumentation bundle not found."));
						return null;
					}
					Set<? extends BundleIdentifier> bundles = lookupres.getLookupResult().getBundles();
					if (ObjectUtils.isNullOrEmpty(bundles)) {
						taskcontext.abortExecution(
								new IllegalArgumentException("No saker.nest instrumentation bundle found."));
						return null;
					}
					nestversion = bundles.iterator().next().getVersionNumber();
					nestbundlekey = BundleKey.create(lookupres.getLookupResult().getStorageView().getStorageViewKey(),
							bundles.iterator().next());
				} else {
					nestbundlekey = taskcontext.getTaskUtilities()
							.getReportExecutionDependency(new BundleIdentifierBundleKeyExecutionProperty(
									BundleIdentifier.valueOf("saker.nest-test-instrumentation-v" + nestversion)));
				}
				SakerPath archive = SakerPathFiles.toAbsolutePath(taskcontext, archiveOption);
				SakerPath outputpath = this.outputPathOption;
				if (outputpath == null) {
					outputpath = SakerPath.valueOf(archive.getFileName());
				} else {
					if (!outputpath.isRelative()) {
						taskcontext.abortExecution(new IllegalArgumentException("Output path must be relative."));
						return null;
					}
				}
				TestInstrumentWorkerTaskIdentifier workertaskid = new TestInstrumentWorkerTaskIdentifier(outputpath);
				taskcontext.startTask(workertaskid,
						new TestInstrumentWorkerTaskFactory(outputpath, archive, nestbundlekey), null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
