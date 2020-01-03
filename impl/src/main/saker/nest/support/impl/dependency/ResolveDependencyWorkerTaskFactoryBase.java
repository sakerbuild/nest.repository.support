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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.nest.bundle.BundleDependencyInformation;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleInformation;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.DependencyConstraintConfiguration;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.NestBundleStorageConfiguration;
import saker.nest.bundle.lookup.BundleLookup;
import saker.nest.bundle.lookup.BundleVersionLookupResult;
import saker.nest.bundle.storage.BundleStorageView;
import saker.nest.bundle.storage.StorageViewKey;
import saker.nest.dependency.DependencyResolutionResult;
import saker.nest.dependency.DependencyUtils;
import saker.nest.exc.BundleDependencyUnsatisfiedException;
import saker.nest.exc.BundleLoadingFailedException;
import saker.nest.support.api.dependency.DependencyResolutionTaskOutput;
import saker.nest.support.api.dependency.filter.DependencyFilter;
import saker.nest.support.impl.dependency.BundleVersionsLookupExecutionProperty.PropertyLookupResult;
import saker.nest.support.impl.dependency.filter.ConstraintDependencyFilter;

abstract class ResolveDependencyWorkerTaskFactoryBase
		implements TaskFactory<DependencyResolutionTaskOutput>, Task<DependencyResolutionTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	protected DependencyFilter filter;
	protected DependencyConstraintConfiguration constraints;

	/**
	 * For {@link Externalizable}.
	 */
	public ResolveDependencyWorkerTaskFactoryBase() {
	}

	public ResolveDependencyWorkerTaskFactoryBase(DependencyFilter filter,
			DependencyConstraintConfiguration constraints) {
		this.filter = filter;
		this.constraints = constraints;
	}

	@Override
	public Task<? extends DependencyResolutionTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	protected DependencyResolutionTaskOutput runWorkerTaskForDependencyInformation(TaskContext taskcontext,
			BundleIdentifier rootbundleid, BundleDependencyInformation depinfo) {
		NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
		NestBundleStorageConfiguration storageconfig = cl.getBundleStorageConfiguration();
		BundleLookup bundlelookup = storageconfig.getBundleLookup();
		BundleKey rootbundlekey = BundleKey.create(null, rootbundleid);

		List<Throwable> unsatisfiedsuppressions = new ArrayList<>();

		DependencyResolutionResult<BundleKey, DependencyResolutionBundleContext> resolutionresult = DependencyUtils
				.<BundleKey, DependencyResolutionBundleContext>satisfyDependencyRequirements(rootbundlekey, null,
						depinfo,
						new BiFunction<BundleIdentifier, DependencyResolutionBundleContext, Iterable<? extends Entry<? extends BundleKey, ? extends DependencyResolutionBundleContext>>>() {
							private boolean rootReported = false;

							@Override
							public Iterable<? extends Entry<? extends BundleKey, ? extends DependencyResolutionBundleContext>> apply(
									BundleIdentifier bi, DependencyResolutionBundleContext bc) {
								BundleLookup lookuptouse;
								if (bc == null) {
									if (!rootReported) {
										taskcontext.reportExecutionDependency(
												RootBundleLookupKeyExecutionProperty.INSTANCE,
												bundlelookup.getLookupKey());
										rootReported = true;
									}
									lookuptouse = bundlelookup;
								} else {
									lookuptouse = bc.getRelativeLookup();
								}
								PropertyLookupResult propertylookup = taskcontext.getTaskUtilities()
										.getReportExecutionDependency(
												new BundleVersionsLookupExecutionProperty(lookuptouse, bi));
								if (propertylookup == null) {
									return null;
								}
								BundleVersionLookupResult lookupresult = propertylookup.getLookupResult();

								if (lookupresult == null) {
									return null;
								}
								return ObjectUtils.singleValueMap(toBundleKeySet(lookupresult),
										new DependencyResolutionBundleContext(lookupresult)).entrySet();
							}
						}, (bk, bc) -> {
							try {
								BundleStorageView storageview = bc.getStorageView();
								BundleIdentifier bundleid = bk.getBundleIdentifier();
								BundleInformation lookupbundleinfo = storageview.getBundleInformation(bundleid);
								taskcontext.reportExecutionDependency(
										new BundleInformationExecutionProperty(storageview, bundleid),
										lookupbundleinfo);

								DependencyConstraintConfiguration constraints = this.constraints;
								if (DependencyUtils.isDependencyConstraintClassPathExcludes(constraints,
										lookupbundleinfo)) {
									//XXX log somewhere?
									return null;
								}
								BundleDependencyInformation lookupbundledepinfo = lookupbundleinfo
										.getDependencyInformation();
								return filterBundleDependencyInformation(bk, constraints, this.filter,
										lookupbundledepinfo);
							} catch (BundleLoadingFailedException e) {
								unsatisfiedsuppressions.add(e);
							}
							return null;
						}, null);
		if (resolutionresult == null) {
			//XXX more information
			BundleDependencyUnsatisfiedException unsatisfiedexc = new BundleDependencyUnsatisfiedException(
					"Failed to satisfy dependencies.");
			unsatisfiedsuppressions.forEach(unsatisfiedexc::addSuppressed);
			taskcontext.abortExecution(unsatisfiedexc);
			return null;
		}
		Set<BundleKey> bundleresolutions = new LinkedHashSet<>();
		for (Entry<Entry<? extends BundleIdentifier, ? extends DependencyResolutionBundleContext>, Entry<? extends BundleKey, ? extends DependencyResolutionBundleContext>> entry : resolutionresult
				.getResultInDeclarationOrder().entrySet()) {
			BundleKey resolutionbundlekey = entry.getValue().getKey();
			if (resolutionbundlekey.equals(rootbundlekey)) {
				//don't include the root container bundle in the result
				continue;
			}
			bundleresolutions.add(resolutionbundlekey);
		}
		DependencyResolutionTaskOutputImpl result = new DependencyResolutionTaskOutputImpl(bundleresolutions);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	protected static BundleDependencyInformation filterBundleDependencyInformation(BundleKey bk,
			DependencyConstraintConfiguration constraints, DependencyFilter filter,
			BundleDependencyInformation depinfo) {
		BundleDependencyInformation constraintfiltered = ConstraintDependencyFilter
				.filterBundleDependencyInformation(depinfo, constraints);
		if (constraintfiltered == null) {
			return constraintfiltered;
		}
		return filter.filterBundleDependency(bk, constraintfiltered);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(filter);
		out.writeObject(constraints);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		filter = (DependencyFilter) in.readObject();
		constraints = (DependencyConstraintConfiguration) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((constraints == null) ? 0 : constraints.hashCode());
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
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
		ResolveDependencyWorkerTaskFactoryBase other = (ResolveDependencyWorkerTaskFactoryBase) obj;
		if (constraints == null) {
			if (other.constraints != null)
				return false;
		} else if (!constraints.equals(other.constraints))
			return false;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		return true;
	}

	protected static Set<BundleKey> toBundleKeySet(BundleVersionLookupResult lookedupversions) {
		//keep order, version descending
		Set<BundleKey> result = new LinkedHashSet<>();
		StorageViewKey storagekey = lookedupversions.getStorageView().getStorageViewKey();
		for (BundleIdentifier bid : lookedupversions.getBundles()) {
			result.add(BundleKey.create(storagekey, bid));
		}
		return result;
	}

	protected static BundleIdentifier generateRootBundleId() {
		return DependencyUtils.randomBundleIdentifier();
	}

	private static class DependencyResolutionBundleContext {
		private BundleStorageView storageView;
		private BundleLookup relativeLookup;

		public DependencyResolutionBundleContext(BundleVersionLookupResult lookupresult) {
			this.storageView = lookupresult.getStorageView();
			this.relativeLookup = lookupresult.getRelativeLookup();
		}

		public BundleStorageView getStorageView() {
			return storageView;
		}

		public BundleLookup getRelativeLookup() {
			return relativeLookup;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((relativeLookup == null) ? 0 : relativeLookup.hashCode());
			result = prime * result + ((storageView == null) ? 0 : storageView.hashCode());
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
			DependencyResolutionBundleContext other = (DependencyResolutionBundleContext) obj;
			if (relativeLookup == null) {
				if (other.relativeLookup != null)
					return false;
			} else if (!relativeLookup.equals(other.relativeLookup))
				return false;
			if (storageView == null) {
				if (other.storageView != null)
					return false;
			} else if (!storageView.equals(other.storageView))
				return false;
			return true;
		}
	}
}