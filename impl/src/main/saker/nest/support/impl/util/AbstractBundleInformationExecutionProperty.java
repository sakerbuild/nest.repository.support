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
package saker.nest.support.impl.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.nest.bundle.BundleInformation;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.storage.BundleStorageView;

public abstract class AbstractBundleInformationExecutionProperty<R> implements ExecutionProperty<R>, Externalizable {
	private static final long serialVersionUID = 1L;

	protected BundleKey bundleKey;

	/**
	 * For {@link Externalizable}.
	 */
	public AbstractBundleInformationExecutionProperty() {
	}

	public AbstractBundleInformationExecutionProperty(BundleKey bundleKey) {
		this.bundleKey = bundleKey;
	}

	@Override
	public R getCurrentValue(ExecutionContext executioncontext) throws Exception {
		NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
		BundleStorageView storageview = cl.getBundleStorageConfiguration()
				.getBundleStorageViewForKey(bundleKey.getStorageViewKey());
		if (storageview == null) {
			throw new IllegalArgumentException("Storage view not found: " + bundleKey.getStorageViewKey());
		}
		BundleInformation bundleinformation = storageview.getBundleInformation(bundleKey.getBundleIdentifier());
		return getPropertyValue(bundleinformation);
	}

	protected abstract R getPropertyValue(BundleInformation bundleinfo);

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(bundleKey);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundleKey = (BundleKey) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleKey == null) ? 0 : bundleKey.hashCode());
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
		AbstractBundleInformationExecutionProperty<?> other = (AbstractBundleInformationExecutionProperty<?>) obj;
		if (bundleKey == null) {
			if (other.bundleKey != null)
				return false;
		} else if (!bundleKey.equals(other.bundleKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (bundleKey != null ? "bundleKey=" + bundleKey : "") + "]";
	}
}
