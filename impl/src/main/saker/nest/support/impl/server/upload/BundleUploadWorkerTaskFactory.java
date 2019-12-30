package saker.nest.support.impl.server.upload;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.jar.JarInputStream;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.util.property.BuildTimeExecutionProperty;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleInformation;
import saker.nest.bundle.storage.ServerStorageUtils;
import saker.nest.bundle.storage.ServerStorageUtils.UploadResult;
import saker.nest.exc.InvalidNestBundleException;
import saker.nest.support.api.server.upload.BundleUploadWorkerTaskOutput;
import saker.nest.support.main.server.upload.ServerUploadTaskFactory;

public class BundleUploadWorkerTaskFactory
		implements TaskFactory<BundleUploadWorkerTaskOutput>, Task<BundleUploadWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath bundlePath;
	private Boolean overwrite;
	private String server;
	private byte[] apiKey;
	private byte[] apiSecret;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleUploadWorkerTaskFactory() {
	}

	public BundleUploadWorkerTaskFactory(SakerPath bundlePath, Boolean overwrite, String server, byte[] apiKey,
			byte[] apiSecret) {
		this.bundlePath = bundlePath;
		this.overwrite = overwrite;
		this.server = server;
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
	}

	@Override
	public BundleUploadWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		taskcontext.setStandardOutDisplayIdentifier(ServerUploadTaskFactory.TASK_NAME);

		SakerFile file = SakerPathFiles.resolveFileAtPath(taskcontext, bundlePath);
		if (file == null) {
			taskcontext.reportInputFileDependency(null, bundlePath, CommonTaskContentDescriptors.IS_NOT_FILE);
			taskcontext.abortExecution(new FileNotFoundException(bundlePath.toString()));
			return null;
		}
		taskcontext.getTaskUtilities().reportInputFileDependency(null, file);

		BundleInformation bundleinfo;

		try (InputStream fileis = file.openInputStream();
				JarInputStream jaris = new JarInputStream(fileis)) {
			bundleinfo = new BundleInformation(jaris);
		} catch (InvalidNestBundleException e) {
			taskcontext.abortExecution(e);
			return null;
		}

		String url = server;
		BundleIdentifier bundleid = bundleinfo.getBundleIdentifier();
		UploadResult uploadresult = ServerStorageUtils.uploadBundle(url, bundleid, file, apiKey, apiSecret, overwrite);
		SakerLog.success().verbose()
				.println("Uploaded bundle: " + bundleid + "\n    SHA256: "
						+ StringUtils.toHexString(uploadresult.getSHA256Hash()) + "\n    MD5: "
						+ StringUtils.toHexString(uploadresult.getMD5Hash()));

		//report build time dependency to always reinvoke the upload task.
		taskcontext.reportExecutionDependency(BuildTimeExecutionProperty.INSTANCE, null);

		String md5 = StringUtils.toHexString(uploadresult.getMD5Hash());
		String sha256 = StringUtils.toHexString(uploadresult.getSHA256Hash());
		return new BundleUploadWorkerTaskOutputImpl(bundleid, md5, sha256);
	}

	@Override
	public Task<? extends BundleUploadWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(bundlePath);
		out.writeObject(server);
		out.writeObject(overwrite);
		out.writeObject(apiKey);
		out.writeObject(apiSecret);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundlePath = (SakerPath) in.readObject();
		server = (String) in.readObject();
		overwrite = (Boolean) in.readObject();
		apiKey = (byte[]) in.readObject();
		apiSecret = (byte[]) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(apiKey);
		result = prime * result + Arrays.hashCode(apiSecret);
		result = prime * result + ((bundlePath == null) ? 0 : bundlePath.hashCode());
		result = prime * result + ((overwrite == null) ? 0 : overwrite.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BundleUploadWorkerTaskFactory other = (BundleUploadWorkerTaskFactory) obj;
		if (!Arrays.equals(apiKey, other.apiKey))
			return false;
		if (!Arrays.equals(apiSecret, other.apiSecret))
			return false;
		if (bundlePath == null) {
			if (other.bundlePath != null)
				return false;
		} else if (!bundlePath.equals(other.bundlePath))
			return false;
		if (overwrite == null) {
			if (other.overwrite != null)
				return false;
		} else if (!overwrite.equals(other.overwrite))
			return false;
		if (server == null) {
			if (other.server != null)
				return false;
		} else if (!server.equals(other.server))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (bundlePath != null ? "bundlePath=" + bundlePath + ", " : "")
				+ (overwrite != null ? "overwrite=" + overwrite + ", " : "")
				+ (server != null ? "server=" + server : "") + "]";
	}

}