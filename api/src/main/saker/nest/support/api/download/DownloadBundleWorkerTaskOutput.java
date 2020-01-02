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