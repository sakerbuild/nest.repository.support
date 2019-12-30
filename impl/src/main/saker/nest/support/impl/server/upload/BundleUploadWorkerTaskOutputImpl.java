package saker.nest.support.impl.server.upload;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.nest.bundle.BundleIdentifier;
import saker.nest.support.api.server.upload.BundleUploadWorkerTaskOutput;

class BundleUploadWorkerTaskOutputImpl implements BundleUploadWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private BundleIdentifier bundleIdentifier;
	private String md5;
	private String sha256;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleUploadWorkerTaskOutputImpl() {
	}

	public BundleUploadWorkerTaskOutputImpl(BundleIdentifier bundleIdentifier, String md5, String sha256) {
		this.bundleIdentifier = bundleIdentifier;
		this.md5 = md5;
		this.sha256 = sha256;
	}

	@Override
	public BundleIdentifier getBundleIdentifier() {
		return bundleIdentifier;
	}

	@Override
	public String getMD5() {
		return md5;
	}

	@Override
	public String getSHA256() {
		return sha256;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(bundleIdentifier);
		out.writeObject(md5);
		out.writeObject(sha256);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundleIdentifier = (BundleIdentifier) in.readObject();
		md5 = (String) in.readObject();
		sha256 = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleIdentifier == null) ? 0 : bundleIdentifier.hashCode());
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
		BundleUploadWorkerTaskOutputImpl other = (BundleUploadWorkerTaskOutputImpl) obj;
		if (bundleIdentifier == null) {
			if (other.bundleIdentifier != null)
				return false;
		} else if (!bundleIdentifier.equals(other.bundleIdentifier))
			return false;
		if (md5 == null) {
			if (other.md5 != null)
				return false;
		} else if (!md5.equals(other.md5))
			return false;
		if (sha256 == null) {
			if (other.sha256 != null)
				return false;
		} else if (!sha256.equals(other.sha256))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (bundleIdentifier != null ? "bundleIdentifier=" + bundleIdentifier + ", " : "")
				+ (md5 != null ? "md5=" + md5 + ", " : "") + (sha256 != null ? "sha256=" + sha256 : "") + "]";
	}

}
