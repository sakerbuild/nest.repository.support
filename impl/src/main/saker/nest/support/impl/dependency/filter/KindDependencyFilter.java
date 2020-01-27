/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.nest.support.impl.dependency.filter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.nest.bundle.BundleDependency;
import saker.nest.bundle.BundleDependencyInformation;
import saker.nest.bundle.BundleDependencyList;
import saker.nest.bundle.BundleKey;
import saker.nest.support.api.dependency.filter.DependencyFilter;

public class KindDependencyFilter implements DependencyFilter, Externalizable {
	private static final long serialVersionUID = 1L;

	protected NavigableSet<String> dependencyKinds;

	/**
	 * For {@link Externalizable}.
	 */
	public KindDependencyFilter() {
	}

	private KindDependencyFilter(NavigableSet<String> dependencyKinds) {
		this.dependencyKinds = dependencyKinds;
	}

	public static DependencyFilter create(NavigableSet<String> dependencyKinds) {
		Objects.requireNonNull(dependencyKinds, "dependency kinds");
		if (dependencyKinds.isEmpty()) {
			return NoneDependencyFilter.INSTANCE;
		}
		return new KindDependencyFilter(dependencyKinds);
	}

	@Override
	public BundleDependencyInformation filterBundleDependency(BundleKey owner, BundleDependencyInformation depinfo) {
		return filterBundleDependencyInformation(depinfo, dependencyKinds);
	}

	public static BundleDependencyInformation filterBundleDependencyInformation(BundleDependencyInformation depinfo,
			NavigableSet<String> kinds) {
		if (depinfo == null) {
			return null;
		}
		return depinfo.filter((bid, deplist) -> filterDependencyList(deplist, kinds));
	}

	public static BundleDependencyList filterDependencyList(BundleDependencyList deplist, Set<String> lookupkinds) {
		if (deplist == null) {
			return null;
		}
		return deplist.filter(dep -> {
			Set<String> depkinds = dep.getKinds();
			if (ObjectUtils.containsAny(depkinds, lookupkinds)) {
				BundleDependency.Builder b = BundleDependency.builder(dep);
				b.clearKinds();
				for (String kind : lookupkinds) {
					if (depkinds.contains(kind)) {
						b.addKind(kind);
					}
				}
				return b.build();
			}
			return null;
		});
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, dependencyKinds);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		dependencyKinds = SerialUtils.readExternalSortedImmutableNavigableSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dependencyKinds == null) ? 0 : dependencyKinds.hashCode());
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
		KindDependencyFilter other = (KindDependencyFilter) obj;
		if (dependencyKinds == null) {
			if (other.dependencyKinds != null)
				return false;
		} else if (!dependencyKinds.equals(other.dependencyKinds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (dependencyKinds != null ? "dependencyKinds=" + dependencyKinds : "")
				+ "]";
	}

}
