package saker.nest.support.api.download;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;

/**
 * Output of a bundle download worker task.
 * <p>
 * The interface provides access to the download result path as well as additional bundle information.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see DownloadBundleTaskOutput
 */
public interface DownloadBundleWorkerTaskOutput {
	/**
	 * Gets the absolute build execution path of the downloaded bundle.
	 * 
	 * @return The result path.
	 */
	public SakerPath getPath();

	/**
	 * Gets the {@linkplain BundleKey bundle key} of the downloaded bundle.
	 * 
	 * @return The bundle key.
	 */
	public BundleKey getBundleKey();

	/**
	 * Gets the {@linkplain BundleIdentifier bundle identifier} of the downloaded bundle.
	 * 
	 * @return The bundle identifier.
	 */
	public default BundleIdentifier getBundleIdentifier() {
		return getBundleKey().getBundleIdentifier();
	}

	/**
	 * Gets a {@linkplain ContentDescriptor content descriptor} of the contents of the bundle.
	 * <p>
	 * The content descriptor doesn't include the dependencies in it.
	 * 
	 * @return The content descriptor.
	 */
	public ContentDescriptor getContentDescriptor();
}