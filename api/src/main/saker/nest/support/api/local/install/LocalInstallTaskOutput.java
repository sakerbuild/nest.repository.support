package saker.nest.support.api.local.install;

import saker.build.task.utils.StructuredListTaskResult;
import saker.nest.bundle.storage.LocalBundleStorageView;

/**
 * Interface representing the result of a bundle installation task to a local bundle storage.
 * <p>
 * The interface provide access to each installation result in the form of a structured task result, as the
 * installoperations are split up into subtasks.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see LocalBundleStorageView
 * @see LocalInstallWorkerTaskOutput
 */
public interface LocalInstallTaskOutput {
	//LocalInstallWorkerTaskOutput
	/**
	 * Gets the structured task result of the install worker tasks.
	 * <p>
	 * Each element in the result is an instance of {@link LocalInstallWorkerTaskOutput}.
	 * 
	 * @return The install results.
	 */
	public StructuredListTaskResult getInstallResults();
}
