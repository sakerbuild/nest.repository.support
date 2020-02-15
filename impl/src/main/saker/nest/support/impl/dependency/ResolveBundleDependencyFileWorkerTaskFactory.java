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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.TaskContext;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.nest.bundle.BundleDependency;
import saker.nest.bundle.BundleDependencyInformation;
import saker.nest.bundle.BundleDependencyList;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.DependencyConstraintConfiguration;
import saker.nest.support.api.dependency.DependencyResolutionTaskOutput;
import saker.nest.support.api.dependency.filter.DependencyFilter;
import saker.nest.support.main.dependency.ResolveBundleDependencyTaskFactory;
import saker.nest.version.ExactVersionRange;
import saker.nest.version.MinimumVersionRange;
import saker.nest.version.VersionRange;

public class ResolveBundleDependencyFileWorkerTaskFactory extends ResolveDependencyWorkerTaskFactoryBase
		implements TaskIdentifier {
	private static final long serialVersionUID = 1L;

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
		super(filter, constraints);
		this.dependencyFilePath = dependencyFilePath;
		this.bundleIds = ObjectUtils.isNullOrEmpty(bundleIds) ? Collections.emptySet()
				: ImmutableUtils.makeImmutableLinkedHashSet(bundleIds);
		this.thisBundleId = thisBundleId;
	}

	@Override
	public DependencyResolutionTaskOutput run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
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

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(dependencyFilePath);
		SerialUtils.writeExternalCollection(out, bundleIds);
		out.writeObject(thisBundleId);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		dependencyFilePath = (SakerPath) in.readObject();
		bundleIds = SerialUtils.readExternalImmutableLinkedHashSet(in);
		thisBundleId = (BundleIdentifier) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((bundleIds == null) ? 0 : bundleIds.hashCode());
		result = prime * result + ((dependencyFilePath == null) ? 0 : dependencyFilePath.hashCode());
		result = prime * result + ((thisBundleId == null) ? 0 : thisBundleId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResolveBundleDependencyFileWorkerTaskFactory other = (ResolveBundleDependencyFileWorkerTaskFactory) obj;
		if (!ObjectUtils.iterablesOrderedEquals(this.bundleIds, other.bundleIds)) {
			return false;
		}
		if (dependencyFilePath == null) {
			if (other.dependencyFilePath != null)
				return false;
		} else if (!dependencyFilePath.equals(other.dependencyFilePath))
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

}