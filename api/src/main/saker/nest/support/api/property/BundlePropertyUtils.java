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

import java.util.Objects;

import saker.build.runtime.execution.ExecutionProperty;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleInformation;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.NestBundleStorageConfiguration;
import saker.nest.bundle.NestRepositoryBundle;
import saker.nest.bundle.lookup.BundleLookup;
import saker.nest.bundle.storage.BundleStorageView;
import saker.nest.bundle.storage.StorageViewKey;
import saker.nest.support.impl.util.BundleDocumentationAttachmentExecutionProperty;
import saker.nest.support.impl.util.BundleIdentifierBundleKeyExecutionProperty;
import saker.nest.support.impl.util.BundleKeyContentDescriptorExecutionProperty;
import saker.nest.support.impl.util.BundleKeyContentDescriptorPathExecutionProperty;
import saker.nest.support.impl.util.BundleSouceAttachmentExecutionProperty;

/**
 * Utility class for working with properties related to saker.nest bundles.
 */
public final class BundlePropertyUtils {
	private BundlePropertyUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets an execution property that looks up the {@link BundleKey} for a {@link BundleIdentifier}.
	 * <p>
	 * The execution property will find the bundle for the specified bundle identifier. It uses the
	 * {@linkplain NestBundleStorageConfiguration#getBundleLookup() root bundle lookup} to find the bundle.
	 * <p>
	 * The lookup has the same semantics as {@link BundleLookup#lookupBundleInformation(BundleIdentifier)}.
	 * 
	 * @param bundleid
	 *            The identifier of the bundle.
	 * @return The execution property.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public static ExecutionProperty<? extends BundleKey> lookupBundleIdentifierToBundleKeyExecutionProperty(
			BundleIdentifier bundleid) throws NullPointerException {
		Objects.requireNonNull(bundleid, "bundle identifier");
		return new BundleIdentifierBundleKeyExecutionProperty(bundleid);
	}

	/**
	 * Gets an execution property that returns the {@linkplain BundleInformation#getSourceAttachmentBundleIdentifier()
	 * source attachment} bundle identifier for a given bundle.
	 * <p>
	 * The execution property will retrieve the bundle information using
	 * {@link BundleStorageView#getBundleInformation(BundleIdentifier)} from the associated storage view, and get the
	 * source attachment bundle identifier from it.
	 * 
	 * @param bundle
	 *            The bundle key for the bundle.
	 * @return The execution property.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see NestBundleStorageConfiguration#getBundleStorageViewForKey(StorageViewKey)
	 */
	public static ExecutionProperty<? extends BundleIdentifier> sourceAttachmentExecutionProperty(BundleKey bundle)
			throws NullPointerException {
		Objects.requireNonNull(bundle, "bundle key");
		return new BundleSouceAttachmentExecutionProperty(bundle);
	}

	/**
	 * Gets an execution property that returns the
	 * {@linkplain BundleInformation#getDocumentationAttachmentBundleIdentifier() documentation attachment} bundle
	 * identifier for a given bundle.
	 * <p>
	 * The execution property will retrieve the bundle information using
	 * {@link BundleStorageView#getBundleInformation(BundleIdentifier)} from the associated storage view, and get the
	 * documentation attachment bundle identifier from it.
	 * 
	 * @param bundle
	 *            The bundle key for the bundle.
	 * @return The execution property.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see NestBundleStorageConfiguration#getBundleStorageViewForKey(StorageViewKey)
	 */
	public static ExecutionProperty<? extends BundleIdentifier> documentationAttachmentExecutionProperty(
			BundleKey bundle) throws NullPointerException {
		Objects.requireNonNull(bundle, "bundle key");
		return new BundleDocumentationAttachmentExecutionProperty(bundle);
	}

	/**
	 * Gets an execution property that returns the contents descriptor and {@linkplain NestRepositoryBundle bundle
	 * reference} for a bundle key.
	 * <p>
	 * The execution property will load the specified bundle and return an object that provides access to the content
	 * descriptor and {@link NestRepositoryBundle} instance for the bundle.
	 * 
	 * @param bundlekey
	 *            The bundle key.
	 * @return The execution property.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public static ExecutionProperty<? extends BundleContentDescriptorPropertyValue> bundleContentDescriptorExecutionProperty(
			BundleKey bundlekey) throws NullPointerException {
		Objects.requireNonNull(bundlekey, "bundle key");
		return new BundleKeyContentDescriptorExecutionProperty(bundlekey);
	}

	/**
	 * Gets an execution property that returns the contents descriptor and the local file system path of the bundle for
	 * a bundle key.
	 * <p>
	 * Computing the value of the execution property will cause the bundle to be localized if it is not present in the
	 * local file system. (E.g. available from a server)
	 * 
	 * @param bundlekey
	 *            The bundle key.
	 * @return The execution property.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public static ExecutionProperty<? extends BundleContentDescriptorPathPropertyValue> bundleContentDescriptorPathExecutionProperty(
			BundleKey bundlekey) throws NullPointerException {
		Objects.requireNonNull(bundlekey, "bundle key");
		return new BundleKeyContentDescriptorPathExecutionProperty(bundlekey);
	}
}
