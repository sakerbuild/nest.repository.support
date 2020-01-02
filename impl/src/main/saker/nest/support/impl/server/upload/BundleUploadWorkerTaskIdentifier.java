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
import java.util.Objects;

import saker.build.file.path.SakerPath;
import saker.build.task.identifier.TaskIdentifier;

public class BundleUploadWorkerTaskIdentifier implements TaskIdentifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath bundlePath;
	private String server;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleUploadWorkerTaskIdentifier() {
	}

	public BundleUploadWorkerTaskIdentifier(SakerPath bundlePath, String server) {
		Objects.requireNonNull(bundlePath, "bundle path");
		this.bundlePath = bundlePath;
		this.server = server;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(bundlePath);
		out.writeObject(server);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundlePath = (SakerPath) in.readObject();
		server = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundlePath == null) ? 0 : bundlePath.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
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
		BundleUploadWorkerTaskIdentifier other = (BundleUploadWorkerTaskIdentifier) obj;
		if (bundlePath == null) {
			if (other.bundlePath != null)
				return false;
		} else if (!bundlePath.equals(other.bundlePath))
			return false;
		if (server == null) {
			if (other.server != null)
				return false;
		} else if (!server.equals(other.server))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (bundlePath != null ? "bundlePath=" + bundlePath + ", " : "")
				+ (server != null ? "server=" + server : "") + "]";
	}

}