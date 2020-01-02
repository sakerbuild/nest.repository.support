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
package saker.nest.support.impl.download;

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
import saker.nest.support.api.download.DownloadBundleTaskOutput;

public class DownloadBundleTaskOutputImpl implements DownloadBundleTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private StructuredListTaskResult downloadResults;

	private transient StructuredListTaskResult bundlePaths;

	/**
	 * For {@link Externalizable}.
	 */
	public DownloadBundleTaskOutputImpl() {
	}

	public DownloadBundleTaskOutputImpl(List<? extends TaskIdentifier> bundledlworkertaskids) {
		List<StructuredTaskResult> bundlepaths = new ArrayList<>();
		List<StructuredTaskResult> dlresults = new ArrayList<>();
		for (TaskIdentifier dltaskid : bundledlworkertaskids) {
			dlresults.add(new SimpleStructuredObjectTaskResult(dltaskid));
			bundlepaths.add(new DownloadWorkerTaskPathStructuredTaskResult(dltaskid));
		}

		this.downloadResults = new SimpleStructuredListTaskResult(dlresults);
		this.bundlePaths = new SimpleStructuredListTaskResult(bundlepaths);
	}

	@Override
	public StructuredListTaskResult getBundlePaths() {
		return bundlePaths;
	}

	@Override
	public StructuredListTaskResult getDownloadResults() {
		return downloadResults;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(downloadResults);
		out.writeObject(bundlePaths);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		downloadResults = (StructuredListTaskResult) in.readObject();
		bundlePaths = (StructuredListTaskResult) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((downloadResults == null) ? 0 : downloadResults.hashCode());
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
		DownloadBundleTaskOutputImpl other = (DownloadBundleTaskOutputImpl) obj;
		if (downloadResults == null) {
			if (other.downloadResults != null)
				return false;
		} else if (!downloadResults.equals(other.downloadResults))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + downloadResults + "]";
	}

}
