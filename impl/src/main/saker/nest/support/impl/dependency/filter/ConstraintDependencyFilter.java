package saker.nest.support.impl.dependency.filter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.nest.bundle.BundleDependencyInformation;
import saker.nest.bundle.BundleDependencyList;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.DependencyConstraintConfiguration;
import saker.nest.dependency.DependencyUtils;
import saker.nest.support.api.dependency.filter.DependencyFilter;

public class ConstraintDependencyFilter implements DependencyFilter, Externalizable {
	private static final long serialVersionUID = 1L;

	private DependencyConstraintConfiguration constraints;

	/**
	 * For {@link Externalizable}.
	 */
	public ConstraintDependencyFilter() {
	}

	public ConstraintDependencyFilter(DependencyConstraintConfiguration constraints) {
		Objects.requireNonNull(constraints, "constraints");
		this.constraints = constraints;
	}

	@Override
	public BundleDependencyInformation filterBundleDependency(BundleKey owner, BundleDependencyInformation depinfo) {
		return filterBundleDependencyInformation(depinfo, constraints);
	}

	public static BundleDependencyInformation filterBundleDependencyInformation(BundleDependencyInformation depinfo,
			DependencyConstraintConfiguration constraints) {
		if (depinfo == null) {
			return null;
		}
		return depinfo.filter((bid, deplist) -> filterDependencyList(deplist, constraints));
	}

	public static BundleDependencyList filterDependencyList(BundleDependencyList deplist,
			DependencyConstraintConfiguration constraints) {
		if (deplist == null) {
			return null;
		}
		return deplist.filter(dep -> {
			if (DependencyUtils.isDependencyConstraintExcludes(constraints, dep)) {
				return null;
			}
			return dep;
		});
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(constraints);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		constraints = (DependencyConstraintConfiguration) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((constraints == null) ? 0 : constraints.hashCode());
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
		ConstraintDependencyFilter other = (ConstraintDependencyFilter) obj;
		if (constraints == null) {
			if (other.constraints != null)
				return false;
		} else if (!constraints.equals(other.constraints))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (constraints != null ? "constraints=" + constraints : "") + "]";
	}

}
