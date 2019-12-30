package saker.nest.support.impl.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.lookup.BundleInformationLookupResult;
import saker.nest.bundle.lookup.BundleLookup;

public class BundleIdentifierBundleKeyExecutionProperty implements ExecutionProperty<BundleKey>, Externalizable {
	private static final long serialVersionUID = 1L;

	private BundleIdentifier bundleId;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleIdentifierBundleKeyExecutionProperty() {
	}

	public BundleIdentifierBundleKeyExecutionProperty(BundleIdentifier bundleId) {
		Objects.requireNonNull(bundleId, "bundle identifier");
		this.bundleId = bundleId;
	}

	@Override
	public BundleKey getCurrentValue(ExecutionContext executioncontext) throws Exception {
		NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
		BundleLookup lookup = cl.getBundleStorageConfiguration().getBundleLookup();
		BundleInformationLookupResult lookupresult = lookup.lookupBundleInformation(bundleId);
		return BundleKey.create(lookupresult.getStorageView().getStorageViewKey(), bundleId);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(bundleId);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundleId = (BundleIdentifier) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleId == null) ? 0 : bundleId.hashCode());
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
		BundleIdentifierBundleKeyExecutionProperty other = (BundleIdentifierBundleKeyExecutionProperty) obj;
		if (bundleId == null) {
			if (other.bundleId != null)
				return false;
		} else if (!bundleId.equals(other.bundleId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + bundleId + "]";
	}

}
