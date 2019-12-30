package saker.nest.support.api.property;

import saker.build.file.content.ContentDescriptor;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.NestRepositoryBundle;

/**
 * Data interface providing access to a bundle and the associated content descriptor.
 * <p>
 * The {@linkplain #getBundle() bundle reference} is a transient property of the instance, and will not be kept when the
 * instance of this interface is serialized and deserialized.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * An instance is created by the {@link BundlePropertyUtils#bundleContentDescriptorExecutionProperty(BundleKey)}
 * execution property.
 */
public interface BundleContentDescriptorPropertyValue {
	/**
	 * Gets the content descriptor of the associated bundle.
	 * <p>
	 * The descriptor doesn't include the dependencies, and is usually based on the
	 * {@linkplain NestRepositoryBundle#getHash() hash} of the contents of the bundle.
	 * 
	 * @return The content descriptor.
	 */
	public ContentDescriptor getContentDescriptor();

	/**
	 * Gets the reference to the loaded bundle.
	 * <p>
	 * This property is not persisted when the instance is serialized.
	 * 
	 * @return The reference to the bundle.
	 */
	public NestRepositoryBundle getBundle();
}
