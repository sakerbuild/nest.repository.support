package saker.nest.support.impl.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.JarNestRepositoryBundle;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.NestRepositoryBundle;
import saker.nest.bundle.storage.BundleStorageView;
import saker.nest.exc.BundleLoadingFailedException;
import saker.nest.support.api.property.BundleContentDescriptorPathPropertyValue;

public class BundleKeyContentDescriptorPathExecutionProperty
		implements ExecutionProperty<BundleContentDescriptorPathPropertyValue>, Externalizable {
	public static class PropertyResult implements Externalizable, BundleContentDescriptorPathPropertyValue {
		private static final long serialVersionUID = 1L;

		private SakerPath localBundlePath;
		private ContentDescriptor contentDescriptor;

		/**
		 * For {@link Externalizable}.
		 */
		public PropertyResult() {
		}

		public PropertyResult(SakerPath localBundlePath, ContentDescriptor contentDescriptor) {
			this.localBundlePath = localBundlePath;
			this.contentDescriptor = contentDescriptor;
		}

		@Override
		public SakerPath getLocalPath() {
			return localBundlePath;
		}

		@Override
		public ContentDescriptor getContentDescriptor() {
			return contentDescriptor;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(localBundlePath);
			out.writeObject(contentDescriptor);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			localBundlePath = (SakerPath) in.readObject();
			contentDescriptor = (ContentDescriptor) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((contentDescriptor == null) ? 0 : contentDescriptor.hashCode());
			result = prime * result + ((localBundlePath == null) ? 0 : localBundlePath.hashCode());
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
			if (localBundlePath == null) {
				if (other.localBundlePath != null)
					return false;
			} else if (!localBundlePath.equals(other.localBundlePath))
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
	public BundleKeyContentDescriptorPathExecutionProperty() {
	}

	public BundleKeyContentDescriptorPathExecutionProperty(BundleKey bundleKey) {
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
		SakerPath localpath;
		if (bundle instanceof JarNestRepositoryBundle) {
			localpath = SakerPath.valueOf(((JarNestRepositoryBundle) bundle).getJarPath());
		} else {
			//the bundle API evolved, a new kind of bundle was added and this code wasn't updated
			throw new IllegalArgumentException("Bundle not a JAR. Cannot determine local path. (" + bundle + ")");
		}
		return new PropertyResult(localpath,
				BundleKeyContentDescriptorExecutionProperty.createContentDescriptorForBundleHash(bundle.getHash()));
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
		BundleKeyContentDescriptorPathExecutionProperty other = (BundleKeyContentDescriptorPathExecutionProperty) obj;
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
