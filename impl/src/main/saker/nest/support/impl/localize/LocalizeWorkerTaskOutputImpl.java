package saker.nest.support.impl.localize;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.nest.bundle.BundleKey;
import saker.nest.support.api.localize.LocalizeBundleWorkerTaskOutput;

public class LocalizeWorkerTaskOutputImpl implements LocalizeBundleWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private BundleKey bundleKey;
	private SakerPath localPath;

	/**
	 * For {@link Externalizable}.
	 */
	public LocalizeWorkerTaskOutputImpl() {
	}

	public LocalizeWorkerTaskOutputImpl(BundleKey bundleKey, SakerPath localPath) {
		this.bundleKey = bundleKey;
		this.localPath = localPath;
	}

	@Override
	public SakerPath getLocalPath() {
		return localPath;
	}

	@Override
	public BundleKey getBundleKey() {
		return bundleKey;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(bundleKey);
		out.writeObject(localPath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundleKey = (BundleKey) in.readObject();
		localPath = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleKey == null) ? 0 : bundleKey.hashCode());
		result = prime * result + ((localPath == null) ? 0 : localPath.hashCode());
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
		LocalizeWorkerTaskOutputImpl other = (LocalizeWorkerTaskOutputImpl) obj;
		if (bundleKey == null) {
			if (other.bundleKey != null)
				return false;
		} else if (!bundleKey.equals(other.bundleKey))
			return false;
		if (localPath == null) {
			if (other.localPath != null)
				return false;
		} else if (!localPath.equals(other.localPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (bundleKey != null ? "bundleKey=" + bundleKey + ", " : "")
				+ (localPath != null ? "localPath=" + localPath : "") + "]";
	}

}
