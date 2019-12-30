package saker.nest.support.impl.local.install;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.file.path.SakerPath;
import saker.build.task.identifier.TaskIdentifier;

public class BundleInstallerTaskIdentifier implements TaskIdentifier, Externalizable {
	private static final long serialVersionUID = 1L;

	protected String storageName;
	protected SakerPath bundlePath;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleInstallerTaskIdentifier() {
	}

	public BundleInstallerTaskIdentifier(String storageName, SakerPath bundlePath) {
		Objects.requireNonNull(storageName, "storage name");
		Objects.requireNonNull(bundlePath, "bundle path");
		this.storageName = storageName;
		this.bundlePath = bundlePath;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(storageName);
		out.writeObject(bundlePath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		storageName = (String) in.readObject();
		bundlePath = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundlePath == null) ? 0 : bundlePath.hashCode());
		result = prime * result + ((storageName == null) ? 0 : storageName.hashCode());
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
		BundleInstallerTaskIdentifier other = (BundleInstallerTaskIdentifier) obj;
		if (bundlePath == null) {
			if (other.bundlePath != null)
				return false;
		} else if (!bundlePath.equals(other.bundlePath))
			return false;
		if (storageName == null) {
			if (other.storageName != null)
				return false;
		} else if (!storageName.equals(other.storageName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BundleInstallerTaskIdentifier[storageName=" + storageName + ", bundlePath=" + bundlePath + "]";
	}

}