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

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.nest.bundle.BundleKey;
import saker.nest.support.api.download.DownloadBundleWorkerTaskOutput;

public class SimpleDownloadWorkerTaskOutput implements DownloadBundleWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath path;
	private BundleKey bundleKey;
	private ContentDescriptor contentDescriptor;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleDownloadWorkerTaskOutput() {
	}

	public SimpleDownloadWorkerTaskOutput(SakerPath path, BundleKey bundleKey, ContentDescriptor contentdescriptor) {
		this.path = path;
		this.bundleKey = bundleKey;
		this.contentDescriptor = contentdescriptor;
	}

	@Override
	public SakerPath getPath() {
		return path;
	}

	@Override
	public BundleKey getBundleKey() {
		return bundleKey;
	}

	@Override
	public ContentDescriptor getContentDescriptor() {
		return contentDescriptor;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
		out.writeObject(bundleKey);
		out.writeObject(contentDescriptor);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = (SakerPath) in.readObject();
		bundleKey = (BundleKey) in.readObject();
		contentDescriptor = (ContentDescriptor) in.readObject();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (path != null ? "path=" + path : "") + "]";
	}

}
