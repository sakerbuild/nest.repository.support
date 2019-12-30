package saker.nest.support.impl.server.upload;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.storage.ServerBundleStorageView;
import saker.nest.bundle.storage.StorageViewKey;

final class NamedServerBundleStorageExecutionProperty
		implements ExecutionProperty<NamedServerBundleStorageExecutionProperty.PropertyValue>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final class PropertyValue implements Externalizable {
		private static final long serialVersionUID = 1L;

		private StorageViewKey storageViewKey;
		private transient ServerBundleStorageView storageView;

		/**
		 * Creates a new property value that signals that the storage was not found.
		 * <p>
		 * Also used for {@link Externalizable}.
		 */
		public PropertyValue() {
		}

		public PropertyValue(ServerBundleStorageView storageView) {
			this.storageViewKey = storageView.getStorageViewKey();
			this.storageView = storageView;
		}

		public boolean isPresent() {
			return storageViewKey != null;
		}

		public StorageViewKey getStorageKey() {
			return storageViewKey;
		}

		public ServerBundleStorageView getStorageView() {
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
			NamedServerBundleStorageExecutionProperty.PropertyValue other = (NamedServerBundleStorageExecutionProperty.PropertyValue) obj;
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
	public NamedServerBundleStorageExecutionProperty() {
	}

	public NamedServerBundleStorageExecutionProperty(String storageName) {
		this.storageName = storageName;
	}

	@Override
	public NamedServerBundleStorageExecutionProperty.PropertyValue getCurrentValue(ExecutionContext executioncontext) {
		NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
		Map<String, ? extends ServerBundleStorageView> storages = cl.getBundleStorageConfiguration()
				.getServerStorages();
		if (storages.isEmpty()) {
			throw new IllegalArgumentException("No server bundle storages configured.");
		}
		if (storageName != null) {
			ServerBundleStorageView storage = storages.get(storageName);
			if (storage == null) {
				throw new IllegalArgumentException("Server bundle storage not found for name: " + storageName);
			}
			return new PropertyValue(storage);
		}
		if (storages.size() > 1) {
			throw new IllegalArgumentException(
					"Failed to determine server bundle storage. Multiple available: " + storages.keySet());
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
		NamedServerBundleStorageExecutionProperty other = (NamedServerBundleStorageExecutionProperty) obj;
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