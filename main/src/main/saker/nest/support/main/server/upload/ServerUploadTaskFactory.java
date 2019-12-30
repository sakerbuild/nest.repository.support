package saker.nest.support.main.server.upload;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

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
import saker.nest.bundle.storage.ServerBundleStorageView;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.api.server.upload.BundleUploadTaskOutput;
import saker.nest.support.api.server.upload.BundleUploadWorkerTaskOutput;
import saker.nest.support.impl.NestSupportImpl;
import saker.nest.support.main.TaskDocs.DocBundleUploadTaskOutput;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocBundleUploadTaskOutput.class))
@NestInformation("Uploads Nest bundles to the specified Nest server.\n"
		+ "The task takes Nest bundle paths as its input, and upload them to the specified server.\n"
		+ "The bundle is uploaded with the permissions granted by the specified APIKey and APISecret.\n"
		+ "Note that this task is NOT incremental. Every time it is invoked, the specified bundles will be uploaded "
		+ "to the server. Make sure to specify bundle upload tasks in a different build target than the usually used "
		+ "build targets. It is recommended to have a separate \"upload\" build target that contains the upload tasks, "
		+ "and is manually invoked when the bundles are publishing ready.")
@NestParameterInformation(value = "Bundle",
		aliases = { "", "Bundles" },
		required = true,
		type = @NestTypeUsage(value = Collection.class, elementTypes = WildcardPath.class),
		info = @NestInformation("Specifies one or more bundles to be uploaded.\n"
				+ "The option accepts simple paths or wildcards to the bundles that should be uploaded."))
@NestParameterInformation(value = "Overwrite",
		type = @NestTypeUsage(Boolean.class),
		info = @NestInformation("Specifies whether or not the upload request should include the Overwrite flag.\n"
				+ "If set to true, the upload process will specify that any existing bundle on the server should be overwritten. "
				+ "If set to false, existing bundles won't be overwritten. If not set, the server defaults will be used, which is false.\n"
				+ "Note that already published bundles may not be overwritten, even of the flag is specified."))
@NestParameterInformation(value = "Server",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("Specifies the server URL to which the bundles should be uploaded.\n"
				+ "By default, the server URL is the following: "
				+ ServerBundleStorageView.REPOSITORY_DEFAULT_SERVER_URL + "\n"
				+ "This default value is independent of any current build configuration."))
@NestParameterInformation(value = "APIKey",
		required = true,
		type = @NestTypeUsage(String.class),
		info = @NestInformation("Specifies the Base64 (URL-safe) encoded API key that should be used when making the request.\n"
				+ "The API key and secret pair is used to determine if the uploader has sufficient permissions to execute the request.\n"
				+ "These keys can be retrieved from the bundle configuration page at: https://nest.saker.build/user/packages"))
@NestParameterInformation(value = "APISecret",
		required = true,
		type = @NestTypeUsage(String.class),
		info = @NestInformation("Specifies the Base64 (URL-safe) encoded API secret that should be used when making the request.\n"
				+ "The API key and secret pair is used to determine if the uploader has sufficient permissions to execute the request.\n"
				+ "These keys can be retrieved from the bundle configuration page at: https://nest.saker.build/user/packages"))
public class ServerUploadTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "nest.server.upload";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Bundle", "Bundles" }, required = true)
			public Collection<WildcardPath> bundlesOption;

			@SakerInput("Overwrite")
			public Boolean overwriteOption;

			@SakerInput("Server")
			public String serverOption = ServerBundleStorageView.REPOSITORY_DEFAULT_SERVER_URL;

			@SakerInput(value = "APIKey", required = true)
			public String apiKeyOption;
			@SakerInput(value = "APISecret", required = true)
			public String apiSecretOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (ObjectUtils.isNullOrEmpty(serverOption)) {
					taskcontext.abortExecution(
							new IllegalArgumentException("Invalid Server parameter value: " + serverOption));
					return null;
				}
				byte[] apikeybytes;
				byte[] apisecretbytes;
				try {
					apikeybytes = Base64.getUrlDecoder().decode(apiKeyOption);
				} catch (Exception e) {
					taskcontext.abortExecution(new IllegalArgumentException(
							"Failed to Base64 decode the API Key. (Using URL decoder)", e));
					return null;
				}
				try {
					apisecretbytes = Base64.getUrlDecoder().decode(apiSecretOption);
				} catch (Exception e) {
					taskcontext.abortExecution(new IllegalArgumentException(
							"Failed to Base64 decode the API Secret. (Using URL decoder)", e));
					return null;
				}

				Set<WildcardPath> bundles = ObjectUtils.cloneTreeSet(bundlesOption);
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

				List<TaskIdentifier> workertaskids = new ArrayList<>();
				for (SakerPath bundlepath : files.keySet()) {
					TaskIdentifier workertaskid = NestSupportImpl.createBundleUploadWorkerTaskIdentifier(bundlepath,
							serverOption);
					TaskFactory<? extends BundleUploadWorkerTaskOutput> workertask = NestSupportImpl
							.createBundleUploadWorkerTaskFactory(bundlepath, overwriteOption, serverOption, apikeybytes,
									apisecretbytes);
					taskcontext.startTask(workertaskid, workertask, null);
					workertaskids.add(workertaskid);
				}

				BundleUploadTaskOutput result = NestSupportImpl.createBundleUploadTaskOutput(workertaskids);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}
}
