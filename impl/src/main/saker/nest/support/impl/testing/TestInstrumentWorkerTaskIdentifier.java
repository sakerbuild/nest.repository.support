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
package saker.nest.support.impl.testing;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.task.identifier.TaskIdentifier;

public class TestInstrumentWorkerTaskIdentifier implements TaskIdentifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath relativeOutputPath;

	/**
	 * For {@link Externalizable}.
	 */
	public TestInstrumentWorkerTaskIdentifier() {
	}

	public TestInstrumentWorkerTaskIdentifier(SakerPath relativeOutputPath) {
		this.relativeOutputPath = relativeOutputPath;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(relativeOutputPath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		relativeOutputPath = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((relativeOutputPath == null) ? 0 : relativeOutputPath.hashCode());
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
		TestInstrumentWorkerTaskIdentifier other = (TestInstrumentWorkerTaskIdentifier) obj;
		if (relativeOutputPath == null) {
			if (other.relativeOutputPath != null)
				return false;
		} else if (!relativeOutputPath.equals(other.relativeOutputPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TestInstrumentWorkerTaskIdentifier["
				+ (relativeOutputPath != null ? "relativeOutputPath=" + relativeOutputPath : "") + "]";
	}

}
