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
package saker.nest.support.impl.dependency;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.SakerFile;
import saker.build.runtime.execution.FileDataComputer;
import saker.nest.bundle.BundleDependencyInformation;
import saker.nest.bundle.BundleIdentifier;

final class BundleDependencyInformationFileDataComputer
		implements FileDataComputer<BundleDependencyInformation>, Externalizable {
	private static final long serialVersionUID = 1L;

	private BundleIdentifier thisBundleId;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleDependencyInformationFileDataComputer() {
	}

	public BundleDependencyInformationFileDataComputer(BundleIdentifier thisBundleId) {
		this.thisBundleId = thisBundleId;
	}

	@Override
	public BundleDependencyInformation compute(SakerFile file) throws IOException {
		try (InputStream is = file.openInputStream()) {
			return BundleDependencyInformation.readFrom(is, thisBundleId);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(thisBundleId);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		thisBundleId = (BundleIdentifier) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((thisBundleId == null) ? 0 : thisBundleId.hashCode());
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
		BundleDependencyInformationFileDataComputer other = (BundleDependencyInformationFileDataComputer) obj;
		if (thisBundleId == null) {
			if (other.thisBundleId != null)
				return false;
		} else if (!thisBundleId.equals(other.thisBundleId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (thisBundleId != null ? "thisBundleId=" + thisBundleId : "") + "]";
	}

}