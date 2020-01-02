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
package saker.nest.support.impl.localize;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredListTaskResult;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.StructuredListTaskResult;
import saker.build.task.utils.StructuredTaskResult;
import saker.nest.support.api.localize.LocalizeBundleTaskOutput;

public class LocalizeBundleTaskOutputImpl implements LocalizeBundleTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private StructuredListTaskResult localizeResults;

	private transient StructuredListTaskResult bundlePaths;

	/**
	 * For {@link Externalizable}.
	 */
	public LocalizeBundleTaskOutputImpl() {
	}

	public LocalizeBundleTaskOutputImpl(List<? extends TaskIdentifier> bundlelocalizeworkertaskids) {
		List<StructuredTaskResult> bundlepaths = new ArrayList<>();
		List<StructuredTaskResult> localizeresults = new ArrayList<>();
		for (TaskIdentifier dltaskid : bundlelocalizeworkertaskids) {
			localizeresults.add(new SimpleStructuredObjectTaskResult(dltaskid));
			bundlepaths.add(new LocalizeWorkerTaskLocalPathStructuredTaskResult(dltaskid));
		}

		this.localizeResults = new SimpleStructuredListTaskResult(localizeresults);
		this.bundlePaths = new SimpleStructuredListTaskResult(bundlepaths);
	}

	@Override
	public StructuredListTaskResult getBundleLocalPaths() {
		return bundlePaths;
	}

	@Override
	public StructuredListTaskResult getLocalizeResults() {
		return localizeResults;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(localizeResults);
		out.writeObject(bundlePaths);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		localizeResults = (StructuredListTaskResult) in.readObject();
		bundlePaths = (StructuredListTaskResult) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((localizeResults == null) ? 0 : localizeResults.hashCode());
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
		LocalizeBundleTaskOutputImpl other = (LocalizeBundleTaskOutputImpl) obj;
		if (localizeResults == null) {
			if (other.localizeResults != null)
				return false;
		} else if (!localizeResults.equals(other.localizeResults))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + localizeResults + "]";
	}

}
