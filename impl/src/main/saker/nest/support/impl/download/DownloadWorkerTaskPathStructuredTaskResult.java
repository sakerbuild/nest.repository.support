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

import saker.build.task.TaskResultResolver;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.StructuredTaskResult;
import saker.nest.support.api.download.DownloadBundleWorkerTaskOutput;

public class DownloadWorkerTaskPathStructuredTaskResult implements StructuredTaskResult, Externalizable {
	private static final long serialVersionUID = 1L;

	private TaskIdentifier downloadWorkerTaskId;

	/**
	 * For {@link Externalizable}.
	 */
	public DownloadWorkerTaskPathStructuredTaskResult() {
	}

	public DownloadWorkerTaskPathStructuredTaskResult(TaskIdentifier downloadWorkerTaskId) {
		this.downloadWorkerTaskId = downloadWorkerTaskId;
	}

	@Override
	public Object toResult(TaskResultResolver results) {
		DownloadBundleWorkerTaskOutput out = (DownloadBundleWorkerTaskOutput) StructuredTaskResult
				.getActualTaskResult(downloadWorkerTaskId, results);
		return out.getPath();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(downloadWorkerTaskId);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		downloadWorkerTaskId = (TaskIdentifier) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((downloadWorkerTaskId == null) ? 0 : downloadWorkerTaskId.hashCode());
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
		DownloadWorkerTaskPathStructuredTaskResult other = (DownloadWorkerTaskPathStructuredTaskResult) obj;
		if (downloadWorkerTaskId == null) {
			if (other.downloadWorkerTaskId != null)
				return false;
		} else if (!downloadWorkerTaskId.equals(other.downloadWorkerTaskId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + downloadWorkerTaskId + "]";
	}

}
