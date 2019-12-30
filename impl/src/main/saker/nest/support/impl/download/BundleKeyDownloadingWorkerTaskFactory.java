package saker.nest.support.impl.download;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.exception.PropertyComputationFailedException;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.JarNestRepositoryBundle;
import saker.nest.bundle.NestRepositoryBundle;
import saker.nest.support.api.download.DownloadBundleWorkerTaskOutput;
import saker.nest.support.api.property.BundleContentDescriptorPropertyValue;
import saker.nest.support.impl.util.BundleKeyContentDescriptorExecutionProperty;
import saker.nest.support.main.download.DownloadBundleTaskFactory;

public class BundleKeyDownloadingWorkerTaskFactory
		implements TaskFactory<DownloadBundleWorkerTaskOutput>, Task<DownloadBundleWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private BundleKey bundleKey;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleKeyDownloadingWorkerTaskFactory() {
	}

	public BundleKeyDownloadingWorkerTaskFactory(BundleKey bundleKey) {
		this.bundleKey = bundleKey;
	}

	@Override
	public Task<? extends DownloadBundleWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public DownloadBundleWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		BundleContentDescriptorPropertyValue propertyresult;
		try {
			propertyresult = taskcontext.getTaskUtilities()
					.getReportExecutionDependency(new BundleKeyContentDescriptorExecutionProperty(bundleKey));
		} catch (PropertyComputationFailedException e) {
			throw ObjectUtils.sneakyThrow(e.getCause());
		}
		NestRepositoryBundle bundle = propertyresult.getBundle();
		ContentDescriptor contentdescriptor = propertyresult.getContentDescriptor();

		SakerPath outpath = executeBundleDownload(taskcontext, bundle, contentdescriptor);
		//XXX self output change detection based on content descriptor
		return new SimpleDownloadWorkerTaskOutput(outpath, bundleKey, contentdescriptor);
	}

	private static SakerPath executeBundleDownload(TaskContext taskcontext, NestRepositoryBundle bundle,
			ContentDescriptor contentdescriptor) throws IOException {
		SakerDirectory builddir = SakerPathFiles.requireBuildDirectory(taskcontext.getExecutionContext())
				.getDirectoryCreate(DownloadBundleTaskFactory.TASK_NAME);
		String bundlefilename = createBundleDownloadFileName(bundle);
		SakerFile bundlefile = taskcontext.getTaskUtilities().createProviderPathFile(bundlefilename,
				LocalFileProvider.getInstance().getPathKey(((JarNestRepositoryBundle) bundle).getJarPath()),
				contentdescriptor);

		SakerDirectory outdir = builddir.getDirectoryCreate(bundle.getBundleIdentifier().getName().toString());
		SakerFile syncfile;
		while (true) {
			SakerFile prevfile = outdir.addIfAbsent(bundlefile);
			if (prevfile != null) {
				//a file was already present
				//check if it has the same content descriptor, in which case we don't need to overwrite
				if (!Objects.equals(prevfile.getContentDescriptor(), contentdescriptor)) {
					//overwrite it
					prevfile.remove();
					continue;
				} else {
					syncfile = prevfile;
				}
			} else {
				syncfile = bundlefile;
			}
			break;
		}
		syncfile.synchronize();
		SakerPath outpath = outdir.getSakerPath().resolve(bundlefilename);

		taskcontext.reportOutputFileDependency(null, outpath, contentdescriptor);
		return outpath;
	}

	private static String createBundleDownloadFileName(NestRepositoryBundle bundle) {
		return bundle.getBundleIdentifier() + "_" + StringUtils.toHexString(bundle.getHash()) + ".jar";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(bundleKey);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundleKey = (BundleKey) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleKey == null) ? 0 : bundleKey.hashCode());
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
		BundleKeyDownloadingWorkerTaskFactory other = (BundleKeyDownloadingWorkerTaskFactory) obj;
		if (bundleKey == null) {
			if (other.bundleKey != null)
				return false;
		} else if (!bundleKey.equals(other.bundleKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (bundleKey != null ? "bundleKey=" + bundleKey : "") + "]";
	}
}