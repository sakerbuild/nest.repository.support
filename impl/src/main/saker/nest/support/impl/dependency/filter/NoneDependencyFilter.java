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

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.nest.bundle.BundleDependencyInformation;
import saker.nest.bundle.BundleKey;
import saker.nest.support.api.dependency.filter.DependencyFilter;

public final class NoneDependencyFilter implements DependencyFilter, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final DependencyFilter INSTANCE = new NoneDependencyFilter();

	/**
	 * For {@link Externalizable}.
	 */
	public NoneDependencyFilter() {
	}

	@Override
	public BundleDependencyInformation filterBundleDependency(BundleKey owner, BundleDependencyInformation depinfo) {
		return BundleDependencyInformation.EMPTY;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	}

	@Override
	public int hashCode() {
		return getClass().getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return ObjectUtils.isSameClass(this, obj);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[]";
	}

}
