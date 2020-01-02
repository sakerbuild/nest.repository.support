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
package saker.nest.support.api.download;

import saker.build.file.path.SakerPath;
import saker.build.task.utils.StructuredListTaskResult;

/**
 * Interface representing the output of the bundle download task.
 * <p>
 * The interface provides access to the paths and download results of the requested bundles. The results are available
 * through strucuted task results, as the bundle downloadings are delegated to worker tasks.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see DownloadBundleWorkerTaskOutput
 */
public interface DownloadBundleTaskOutput {
	//element SakerPath
	/**
	 * Gets the structured task result of the result bundle execution paths.
	 * <p>
	 * The elements of the result are instance of {@link SakerPath} that represent the absolute build execution file
	 * paths of the downloaded bundles.
	 * 
	 * @return The bundle paths.
	 */
	public StructuredListTaskResult getBundlePaths();

	//element DownloadBundleWorkerTaskOutput
	/**
	 * Gets the download worker task results of each bundle download request.
	 * <p>
	 * The result contains elements that are instances of {@link DownloadBundleWorkerTaskOutput}
	 * 
	 * @return The download results.
	 */
	public StructuredListTaskResult getDownloadResults();
}
