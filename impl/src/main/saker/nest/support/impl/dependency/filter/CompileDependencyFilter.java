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

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.nest.bundle.BundleDependency;
import saker.nest.bundle.BundleDependencyInformation;
import saker.nest.bundle.BundleKey;
import saker.nest.meta.Versions;
import saker.nest.support.api.dependency.filter.DependencyFilter;

public class CompileDependencyFilter implements DependencyFilter, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final String DEPENDENCY_META_COMPILE_TRANSITIVE = "compile-transitive";

	private NavigableSet<String> kinds;
	private boolean compileTransitive;

	/**
	 * For {@link Externalizable}.
	 */
	public CompileDependencyFilter() {
	}

	private CompileDependencyFilter(NavigableSet<String> kinds, boolean compileTransitive) {
		this.kinds = kinds;
		this.compileTransitive = compileTransitive;
	}

	public static DependencyFilter createTransitive(NavigableSet<String> kinds) {
		Objects.requireNonNull(kinds, "dependency kinds");
		return new CompileDependencyFilter(kinds, true);
	}

	public static DependencyFilter createNonTransitive(NavigableSet<String> kinds) {
		Objects.requireNonNull(kinds, "dependency kinds");
		return new CompileDependencyFilter(kinds, false);
	}

	@Override
	public BundleDependencyInformation filterBundleDependency(BundleKey owner, BundleDependencyInformation depinfo) {
		depinfo = KindDependencyFilter.filterBundleDependencyInformation(depinfo, kinds);
		if (owner == null) {
			//if the root dependency set, dont filter
			return depinfo;
		}
		if (compileTransitive) {
			return depinfo.filter((bi, deplist) -> {
				return deplist.filter(bd -> {
					if (dependencyIsPrivate(bd)) {
						return null;
					}
					return bd;
				});
			});
		}
		return depinfo.filter((bi, deplist) -> {
			return deplist.filter(bd -> {
				if (dependencyIsPrivate(bd)) {
					return null;
				}
				if ("false".equalsIgnoreCase(bd.getMetaData().get(DEPENDENCY_META_COMPILE_TRANSITIVE))) {
					return null;
				}
				return bd;
			});
		});
	}

	private static boolean dependencyIsPrivate(BundleDependency bd) {
		if (Versions.VERSION_MAJOR == 0 && Versions.VERSION_MINOR == 8 && Versions.VERSION_PATCH < 1) {
			return Boolean.parseBoolean(bd.getMetaData().get("private"));
		}
		return bd.isPrivate();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, kinds);
		out.writeBoolean(compileTransitive);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		kinds = SerialUtils.readExternalSortedImmutableNavigableSet(in);
		compileTransitive = in.readBoolean();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (compileTransitive ? 1231 : 1237);
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
		CompileDependencyFilter other = (CompileDependencyFilter) obj;
		if (compileTransitive != other.compileTransitive)
			return false;
		if (kinds == null) {
			if (other.kinds != null)
				return false;
		} else if (!kinds.equals(other.kinds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (kinds != null ? "kinds=" + kinds + ", " : "") + "compileTransitive="
				+ compileTransitive + "]";
	}
}
