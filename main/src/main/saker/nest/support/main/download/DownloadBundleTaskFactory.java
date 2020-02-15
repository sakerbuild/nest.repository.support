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
package saker.nest.support.main.download;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import saker.build.exception.PropertyComputationFailedException;
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
import saker.build.trace.BuildTrace;
import saker.build.util.data.DataConverterUtils;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.exc.BundleLoadingFailedException;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.api.dependency.DependencyResolutionTaskOutput;
import saker.nest.support.api.download.BundleDownloadUtils;
import saker.nest.support.api.download.DownloadBundleWorkerTaskOutput;
import saker.nest.support.api.localize.LocalizeBundleTaskOutput;
import saker.nest.support.api.localize.LocalizeBundleWorkerTaskOutput;
import saker.nest.support.impl.NestSupportImpl;
import saker.nest.support.impl.util.BundleIdentifierBundleKeyExecutionProperty;
import saker.nest.support.main.TaskDocs.DocDownloadBundleTaskOutput;
import saker.nest.support.main.dependency.ResolveBundleDependencyTaskFactory;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocDownloadBundleTaskOutput.class))
@NestInformation("Downloads the specified bundles for the build execution.\n"
		+ "The task will take the specified bundles and put them in a build directory which is accessible during build execution. "
		+ "Bundle \"downloading\" doesn't necessarily involve network communication. Downloading is merely retrieving the bundles "
		+ "from the repository, which may be remote or local to the current machine.\n"
		+ "When a bundle is downloaded, it is put to a given path inside the build directory. The bundles will be accessible to other "
		+ "tasks during the build execution.\n"
		+ "This task doesn't perform any dependency resolution. To resolve dependencies, use the "
		+ ResolveBundleDependencyTaskFactory.TASK_NAME + "() task.")
@NestParameterInformation(value = "Bundles",
		aliases = { "", "Bundle" },
		required = true,
		info = @NestInformation("Specifies one or more bundles that should be downloaded.\n"
				+ "The parameter accepts a list of bundle identifiers, or the output from the "
				+ ResolveBundleDependencyTaskFactory.TASK_NAME + "() task.\n"
				+ "If a specified bundle identifier doesn't have a version number, the most recent bundle version will be downloaded."))
public class DownloadBundleTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "nest.bundle.download";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Bundle", "Bundles" }, required = true)
			public Object bundles;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				//XXX abort executions where possible instead of throwing
				if (bundles instanceof StructuredTaskResult) {
					if (bundles instanceof StructuredListTaskResult) {
						StructuredListTaskResult bundlesstructuredlist = (StructuredListTaskResult) bundles;
						Iterator<? extends StructuredTaskResult> it = bundlesstructuredlist.resultIterator();
						List<TaskIdentifier> bundlepaths = new ArrayList<>();
						while (it.hasNext()) {
							Object taskres = it.next().toResult(taskcontext);
							TaskIdentifier dltaskid = handleBundleObject(taskcontext, taskres);
							bundlepaths.add(dltaskid);
						}
						return NestSupportImpl.createBundleDownloadTaskOutput(bundlepaths);
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
						TaskIdentifier dltaskid = handleBundleObject(taskcontext, o);
						bundlepaths.add(dltaskid);
					}
					return NestSupportImpl.createBundleDownloadTaskOutput(bundlepaths);
				}

				Exception adaptexc = null;
				try {
					Object adapted = DataConverterUtils.adaptInterface(this.getClass().getClassLoader(), bundles);
					if (adapted instanceof DependencyResolutionTaskOutput) {
						DependencyResolutionTaskOutput depoutput = (DependencyResolutionTaskOutput) adapted;
						List<TaskIdentifier> bundlepaths = new ArrayList<>();
						for (BundleKey bundlekey : depoutput.getBundles()) {
							TaskIdentifier dltaskid = handleBundleObject(taskcontext, bundlekey);
							bundlepaths.add(dltaskid);
						}
						return NestSupportImpl.createBundleDownloadTaskOutput(bundlepaths);
					}
					if (adapted instanceof LocalizeBundleTaskOutput) {
						LocalizeBundleTaskOutput locoutput = (LocalizeBundleTaskOutput) adapted;
						StructuredListTaskResult bundlesstructuredlist = locoutput.getLocalizeResults();
						Iterator<? extends StructuredTaskResult> it = bundlesstructuredlist.resultIterator();
						List<TaskIdentifier> bundlepaths = new ArrayList<>();
						while (it.hasNext()) {
							StructuredTaskResult taskres = it.next();
							LocalizeBundleWorkerTaskOutput bundlelocalizeworkerout = (LocalizeBundleWorkerTaskOutput) taskres
									.toResult(taskcontext);
							TaskIdentifier dltaskid = handleBundleObject(taskcontext,
									bundlelocalizeworkerout.getBundleKey());
							bundlepaths.add(dltaskid);
						}
						return NestSupportImpl.createBundleDownloadTaskOutput(bundlepaths);
					}
				} catch (Exception e) {
					adaptexc = e;
				}
				try {
					TaskIdentifier singledltaskid = handleBundleObject(taskcontext, bundles);
					return NestSupportImpl.createBundleDownloadTaskOutput(Collections.singletonList(singledltaskid));
				} catch (Exception e) {
					throw IOUtils.addExc(e, adaptexc);
				}
			}

			private BundleKey getBundleKeyOfObject(TaskContext taskcontext, Object obj)
					throws IllegalArgumentException, BundleLoadingFailedException {
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
					IOUtils.addExc(e, adaptexc);
					throw new IllegalArgumentException("Failed to parse bundle identifier: " + str, e);
				}
				try {
					return taskcontext.getTaskUtilities()
							.getReportExecutionDependency(new BundleIdentifierBundleKeyExecutionProperty(bundleid));
				} catch (PropertyComputationFailedException e) {
					Throwable cause = e.getCause();
					if (cause instanceof BundleLoadingFailedException) {
						IOUtils.addExc(cause, adaptexc);
						throw (BundleLoadingFailedException) cause;
					}
					IOUtils.addExc(e, adaptexc);
					throw e;
				}
			}

			private TaskIdentifier handleBundleObject(TaskContext taskcontext, Object obj) throws Exception {
				return handleBundleObject(taskcontext, getBundleKeyOfObject(taskcontext, obj));
			}

			private TaskIdentifier handleBundleObject(TaskContext taskcontext, BundleKey bundlekey) {
				TaskIdentifier taskid = BundleDownloadUtils.createBundleDownloadWorkerTaskIdentifier(bundlekey);
				TaskFactory<? extends DownloadBundleWorkerTaskOutput> task = BundleDownloadUtils
						.createBundleDownloadWorkerTask(bundlekey);
				taskcontext.startTask(taskid, task, null);
				return taskid;
			}
		};
	}
}
