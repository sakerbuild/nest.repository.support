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
package saker.nest.support.impl.dependency;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.lookup.LookupKey;

public class RootBundleLookupKeyExecutionProperty implements ExecutionProperty<LookupKey>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final RootBundleLookupKeyExecutionProperty INSTANCE = new RootBundleLookupKeyExecutionProperty();

	/**
	 * For {@link Externalizable}.
	 * 
	 * @see #INSTANCE
	 */
	public RootBundleLookupKeyExecutionProperty() {
	}

	@Override
	public LookupKey getCurrentValue(ExecutionContext executioncontext) {
		return ((NestBundleClassLoader) this.getClass().getClassLoader()).getBundleStorageConfiguration()
				.getBundleLookup().getLookupKey();
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
