package saker.nest.support.impl.dependency.filter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableSet;
import java.util.Objects;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.nest.bundle.BundleDependencyInformation;
import saker.nest.bundle.BundleKey;
import saker.nest.support.api.dependency.filter.DependencyFilter;

public class ClasspathDependencyFilter implements DependencyFilter, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final String DEPENDENCY_META_COMPILE_TRANSITIVE = "compile-transitive";

	private NavigableSet<String> kinds;

	/**
	 * For {@link Externalizable}.
	 */
	public ClasspathDependencyFilter() {
	}

	private ClasspathDependencyFilter(NavigableSet<String> kinds) {
		this.kinds = kinds;
	}

	public static DependencyFilter createTransitive(NavigableSet<String> kinds) {
		return new KindDependencyFilter(kinds);
	}

	public static DependencyFilter createNonTransitive(NavigableSet<String> kinds) {
		Objects.requireNonNull(kinds, "dependency kinds");
		return new ClasspathDependencyFilter(kinds);
	}

	@Override
	public BundleDependencyInformation filterBundleDependency(BundleKey owner, BundleDependencyInformation depinfo) {
		depinfo = KindDependencyFilter.filterBundleDependencyInformation(depinfo, kinds);
		if (owner == null) {
			//if the root dependency set, dont filter
			return depinfo;
		}
		return depinfo.filter((bi, deplist) -> {
			return deplist.filter(bd -> {
				if ("false".equalsIgnoreCase(bd.getMetaData().get(DEPENDENCY_META_COMPILE_TRANSITIVE))) {
					return null;
				}
				return bd;
			});
		});
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, kinds);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		kinds = SerialUtils.readExternalSortedImmutableNavigableSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kinds == null) ? 0 : kinds.hashCode());
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
		ClasspathDependencyFilter other = (ClasspathDependencyFilter) obj;
		if (kinds == null) {
			if (other.kinds != null)
				return false;
		} else if (!kinds.equals(other.kinds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + kinds + "]";
	}
}
