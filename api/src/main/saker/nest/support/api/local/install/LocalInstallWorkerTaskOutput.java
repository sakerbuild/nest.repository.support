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

import saker.build.thirdparty.saker.util.StringUtils;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.NestRepositoryBundle;
import saker.nest.bundle.storage.LocalBundleStorageView;

/**
 * Interface representing the results of a single bundle installation.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see LocalBundleStorageView
 * @see LocalInstallTaskOutput
 */
public interface LocalInstallWorkerTaskOutput {
	/**
	 * Gets the identifier of the installed bundle.
	 * 
	 * @return The identifier.
	 */
	public BundleIdentifier getBundleIdentifier();

	//hexa encoded
	/**
	 * Gets the hash of the bundle contents that was installed.
	 * <p>
	 * The hash code is computed using the same algorithm as {@link NestRepositoryBundle#getHash()}.
	 * 
	 * @return The bundle hash in a hexa encoded format.
	 * @see StringUtils#toHexString(byte[])
	 * @see StringUtils#parseHexString(CharSequence)
	 */
	public String getBundleHash();

}
