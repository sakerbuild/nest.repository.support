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
import java.util.function.Supplier;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleInformation;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.storage.BundleStorageView;
import saker.nest.bundle.storage.StorageViewKey;

public class BundleInformationExecutionProperty implements ExecutionProperty<BundleInformation>, Externalizable {
	private static final long serialVersionUID = 1L;

	private transient Supplier<BundleStorageView> storageView;
	private StorageViewKey storageViewKey;
	private BundleIdentifier bundleIdentifier;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleInformationExecutionProperty() {
	}

	public BundleInformationExecutionProperty(BundleStorageView storageView, BundleIdentifier bundleIdentifier) {
		this.storageView = Functionals.valSupplier(storageView);
		this.bundleIdentifier = bundleIdentifier;
		this.storageViewKey = storageView.getStorageViewKey();
	}

	@Override
	public BundleInformation getCurrentValue(ExecutionContext executioncontext) throws Exception {
		BundleStorageView sv = storageView.get();
		if (sv == null) {
			return null;
		}
		return sv.getBundleInformation(bundleIdentifier);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(storageViewKey);
		out.writeObject(bundleIdentifier);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		storageViewKey = (StorageViewKey) in.readObject();
		bundleIdentifier = (BundleIdentifier) in.readObject();

		storageView = LazySupplier.of(() -> ((NestBundleClassLoader) this.getClass().getClassLoader())
				.getBundleStorageConfiguration().getBundleStorageViewForKey(storageViewKey));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleIdentifier == null) ? 0 : bundleIdentifier.hashCode());
		result = prime * result + ((storageViewKey == null) ? 0 : storageViewKey.hashCode());
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
		BundleInformationExecutionProperty other = (BundleInformationExecutionProperty) obj;
		if (bundleIdentifier == null) {
			if (other.bundleIdentifier != null)
				return false;
		} else if (!bundleIdentifier.equals(other.bundleIdentifier))
			return false;
		if (storageViewKey == null) {
			if (other.storageViewKey != null)
				return false;
		} else if (!storageViewKey.equals(other.storageViewKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (storageViewKey != null ? "storageViewKey=" + storageViewKey + ", " : "")
				+ (bundleIdentifier != null ? "bundleIdentifier=" + bundleIdentifier : "") + "]";
	}

}
