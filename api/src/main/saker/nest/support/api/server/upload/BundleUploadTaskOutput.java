package saker.nest.support.api.server.upload;

import saker.build.task.utils.StructuredListTaskResult;
import saker.nest.bundle.storage.ServerBundleStorageView;
import saker.nest.bundle.storage.ServerStorageUtils;

/**
 * Represents the output of a bundle upload build task.
 * <p>
 * The interface provide access to each upload result in the form of a structured task result, as the upload operations
 * are split up into subtasks.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see ServerBundleStorageView
 * @see ServerStorageUtils
 * @see BundleUploadWorkerTaskOutput
 */
public interface BundleUploadTaskOutput {
	//BundleUploadWorkerTaskOutput
	/**
	 * Gets the structured task result of the upload worker tasks.
	 * <p>
	 * Each element in the result is an instance of {@link BundleUploadWorkerTaskOutput}.
	 * 
	 * @return The upload results.
	 */
	public StructuredListTaskResult getUploadResults();
}
