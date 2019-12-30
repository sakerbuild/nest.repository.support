package saker.nest.support.impl.dependency;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.nest.bundle.BundleKey;
import saker.nest.support.api.dependency.DependencyResolutionTaskOutput;

public class DependencyResolutionTaskOutputImpl implements DependencyResolutionTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private Set<BundleKey> bundleKeys;

	/**
	 * For {@link Externalizable}.
	 */
	public DependencyResolutionTaskOutputImpl() {
	}

	public DependencyResolutionTaskOutputImpl(Set<BundleKey> bundleKeys) {
		this.bundleKeys = ImmutableUtils.makeImmutableLinkedHashSet(bundleKeys);
	}

	@Override
	public Collection<BundleKey> getBundles() {
		return bundleKeys;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, bundleKeys);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundleKeys = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		return bundleKeys.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DependencyResolutionTaskOutput)) {
			return false;
		}
		DependencyResolutionTaskOutput other = (DependencyResolutionTaskOutput) obj;
		if (!Objects.equals(this.getBundles(), other.getBundles())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append("[");
		for (Iterator<BundleKey> it = bundleKeys.iterator(); it.hasNext();) {
			BundleKey bk = it.next();
			sb.append(bk.getBundleIdentifier());
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

}
