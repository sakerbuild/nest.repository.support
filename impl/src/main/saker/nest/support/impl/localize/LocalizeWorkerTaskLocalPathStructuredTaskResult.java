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

import saker.build.task.TaskResultResolver;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.StructuredTaskResult;
import saker.nest.support.api.localize.LocalizeBundleWorkerTaskOutput;

public class LocalizeWorkerTaskLocalPathStructuredTaskResult implements StructuredTaskResult, Externalizable {
	private static final long serialVersionUID = 1L;

	private TaskIdentifier localizeWorkerTaskId;

	/**
	 * For {@link Externalizable}.
	 */
	public LocalizeWorkerTaskLocalPathStructuredTaskResult() {
	}

	public LocalizeWorkerTaskLocalPathStructuredTaskResult(TaskIdentifier localizeWorkerTaskId) {
		this.localizeWorkerTaskId = localizeWorkerTaskId;
	}

	@Override
	public Object toResult(TaskResultResolver results) {
		LocalizeBundleWorkerTaskOutput out = (LocalizeBundleWorkerTaskOutput) StructuredTaskResult
				.getActualTaskResult(localizeWorkerTaskId, results);
		return out.getLocalPath();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(localizeWorkerTaskId);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		localizeWorkerTaskId = (TaskIdentifier) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((localizeWorkerTaskId == null) ? 0 : localizeWorkerTaskId.hashCode());
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
		LocalizeWorkerTaskLocalPathStructuredTaskResult other = (LocalizeWorkerTaskLocalPathStructuredTaskResult) obj;
		if (localizeWorkerTaskId == null) {
			if (other.localizeWorkerTaskId != null)
				return false;
		} else if (!localizeWorkerTaskId.equals(other.localizeWorkerTaskId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + localizeWorkerTaskId + "]";
	}

}
