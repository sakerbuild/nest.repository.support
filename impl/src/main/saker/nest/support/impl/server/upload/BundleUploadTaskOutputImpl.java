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
