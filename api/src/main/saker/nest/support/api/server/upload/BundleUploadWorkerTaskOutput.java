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

import saker.build.thirdparty.saker.util.StringUtils;
import saker.nest.bundle.BundleIdentifier;

/**
 * Represents the result of a bundle uploading worker task.
 * <p>
 * The interface provides access to information about the uploaded bundle.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see BundleUploadTaskOutput
 */
public interface BundleUploadWorkerTaskOutput {
	/**
	 * Gets the identifier of the uploaded bundle.
	 * 
	 * @return The identifier.
	 */
	public BundleIdentifier getBundleIdentifier();

	//hexa encoded
	/**
	 * Gets the <code>MD5</code> hash of the bundle contents that was uploaded.
	 * 
	 * @return The <code>MD5</code> hash in a hexa encoded format.
	 * @see StringUtils#toHexString(byte[])
	 * @see StringUtils#parseHexString(CharSequence)
	 */
	public String getMD5();

	//hexa encoded
	/**
	 * Gets the <code>SHA256</code> hash of the bundle contents that was uploaded.
	 * 
	 * @return The <code>SHA256</code> hash in a hexa encoded format.
	 * @see StringUtils#toHexString(byte[])
	 * @see StringUtils#parseHexString(CharSequence)
	 */
	public String getSHA256();
}
