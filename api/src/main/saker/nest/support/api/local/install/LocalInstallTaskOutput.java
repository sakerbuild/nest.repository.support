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
