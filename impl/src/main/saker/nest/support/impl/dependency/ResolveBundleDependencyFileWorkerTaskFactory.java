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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.nest.bundle.BundleDependency;
import saker.nest.bundle.BundleDependencyInformation;
import saker.nest.bundle.BundleDependencyList;
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
import saker.nest.dependency.DependencyDomainResolutionResult;
import saker.nest.dependency.DependencyUtils;
import saker.nest.exc.BundleDependencyUnsatisfiedException;
import saker.nest.exc.BundleLoadingFailedException;
import saker.nest.support.api.dependency.DependencyResolutionTaskOutput;
import saker.nest.support.api.dependency.filter.DependencyFilter;
import saker.nest.support.impl.dependency.filter.ConstraintDependencyFilter;
import saker.nest.support.impl.util.BundleVersionsLookupExecutionProperty;
import saker.nest.support.impl.util.BundleVersionsLookupExecutionProperty.PropertyLookupResult;
import saker.nest.support.main.dependency.ResolveBundleDependencyTaskFactory;
import saker.nest.version.ExactVersionRange;
import saker.nest.version.MinimumVersionRange;
import saker.nest.version.VersionRange;

public class ResolveBundleDependencyFileWorkerTaskFactory implements TaskFactory<DependencyResolutionTaskOutput>,
		Task<DependencyResolutionTaskOutput>, Externalizable, TaskIdentifier {
	private static final long serialVersionUID = 1L;

	protected DependencyFilter filter;
	protected DependencyConstraintConfiguration constraints;

	protected Set<BundleIdentifier> bundleIds;
	protected SakerPath dependencyFilePath;
	protected BundleIdentifier thisBundleId;

	/**
	 * For {@link Externalizable}.
	 */
	public ResolveBundleDependencyFileWorkerTaskFactory() {
	}

	public ResolveBundleDependencyFileWorkerTaskFactory(DependencyFilter filter,
			DependencyConstraintConfiguration constraints, Set<BundleIdentifier> bundleIds,
			SakerPath dependencyFilePath, BundleIdentifier thisBundleId) {
		this.filter = filter;
		this.constraints = constraints;
		this.dependencyFilePath = dependencyFilePath;
		this.bundleIds = ObjectUtils.isNullOrEmpty(bundleIds) ? Collections.emptySet()
				: ImmutableUtils.makeImmutableLinkedHashSet(bundleIds);
		this.thisBundleId = thisBundleId;
	}

	@Override
	public Task<? extends DependencyResolutionTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public DependencyResolutionTaskOutput run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
			if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_009) {
				Map<String, Object> valmap = new LinkedHashMap<>();
				if (!ObjectUtils.isNullOrEmpty(bundleIds)) {
					valmap.put("Bundles", bundleIds.stream().map(Object::toString).toArray());
				}
				if (dependencyFilePath != null) {
					valmap.put("Dependency file", dependencyFilePath.toString());
				}
				if (constraints != null) {
					Map<String, Object> constraintvals = new LinkedHashMap<>();
					String bsver = constraints.getBuildSystemVersion();
					Integer jremajor = constraints.getJreMajorVersion();
					String natarch = constraints.getNativeArchitecture();
					String repover = constraints.getRepositoryVersion();
					if (jremajor != null) {
						constraintvals.put("JRE major version", jremajor);
					}
					if (natarch != null) {
						constraintvals.put("Native architecture", natarch);
					}
					if (bsver != null) {
						constraintvals.put("Saker.build version", bsver);
					}
					if (repover != null) {
						constraintvals.put("Saker.nest version", repover);
					}
					if (!constraintvals.isEmpty()) {
						valmap.put("Dependency constraints", constraintvals);
					}
				}
				BuildTrace.setValues(valmap, BuildTrace.VALUE_CATEGORY_TASK);
			}
		}
		taskcontext.setStandardOutDisplayIdentifier(ResolveBundleDependencyTaskFactory.TASK_NAME);

		BundleDependencyInformation bundledependencyinfo = BundleDependencyInformation.EMPTY;
		if (dependencyFilePath != null) {
			SakerFile depfile = taskcontext.getTaskUtilities().resolveFileAtPath(dependencyFilePath);
			if (depfile == null) {
				taskcontext.reportInputFileDependency(null, dependencyFilePath,
						CommonTaskContentDescriptors.IS_NOT_FILE);
				taskcontext
						.abortExecution(new FileNotFoundException("Dependency file not found: " + dependencyFilePath));
				return null;
			}
			taskcontext.getTaskUtilities().reportInputFileDependency(null, depfile);

			bundledependencyinfo = taskcontext.computeFileContentData(depfile,
					new BundleDependencyInformationFileDataComputer(thisBundleId));
			bundledependencyinfo = filterBundleDependencyInformation(null, constraints, filter, bundledependencyinfo);
		}
		if (!ObjectUtils.isNullOrEmpty(bundleIds)) {
			Map<BundleIdentifier, BundleDependencyList> deps = new LinkedHashMap<>();
			for (BundleIdentifier bid : bundleIds) {
				String versionnum = bid.getVersionNumber();
				VersionRange deprange;
				if (versionnum == null) {
					deprange = MinimumVersionRange.ANY_VERSION_RANGE_INSTANCE;
				} else {
					deprange = ExactVersionRange.create(versionnum);
				}
				deps.put(bid.withoutMetaQualifiers(), BundleDependencyList.create(
						Collections.singleton(BundleDependency.builder().addKind("dep").setRange(deprange).build())));
			}
			for (Entry<BundleIdentifier, ? extends BundleDependencyList> entry : bundledependencyinfo.getDependencies()
					.entrySet()) {
				deps.putIfAbsent(entry.getKey(), entry.getValue());
			}
			bundledependencyinfo = BundleDependencyInformation.create(deps);
		}
		if (thisBundleId != null) {
			BundleIdentifier thisbundleidwithoutmeta = thisBundleId.withoutMetaQualifiers();
			if (bundledependencyinfo.getDependencyList(thisbundleidwithoutmeta) != null) {
				taskcontext.abortExecution(new IllegalArgumentException(
						"Self bundle dependency declaration found: " + thisbundleidwithoutmeta));
				return null;
			}
		}
		BundleIdentifier rootbundleid = generateRootBundleId();
		return runWorkerTaskForDependencyInformation(taskcontext, rootbundleid, bundledependencyinfo);
	}

	protected DependencyResolutionTaskOutput runWorkerTaskForDependencyInformation(TaskContext taskcontext,
			BundleIdentifier rootbundleid, BundleDependencyInformation depinfo) {
		NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
		NestBundleStorageConfiguration storageconfig = cl.getBundleStorageConfiguration();
		BundleLookup bundlelookup = storageconfig.getBundleLookup();
		BundleKey rootbundlekey = BundleKey.create(null, rootbundleid);

		return executeDependencyResolution(taskcontext, depinfo, bundlelookup, rootbundlekey);
	}

	private DependencyResolutionTaskOutput executeDependencyResolution(TaskContext taskcontext,
			BundleDependencyInformation depinfo, BundleLookup bundlelookup, BundleKey rootbundlekey) {
		List<Throwable> unsatisfiedsuppressions = new ArrayList<>();

		BiFunction<BundleIdentifier, DependencyResolutionBundleContext, Iterable<? extends Entry<? extends BundleKey, ? extends DependencyResolutionBundleContext>>> bundleslookupfunction = new BiFunction<BundleIdentifier, DependencyResolutionBundleContext, Iterable<? extends Entry<? extends BundleKey, ? extends DependencyResolutionBundleContext>>>() {
			private boolean rootReported = false;

			@Override
			public Iterable<? extends Entry<? extends BundleKey, ? extends DependencyResolutionBundleContext>> apply(
					BundleIdentifier bi, DependencyResolutionBundleContext bc) {
				BundleLookup lookuptouse;
				if (bc == null) {
					if (!rootReported) {
						taskcontext.reportExecutionDependency(RootBundleLookupKeyExecutionProperty.INSTANCE,
								bundlelookup.getLookupKey());
						rootReported = true;
					}
					lookuptouse = bundlelookup;
				} else {
					lookuptouse = bc.getRelativeLookup();
				}
				PropertyLookupResult propertylookup = taskcontext.getTaskUtilities()
						.getReportExecutionDependency(new BundleVersionsLookupExecutionProperty(lookuptouse, bi));
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
		};
		BiFunction<? super BundleKey, ? super DependencyResolutionBundleContext, ? extends BundleDependencyInformation> bundledependencieslookupfunction = (
				bk, bc) -> {
			try {
				BundleStorageView storageview = bc.getStorageView();
				BundleIdentifier bundleid = bk.getBundleIdentifier();
				BundleInformation lookupbundleinfo = storageview.getBundleInformation(bundleid);
				taskcontext.reportExecutionDependency(new BundleInformationExecutionProperty(storageview, bundleid),
						lookupbundleinfo);

				DependencyConstraintConfiguration constraints = this.constraints;
				if (DependencyUtils.isDependencyConstraintClassPathExcludes(constraints, lookupbundleinfo)) {
					//XXX log somewhere?
					return null;
				}
				BundleDependencyInformation lookupbundledepinfo = lookupbundleinfo.getDependencyInformation();
				return filterBundleDependencyInformation(bk, constraints, this.filter, lookupbundledepinfo);
			} catch (BundleLoadingFailedException e) {
				unsatisfiedsuppressions.add(e);
			}
			return null;
		};

		if (saker.nest.meta.Versions.VERSION_FULL_COMPOUND < 8_001) {
			//domain based dependency resolution is not yet available
			//use legacy
			return executeLegacyDependencyResolution(taskcontext, depinfo, rootbundlekey, bundleslookupfunction,
					bundledependencieslookupfunction, unsatisfiedsuppressions);
		}
		return executeDomainDependencyResolution(taskcontext, depinfo, rootbundlekey, bundleslookupfunction,
				bundledependencieslookupfunction, unsatisfiedsuppressions);
	}

	private static DependencyResolutionTaskOutput executeDomainDependencyResolution(TaskContext taskcontext,
			BundleDependencyInformation depinfo, BundleKey rootbundlekey,
			BiFunction<BundleIdentifier, DependencyResolutionBundleContext, Iterable<? extends Entry<? extends BundleKey, ? extends DependencyResolutionBundleContext>>> bundleslookupfunction,
			BiFunction<? super BundleKey, ? super DependencyResolutionBundleContext, ? extends BundleDependencyInformation> bundledependencieslookupfunction,
			List<Throwable> unsatisfiedsuppressions) {
		DependencyDomainResolutionResult<BundleKey, DependencyResolutionBundleContext> resolutionresult = DependencyUtils
				.<BundleKey, DependencyResolutionBundleContext>satisfyDependencyDomain(rootbundlekey, null, depinfo,
						bundleslookupfunction, bundledependencieslookupfunction, null);
		if (resolutionresult == null) {
			//XXX more information
			BundleDependencyUnsatisfiedException unsatisfiedexc = new BundleDependencyUnsatisfiedException(
					"Failed to satisfy dependencies.");
			unsatisfiedsuppressions.forEach(unsatisfiedexc::addSuppressed);
			taskcontext.abortExecution(unsatisfiedexc);
			return null;
		}
		Set<BundleKey> bundleresolutions = new LinkedHashSet<>();
		collectBundleResolutions(resolutionresult, bundleresolutions, new HashSet<>());
		//don't include the root container bundle in the result
		bundleresolutions.remove(rootbundlekey);
		DependencyResolutionTaskOutputImpl result = new DependencyResolutionTaskOutputImpl(bundleresolutions);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	private static void collectBundleResolutions(DependencyDomainResolutionResult<BundleKey, ?> domain,
			Set<BundleKey> result, Set<DependencyDomainResolutionResult<BundleKey, ?>> collected) {
		if (!collected.add(domain)) {
			return;
		}
		//add first and collect later for order
		for (Entry<? extends Entry<? extends BundleKey, ?>, ? extends DependencyDomainResolutionResult<BundleKey, ?>> entry : domain
				.getDirectDependencies().entrySet()) {
			result.add(entry.getKey().getKey());
		}
		for (Entry<? extends Entry<? extends BundleKey, ?>, ? extends DependencyDomainResolutionResult<BundleKey, ?>> entry : domain
				.getDirectDependencies().entrySet()) {
			collectBundleResolutions(entry.getValue(), result, collected);
		}
	}

	@SuppressWarnings("deprecation")
	private static DependencyResolutionTaskOutput executeLegacyDependencyResolution(TaskContext taskcontext,
			BundleDependencyInformation depinfo, BundleKey rootbundlekey,
			BiFunction<BundleIdentifier, DependencyResolutionBundleContext, Iterable<? extends Entry<? extends BundleKey, ? extends DependencyResolutionBundleContext>>> bundleslookupfunction,
			BiFunction<? super BundleKey, ? super DependencyResolutionBundleContext, ? extends BundleDependencyInformation> bundledependencieslookupfunction,
			List<Throwable> unsatisfiedsuppressions) {

		//use fully qualified name to avoid importing and having a warning there
		saker.nest.dependency.DependencyResolutionResult<BundleKey, DependencyResolutionBundleContext> resolutionresult = DependencyUtils
				.<BundleKey, DependencyResolutionBundleContext>satisfyDependencyRequirements(rootbundlekey, null,
						depinfo, bundleslookupfunction, bundledependencieslookupfunction, null);
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

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(filter);
		out.writeObject(constraints);
		out.writeObject(dependencyFilePath);
		SerialUtils.writeExternalCollection(out, bundleIds);
		out.writeObject(thisBundleId);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		filter = (DependencyFilter) in.readObject();
		constraints = (DependencyConstraintConfiguration) in.readObject();
		dependencyFilePath = (SakerPath) in.readObject();
		bundleIds = SerialUtils.readExternalImmutableLinkedHashSet(in);
		thisBundleId = (BundleIdentifier) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleIds == null) ? 0 : bundleIds.hashCode());
		result = prime * result + ((constraints == null) ? 0 : constraints.hashCode());
		result = prime * result + ((dependencyFilePath == null) ? 0 : dependencyFilePath.hashCode());
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + ((thisBundleId == null) ? 0 : thisBundleId.hashCode());
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
		ResolveBundleDependencyFileWorkerTaskFactory other = (ResolveBundleDependencyFileWorkerTaskFactory) obj;
		if (bundleIds == null) {
			if (other.bundleIds != null)
				return false;
		} else if (!bundleIds.equals(other.bundleIds))
			return false;
		if (constraints == null) {
			if (other.constraints != null)
				return false;
		} else if (!constraints.equals(other.constraints))
			return false;
		if (dependencyFilePath == null) {
			if (other.dependencyFilePath != null)
				return false;
		} else if (!dependencyFilePath.equals(other.dependencyFilePath))
			return false;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		if (thisBundleId == null) {
			if (other.thisBundleId != null)
				return false;
		} else if (!thisBundleId.equals(other.thisBundleId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResolveBundleDependencyFileWorkerTaskFactory["
				+ (bundleIds != null ? "bundleIds=" + bundleIds + ", " : "")
				+ (dependencyFilePath != null ? "dependencyFilePath=" + dependencyFilePath + ", " : "")
				+ (thisBundleId != null ? "thisBundleId=" + thisBundleId : "") + "]";
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