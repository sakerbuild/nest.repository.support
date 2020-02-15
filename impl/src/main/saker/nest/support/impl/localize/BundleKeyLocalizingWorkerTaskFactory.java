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

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.trace.BuildTrace;
import saker.nest.bundle.BundleKey;
import saker.nest.support.api.localize.LocalizeBundleWorkerTaskOutput;
import saker.nest.support.impl.util.BundleKeyLocalPathExecutionProperty;
import saker.nest.support.main.localize.LocalizeBundleTaskFactory;

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
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
			BuildTrace.setDisplayInformation("nest.localize:" + bundleKey.getBundleIdentifier(), null);
		}
		taskcontext.setStandardOutDisplayIdentifier(LocalizeBundleTaskFactory.TASK_NAME);

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
