package saker.nest.support.impl.local.install;

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
import saker.nest.support.api.local.install.LocalInstallTaskOutput;

public class LocalInstallTaskOutputImpl implements LocalInstallTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private StructuredListTaskResult installResults;

	/**
	 * For {@link Externalizable}.
	 */
	public LocalInstallTaskOutputImpl() {
	}

	public LocalInstallTaskOutputImpl(List<? extends TaskIdentifier> workertaskids) {
		List<StructuredTaskResult> elementresults = new ArrayList<>();
		for (TaskIdentifier taskid : workertaskids) {
			elementresults.add(new SimpleStructuredObjectTaskResult(taskid));
		}
		this.installResults = new SimpleStructuredListTaskResult(elementresults);
	}

	@Override
	public StructuredListTaskResult getInstallResults() {
		return installResults;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(installResults);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		installResults = (StructuredListTaskResult) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((installResults == null) ? 0 : installResults.hashCode());
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
		LocalInstallTaskOutputImpl other = (LocalInstallTaskOutputImpl) obj;
		if (installResults == null) {
			if (other.installResults != null)
				return false;
		} else if (!installResults.equals(other.installResults))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[]";
	}

}
