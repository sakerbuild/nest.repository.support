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
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.nest.bundle.BundleDependencyInformation;
import saker.nest.bundle.BundleKey;
import saker.nest.support.api.dependency.filter.DependencyFilter;

public class ChainDependencyFilter implements DependencyFilter, Externalizable {
	private static final DependencyFilter[] EMPTY_DEPENDENCY_FILTER_ARRAY = new DependencyFilter[0];

	private static final long serialVersionUID = 1L;

	private DependencyFilter[] filters;

	/**
	 * For {@link Externalizable}.
	 */
	public ChainDependencyFilter() {
	}

	private ChainDependencyFilter(DependencyFilter[] filters) {
		this.filters = filters;
	}

	public static DependencyFilter create(Collection<? extends DependencyFilter> filters) throws NullPointerException {
		Objects.requireNonNull(filters, "filters");
		if (filters.isEmpty()) {
			return IdentityDependencyFilter.INSTANCE;
		}
		DependencyFilter[] array = filters.toArray(EMPTY_DEPENDENCY_FILTER_ARRAY);
		if (array.length == 0) {
			//check just in case
			return IdentityDependencyFilter.INSTANCE;
		}
		if (ObjectUtils.hasNull((Object[]) array)) {
			throw new NullPointerException("Null dependency filter element in: " + Arrays.toString(array));
		}
		if (array.length == 1) {
			return array[0];
		}
		return new ChainDependencyFilter(array);
	}

	@Override
	public BundleDependencyInformation filterBundleDependency(BundleKey owner, BundleDependencyInformation depinfo) {
		for (DependencyFilter f : filters) {
			depinfo = f.filterBundleDependency(owner, depinfo);
			if (depinfo == null) {
				return null;
			}
		}
		return depinfo;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(filters);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		filters = (DependencyFilter[]) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(filters);
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
		ChainDependencyFilter other = (ChainDependencyFilter) obj;
		if (!Arrays.equals(filters, other.filters))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + StringUtils.toStringJoin(", ", filters) + "]";
	}

}
