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
package saker.nest.support.impl.local.install;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.NoSuchFileException;

import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.trace.BuildTrace;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.storage.LocalBundleStorageView;
import saker.nest.bundle.storage.LocalBundleStorageView.InstallResult;
import saker.nest.support.api.local.install.LocalInstallWorkerTaskOutput;
import saker.nest.support.impl.util.BundleKeyContentDescriptorExecutionProperty;
import saker.nest.support.main.local.install.LocalInstallTaskFactory;

public class BundleInstallerTaskFactory implements TaskFactory<LocalInstallWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	protected String storageName;
	protected SakerPath bundlePath;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleInstallerTaskFactory() {
	}

	public BundleInstallerTaskFactory(String storageName, SakerPath bundlePath) {
		this.storageName = storageName;
		this.bundlePath = bundlePath;
	}

	@Override
	public Task<? extends LocalInstallWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return new Task<LocalInstallWorkerTaskOutput>() {
			@Override
			public LocalInstallWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
					BuildTrace.setDisplayInformation("nest.install:" + bundlePath.getFileName(), null);
				}
				taskcontext.setStandardOutDisplayIdentifier(LocalInstallTaskFactory.TASK_NAME);

				SakerFile bundlefile = taskcontext.getTaskUtilities().resolveFileAtPath(bundlePath);
				if (bundlefile == null) {
					throw new NoSuchFileException(bundlePath.toString(), null, "Bundle JAR file not found.");
				}
				NamedLocalBundleStorageExecutionProperty.PropertyValue localstorageproperty = taskcontext
						.getTaskUtilities()
						.getReportExecutionDependency(new NamedLocalBundleStorageExecutionProperty(storageName));
				if (!localstorageproperty.isPresent()) {
					if (storageName == null) {
						throw new IllegalArgumentException(
								"Failed to determine local storage to install the bundle to. "
										+ "Use StorageName parameter to specify its name.");
					}
					throw new IllegalArgumentException("Local bundle storage not found with name: " + storageName);
				}
				taskcontext.getTaskUtilities().reportInputFileDependency(null, bundlefile);
				InstallResult installresult;
				BundleIdentifier installedbundleid;
				try {
					LocalBundleStorageView storageview = localstorageproperty.getStorageView();
					installresult = storageview.install(bundlefile);
					installedbundleid = installresult.getBundleIdentifier();
					SakerLog.success().out(taskcontext).verbose()
							.println("Bundle successfully installed: " + installedbundleid);

					ContentDescriptor hashcd = BundleKeyContentDescriptorExecutionProperty
							.createContentDescriptorForBundleHash(installresult.getBundleHash());

					//install a dependency that tracks the current bundle hash, and reinvokes this task if changes
					taskcontext.reportExecutionDependency(
							new BundleKeyContentDescriptorExecutionProperty(
									BundleKey.create(storageview.getStorageViewKey(), installedbundleid)),
							new BundleKeyContentDescriptorExecutionProperty.PropertyResult(null, hashcd));
				} catch (Exception e) {
					SakerLog.error().out(taskcontext).println("Failed to install bundle: "
							+ SakerPathFiles.toRelativeString(bundlePath) + " (" + e + ")");
					throw e;
				}
				return new LocalInstallWorkerTaskOutputImpl(installedbundleid,
						StringUtils.toHexString(installresult.getBundleHash()));
			}
		};
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(storageName);
		out.writeObject(bundlePath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		storageName = (String) in.readObject();
		bundlePath = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundlePath == null) ? 0 : bundlePath.hashCode());
		result = prime * result + ((storageName == null) ? 0 : storageName.hashCode());
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
		BundleInstallerTaskFactory other = (BundleInstallerTaskFactory) obj;
		if (bundlePath == null) {
			if (other.bundlePath != null)
				return false;
		} else if (!bundlePath.equals(other.bundlePath))
			return false;
		if (storageName == null) {
			if (other.storageName != null)
				return false;
		} else if (!storageName.equals(other.storageName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + storageName + " : " + bundlePath + "]";
	}

}