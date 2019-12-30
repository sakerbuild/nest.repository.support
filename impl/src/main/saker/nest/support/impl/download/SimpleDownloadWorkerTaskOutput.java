package saker.nest.support.impl.download;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.nest.bundle.BundleKey;
import saker.nest.support.api.download.DownloadBundleWorkerTaskOutput;

public class SimpleDownloadWorkerTaskOutput implements DownloadBundleWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath path;
	private BundleKey bundleKey;
	private ContentDescriptor contentDescriptor;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleDownloadWorkerTaskOutput() {
	}

	public SimpleDownloadWorkerTaskOutput(SakerPath path, BundleKey bundleKey, ContentDescriptor contentdescriptor) {
		this.path = path;
		this.bundleKey = bundleKey;
		this.contentDescriptor = contentdescriptor;
	}

	@Override
	public SakerPath getPath() {
		return path;
	}

	@Override
	public BundleKey getBundleKey() {
		return bundleKey;
	}

	@Override
	public ContentDescriptor getContentDescriptor() {
		return contentDescriptor;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
		out.writeObject(bundleKey);
		out.writeObject(contentDescriptor);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = (SakerPath) in.readObject();
		bundleKey = (BundleKey) in.readObject();
		contentDescriptor = (ContentDescriptor) in.readObject();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (path != null ? "path=" + path : "") + "]";
	}

}
