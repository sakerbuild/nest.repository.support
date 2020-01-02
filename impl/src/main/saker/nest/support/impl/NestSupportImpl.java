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
package saker.nest.support.impl;

import java.util.List;
import java.util.Objects;

import saker.build.file.path.SakerPath;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.nest.support.api.download.DownloadBundleTaskOutput;
import saker.nest.support.api.local.install.LocalInstallTaskOutput;
import saker.nest.support.api.local.install.LocalInstallWorkerTaskOutput;
import saker.nest.support.api.server.upload.BundleUploadTaskOutput;
import saker.nest.support.api.server.upload.BundleUploadWorkerTaskOutput;
import saker.nest.support.impl.download.DownloadBundleTaskOutputImpl;
import saker.nest.support.impl.local.install.BundleInstallerTaskFactory;
import saker.nest.support.impl.local.install.BundleInstallerTaskIdentifier;
import saker.nest.support.impl.local.install.LocalInstallTaskOutputImpl;
import saker.nest.support.impl.server.upload.BundleUploadTaskOutputImpl;
import saker.nest.support.impl.server.upload.BundleUploadWorkerTaskFactory;
import saker.nest.support.impl.server.upload.BundleUploadWorkerTaskIdentifier;

public class NestSupportImpl {
	private NestSupportImpl() {
		throw new UnsupportedOperationException();
	}

	public static TaskIdentifier createBundleUploadWorkerTaskIdentifier(SakerPath bundlepath, String serverurl) {
		return new BundleUploadWorkerTaskIdentifier(bundlepath, serverurl);
	}

	public static TaskFactory<? extends BundleUploadWorkerTaskOutput> createBundleUploadWorkerTaskFactory(
			SakerPath bundlePath, Boolean overwrite, String server, byte[] apiKey, byte[] apiSecret) {
		Objects.requireNonNull(bundlePath, "bundle path");
		Objects.requireNonNull(server, "server");
		Objects.requireNonNull(apiKey, "api key");
		Objects.requireNonNull(apiSecret, "api secret");
		return new BundleUploadWorkerTaskFactory(bundlePath, overwrite, server, apiKey.clone(), apiSecret.clone());
	}

	public static TaskIdentifier createLocalInstallWorkerTaskIdentifier(String storagename, SakerPath bundlepath) {
		return new BundleInstallerTaskIdentifier(storagename, bundlepath);
	}

	public static TaskFactory<? extends LocalInstallWorkerTaskOutput> createLocalInstallWorkerTaskFactory(
			String storagename, SakerPath bundlepath) {
		return new BundleInstallerTaskFactory(storagename, bundlepath);
	}

	public static BundleUploadTaskOutput createBundleUploadTaskOutput(List<? extends TaskIdentifier> workertaskids) {
		return new BundleUploadTaskOutputImpl(workertaskids);
	}

	public static DownloadBundleTaskOutput createBundleDownloadTaskOutput(
			List<? extends TaskIdentifier> bundleworkertaskids) {
		return new DownloadBundleTaskOutputImpl(bundleworkertaskids);
	}

	public static LocalInstallTaskOutput createLocalInstallTaskOutput(List<? extends TaskIdentifier> workertaskids) {
		return new LocalInstallTaskOutputImpl(workertaskids);
	}
}
