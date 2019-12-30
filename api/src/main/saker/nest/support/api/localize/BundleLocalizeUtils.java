package saker.nest.support.api.localize;

import java.util.Objects;

import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.support.impl.localize.BundleKeyLocalizingWorkerTaskFactory;
import saker.nest.support.impl.localize.BundleLocalizeWorkerTaskIdentifierImpl;

/**
 * Utility class for working with bundle localization functionality.
 */
public final class BundleLocalizeUtils {
	private BundleLocalizeUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a task identifier that can be used to start a bundle localization task.
	 * <p>
	 * The task identifer should be used with the {@link #createBundleLocalizeWorkerTask(BundleKey)} task.
	 * 
	 * @param bundlekey
	 *            The bundle key of the bundle that is being localized.
	 * @return The task identifier.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public static TaskIdentifier createBundleLocalizeWorkerTaskIdentifier(BundleKey bundlekey)
			throws NullPointerException {
		Objects.requireNonNull(bundlekey, "bundle key");
		return new BundleLocalizeWorkerTaskIdentifierImpl(bundlekey);
	}

	/**
	 * Creates a build task that localizes the specified bundle.
	 * 
	 * @param bundlekey
	 *            The bundle key of the bundle that is being localized.
	 * @return The bundle localization task.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see {@link #createBundleLocalizeWorkerTaskIdentifier(BundleKey)}
	 */
	public static TaskFactory<? extends LocalizeBundleWorkerTaskOutput> createBundleLocalizeWorkerTask(
			BundleKey bundlekey) throws NullPointerException {
		Objects.requireNonNull(bundlekey, "bundle key");
		return new BundleKeyLocalizingWorkerTaskFactory(bundlekey);
	}
}
