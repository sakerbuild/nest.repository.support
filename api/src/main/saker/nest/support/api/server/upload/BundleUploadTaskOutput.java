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
