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
package saker.nest.support.impl.server.upload;

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
import saker.nest.support.api.server.upload.BundleUploadTaskOutput;

public class BundleUploadTaskOutputImpl implements BundleUploadTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private StructuredListTaskResult uploadResults;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleUploadTaskOutputImpl() {
	}

	public BundleUploadTaskOutputImpl(List<? extends TaskIdentifier> workertaskids) {
		List<StructuredTaskResult> elementresults = new ArrayList<>();
		for (TaskIdentifier taskid : workertaskids) {
			elementresults.add(new SimpleStructuredObjectTaskResult(taskid));
		}
		this.uploadResults = new SimpleStructuredListTaskResult(elementresults);
	}

	@Override
	public StructuredListTaskResult getUploadResults() {
		return uploadResults;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(uploadResults);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		uploadResults = (StructuredListTaskResult) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uploadResults == null) ? 0 : uploadResults.hashCode());
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
		BundleUploadTaskOutputImpl other = (BundleUploadTaskOutputImpl) obj;
		if (uploadResults == null) {
			if (other.uploadResults != null)
				return false;
		} else if (!uploadResults.equals(other.uploadResults))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (uploadResults != null ? "uploadResults=" + uploadResults : "") + "]";
	}

}
