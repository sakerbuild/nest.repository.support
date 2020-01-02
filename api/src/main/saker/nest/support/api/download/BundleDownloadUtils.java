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

import java.util.Objects;

import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.support.impl.download.BundleDownloadWorkerTaskIdentifierImpl;
import saker.nest.support.impl.download.BundleKeyDownloadingWorkerTaskFactory;

/**
 * Utility class for working with bundle download functionality.
 */
public final class BundleDownloadUtils {
	private BundleDownloadUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a task identifier that can be used to start a bundle download task.
	 * <p>
	 * The task identifer should be used with the {@link #createBundleDownloadWorkerTask(BundleKey)} task.
	 * 
	 * @param bundlekey
	 *            The bundle key of the bundle that is being downloaded.
	 * @return The task identifier.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public static TaskIdentifier createBundleDownloadWorkerTaskIdentifier(BundleKey bundlekey)
			throws NullPointerException {
		Objects.requireNonNull(bundlekey, "bundle key");
		return new BundleDownloadWorkerTaskIdentifierImpl(bundlekey);
	}

	/**
	 * Creates a build task that downloads the specified bundle.
	 * 
	 * @param bundlekey
	 *            The bundle key of the bundle that is being downloaded.
	 * @return The bundle downloading task.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see {@link #createBundleDownloadWorkerTaskIdentifier(BundleKey)}
	 */
	public static TaskFactory<? extends DownloadBundleWorkerTaskOutput> createBundleDownloadWorkerTask(
			BundleKey bundlekey) throws NullPointerException {
		Objects.requireNonNull(bundlekey, "bundle key");
		return new BundleKeyDownloadingWorkerTaskFactory(bundlekey);
	}
}
