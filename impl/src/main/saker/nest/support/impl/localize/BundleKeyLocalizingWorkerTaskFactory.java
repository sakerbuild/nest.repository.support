package saker.nest.support.impl.localize;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.nest.bundle.BundleKey;
import saker.nest.support.api.localize.LocalizeBundleWorkerTaskOutput;
import saker.nest.support.impl.util.BundleKeyLocalPathExecutionProperty;

public class BundleKeyLocalizingWorkerTaskFactory
		implements TaskFactory<LocalizeBundleWorkerTaskOutput>, Task<LocalizeBundleWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private BundleKey bundleKey;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleKeyLocalizingWorkerTaskFactory() {
	}

	public BundleKeyLocalizingWorkerTaskFactory(BundleKey bundleKey) {
		this.bundleKey = bundleKey;
	}

	@Override
	public Task<? extends LocalizeBundleWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public LocalizeBundleWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		SakerPath localPath = taskcontext.getTaskUtilities()
				.getReportExecutionDependency(new BundleKeyLocalPathExecutionProperty(bundleKey));

		LocalizeWorkerTaskOutputImpl result = new LocalizeWorkerTaskOutputImpl(bundleKey, localPath);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(bundleKey);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundleKey = (BundleKey) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleKey == null) ? 0 : bundleKey.hashCode());
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
		BundleKeyLocalizingWorkerTaskFactory other = (BundleKeyLocalizingWorkerTaskFactory) obj;
		if (bundleKey == null) {
			if (other.bundleKey != null)
				return false;
		} else if (!bundleKey.equals(other.bundleKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (bundleKey != null ? "bundleKey=" + bundleKey : "") + "]";
	}

}
