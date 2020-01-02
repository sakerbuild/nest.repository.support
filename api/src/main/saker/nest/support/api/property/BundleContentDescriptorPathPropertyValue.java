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
package saker.nest.support.api.property;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.NestRepositoryBundle;

/**
 * Data interface providing access to the local file system path and associated contents descriptor of a bundle.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * An instance is created by the {@link BundlePropertyUtils#bundleContentDescriptorPathExecutionProperty(BundleKey)}
 * execution property.
 */
public interface BundleContentDescriptorPathPropertyValue {
	/**
	 * Gets the content descriptor of the associated bundle.
	 * <p>
	 * The descriptor doesn't include the dependencies, and is usually based on the
	 * {@linkplain NestRepositoryBundle#getHash() hash} of the contents of the bundle.
	 * 
	 * @return The content descriptor.
	 */
	public ContentDescriptor getContentDescriptor();

	/**
	 * Gets the local file system path to the bundle.
	 * 
	 * @return The local path.
	 */
	public SakerPath getLocalPath();
}
