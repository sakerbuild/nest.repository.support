package saker.nest.support.impl.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.HashContentDescriptor;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.NestRepositoryBundle;
import saker.nest.bundle.storage.BundleStorageView;
import saker.nest.exc.BundleLoadingFailedException;
import saker.nest.support.api.property.BundleContentDescriptorPropertyValue;

public class BundleKeyContentDescriptorExecutionProperty
		implements ExecutionProperty<BundleContentDescriptorPropertyValue>, Externalizable {
	public static class PropertyResult implements Externalizable, BundleContentDescriptorPropertyValue {
		private static final long serialVersionUID = 1L;

		private transient NestRepositoryBundle bundle;
		private ContentDescriptor contentDescriptor;

		/**
		 * For {@link Externalizable}.
		 */
		public PropertyResult() {
		}

		public PropertyResult(NestRepositoryBundle bundle, ContentDescriptor contentDescriptor) {
			this.bundle = bundle;
			this.contentDescriptor = contentDescriptor;
		}

		@Override
		public NestRepositoryBundle getBundle() {
			return bundle;
		}

		@Override
		public ContentDescriptor getContentDescriptor() {
			return contentDescriptor;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(contentDescriptor);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			contentDescriptor = (ContentDescriptor) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((contentDescriptor == null) ? 0 : contentDescriptor.hashCode());
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
			PropertyResult other = (PropertyResult) obj;
			if (contentDescriptor == null) {
				if (other.contentDescriptor != null)
					return false;
			} else if (!contentDescriptor.equals(other.contentDescriptor))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + contentDescriptor + "]";
		}

	}

	private static final long serialVersionUID = 1L;

	private BundleKey bundleKey;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleKeyContentDescriptorExecutionProperty() {
	}

	public BundleKeyContentDescriptorExecutionProperty(BundleKey bundleKey) {
		this.bundleKey = bundleKey;
	}

	@Override
	public PropertyResult getCurrentValue(ExecutionContext executioncontext)
			throws IllegalArgumentException, BundleLoadingFailedException {
		NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
		BundleStorageView storageview = cl.getBundleStorageConfiguration()
				.getBundleStorageViewForKey(bundleKey.getStorageViewKey());
		if (storageview == null) {
			throw new IllegalArgumentException("Storage view not found: " + bundleKey.getStorageViewKey());
		}
		NestRepositoryBundle bundle = storageview.getBundle(bundleKey.getBundleIdentifier());
		byte[] hash = bundle.getHash();
		return new PropertyResult(bundle, createContentDescriptorForBundleHash(hash));
	}

	public static ContentDescriptor createContentDescriptorForBundleHash(byte[] hash) {
		return HashContentDescriptor.createWithHash(hash);
	}

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
		BundleKeyContentDescriptorExecutionProperty other = (BundleKeyContentDescriptorExecutionProperty) obj;
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
