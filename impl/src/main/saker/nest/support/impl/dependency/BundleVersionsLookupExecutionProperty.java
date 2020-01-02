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
package saker.nest.support.impl.dependency;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;
import java.util.function.Supplier;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.lookup.BundleLookup;
import saker.nest.bundle.lookup.BundleVersionLookupResult;
import saker.nest.bundle.lookup.LookupKey;

public class BundleVersionsLookupExecutionProperty
		implements ExecutionProperty<BundleVersionsLookupExecutionProperty.PropertyLookupResult>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static class PropertyLookupResult implements Externalizable {
		private static final long serialVersionUID = 1L;

		private transient BundleVersionLookupResult lookupResult;

		private Set<? extends BundleIdentifier> bundles;
		private LookupKey lookupKey;

		/**
		 * For {@link Externalizable}.
		 */
		public PropertyLookupResult() {
		}

		public PropertyLookupResult(BundleVersionLookupResult lookupResult) {
			this.lookupResult = lookupResult;
			this.bundles = lookupResult.getBundles();
			this.lookupKey = lookupResult.getRelativeLookup().getLookupKey();
		}

		public BundleVersionLookupResult getLookupResult() {
			return lookupResult;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bundles == null) ? 0 : bundles.hashCode());
			result = prime * result + ((lookupKey == null) ? 0 : lookupKey.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PropertyLookupResult other = (PropertyLookupResult) obj;
			if (bundles == null) {
				if (other.bundles != null)
					return false;
			} else if (!bundles.equals(other.bundles))
				return false;
			if (lookupKey == null) {
				if (other.lookupKey != null)
					return false;
			} else if (!lookupKey.equals(other.lookupKey))
				return false;
			return true;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			SerialUtils.writeExternalCollection(out, bundles);
			out.writeObject(lookupKey);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			bundles = SerialUtils.readExternalImmutableLinkedHashSet(in);
			lookupKey = (LookupKey) in.readObject();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + bundles;
		}

	}

	private transient Supplier<BundleLookup> lookup;
	private BundleIdentifier bundleIdentifier;
	private LookupKey lookupKey;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleVersionsLookupExecutionProperty() {
	}

	public BundleVersionsLookupExecutionProperty(BundleLookup lookup, BundleIdentifier bundleIdentifier) {
		this.lookup = Functionals.valSupplier(lookup);
		this.bundleIdentifier = bundleIdentifier;
		this.lookupKey = lookup.getLookupKey();
	}

	@Override
	public PropertyLookupResult getCurrentValue(ExecutionContext executioncontext) {
		BundleLookup lookup = this.lookup.get();
		if (lookup == null) {
			return null;
		}
		BundleVersionLookupResult lookupresult = lookup.lookupBundleVersions(bundleIdentifier);
		if (lookupresult == null) {
			return null;
		}
		return new PropertyLookupResult(lookupresult);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(bundleIdentifier);
		out.writeObject(lookupKey);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundleIdentifier = (BundleIdentifier) in.readObject();
		lookupKey = (LookupKey) in.readObject();
		lookup = LazySupplier.of(() -> ((NestBundleClassLoader) this.getClass().getClassLoader())
				.getBundleStorageConfiguration().getBundleLookupForKey(lookupKey));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleIdentifier == null) ? 0 : bundleIdentifier.hashCode());
		result = prime * result + ((lookupKey == null) ? 0 : lookupKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BundleVersionsLookupExecutionProperty other = (BundleVersionsLookupExecutionProperty) obj;
		if (bundleIdentifier == null) {
			if (other.bundleIdentifier != null)
				return false;
		} else if (!bundleIdentifier.equals(other.bundleIdentifier))
			return false;
		if (lookupKey == null) {
			if (other.lookupKey != null)
				return false;
		} else if (!lookupKey.equals(other.lookupKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (bundleIdentifier != null ? "bundleIdentifier=" + bundleIdentifier + ", " : "")
				+ (lookupKey != null ? "lookupKey=" + lookupKey : "") + "]";
	}

}
