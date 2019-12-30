package saker.nest.support.api.localize;

import saker.build.file.path.SakerPath;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;

/**
 * Output of a bundle localization worker task.
 * <p>
 * The interface provides access to the localization result path as well as additional bundle information.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see LocalizeBundleTaskOutput
 */
public interface LocalizeBundleWorkerTaskOutput {
	/**
	 * Gets the absolute local file system path of the localized bundle.
	 * 
	 * @return The result path.
	 */
	public SakerPath getLocalPath();

	/**
	 * Gets the {@linkplain BundleKey bundle key} of the localized bundle.
	 * 
	 * @return The bundle key.
	 */
	public BundleKey getBundleKey();

	/**
	 * Gets the {@linkplain BundleIdentifier bundle identifier} of the localized bundle.
	 * 
	 * @return The bundle identifier.
	 */
	public default BundleIdentifier getBundleIdentifier() {
		return getBundleKey().getBundleIdentifier();
	}

	// contrary to DownloadBundleWorkerTaskOutput, getContentDescriptor() is intentionally left out.
}
