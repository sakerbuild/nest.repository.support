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
package saker.nest.support.impl.local.install;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.storage.LocalBundleStorageView;
import saker.nest.bundle.storage.StorageViewKey;

final class NamedLocalBundleStorageExecutionProperty
		implements ExecutionProperty<NamedLocalBundleStorageExecutionProperty.PropertyValue>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final class PropertyValue implements Externalizable {
		private static final long serialVersionUID = 1L;

		private StorageViewKey storageViewKey;
		private transient LocalBundleStorageView storageView;

		/**
		 * Creates a new property value that signals that the storage was not found.
		 * <p>
		 * Also used for {@link Externalizable}.
		 */
		public PropertyValue() {
		}

		public PropertyValue(LocalBundleStorageView storageView) {
			this.storageViewKey = storageView.getStorageViewKey();
			this.storageView = storageView;
		}

		public boolean isPresent() {
			return storageViewKey != null;
		}

		public StorageViewKey getStorageKey() {
			return storageViewKey;
		}

		public LocalBundleStorageView getStorageView() {
			return storageView;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(storageViewKey);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			storageViewKey = (StorageViewKey) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			NamedLocalBundleStorageExecutionProperty.PropertyValue other = (NamedLocalBundleStorageExecutionProperty.PropertyValue) obj;
			if (storageViewKey == null) {
				if (other.storageViewKey != null)
					return false;
			} else if (!storageViewKey.equals(other.storageViewKey))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "PropertyValue[" + storageViewKey + "]";
		}
	}

	private String storageName;

	/**
	 * For {@link Externalizable}.
	 */
	public NamedLocalBundleStorageExecutionProperty() {
	}

	public NamedLocalBundleStorageExecutionProperty(String storageName) {
		this.storageName = storageName;
	}

	@Override
	public NamedLocalBundleStorageExecutionProperty.PropertyValue getCurrentValue(ExecutionContext executioncontext) {
		NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
		Map<String, ? extends LocalBundleStorageView> storages = cl.getBundleStorageConfiguration().getLocalStorages();
		if (storages.isEmpty()) {
			throw new IllegalArgumentException("No local bundle storages configured.");
		}
		if (storageName != null) {
			LocalBundleStorageView storage = storages.get(storageName);
			if (storage == null) {
				throw new IllegalArgumentException("Local bundle storage not found for name: " + storageName);
			}
			return new PropertyValue(storage);
		}
		if (storages.size() > 1) {
			throw new IllegalArgumentException(
					"Failed to determine local bundle storage. Multiple available: " + storages.keySet());
		}
		//there is only 1 entry
		return new PropertyValue(storages.entrySet().iterator().next().getValue());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(storageName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		storageName = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((storageName == null) ? 0 : storageName.hashCode());
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
		NamedLocalBundleStorageExecutionProperty other = (NamedLocalBundleStorageExecutionProperty) obj;
		if (storageName == null) {
			if (other.storageName != null)
				return false;
		} else if (!storageName.equals(other.storageName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[storageName=" + storageName + "]";
	}

}