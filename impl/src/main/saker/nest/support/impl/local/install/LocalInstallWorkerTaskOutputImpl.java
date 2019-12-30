package saker.nest.support.impl.local.install;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.nest.bundle.BundleIdentifier;
import saker.nest.support.api.local.install.LocalInstallWorkerTaskOutput;

public class LocalInstallWorkerTaskOutputImpl implements LocalInstallWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private BundleIdentifier bundleIdentifier;
	private String hash;

	/**
	 * For {@link Externalizable}.
	 */
	public LocalInstallWorkerTaskOutputImpl() {
	}

	public LocalInstallWorkerTaskOutputImpl(BundleIdentifier bundleIdentifier, String hash) {
		this.bundleIdentifier = bundleIdentifier;
		this.hash = hash;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(bundleIdentifier);
		out.writeObject(hash);
	}

	@Override
	public BundleIdentifier getBundleIdentifier() {
		return bundleIdentifier;
	}

	@Override
	public String getBundleHash() {
		return hash;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundleIdentifier = (BundleIdentifier) in.readObject();
		hash = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleIdentifier == null) ? 0 : bundleIdentifier.hashCode());
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
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
		LocalInstallWorkerTaskOutputImpl other = (LocalInstallWorkerTaskOutputImpl) obj;
		if (bundleIdentifier == null) {
			if (other.bundleIdentifier != null)
				return false;
		} else if (!bundleIdentifier.equals(other.bundleIdentifier))
			return false;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (bundleIdentifier != null ? "bundleIdentifier=" + bundleIdentifier + ", " : "")
				+ (hash != null ? "md5=" + hash : "") + "]";
	}

}
