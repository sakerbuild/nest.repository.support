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
package saker.nest.support.main.dependency;

import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestInformation("Contains dependency constraints that restrict the resolution of dependencies.\n"
		+ "Any dependencies that doesn't match the specified constraints are filtered out during resolution.")
@NestFieldInformation(value = "JREMajorVersion",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The JRE major version dependency constraint."))
@NestFieldInformation(value = "RepositoryVersion",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The Nest repository version constraint."))
@NestFieldInformation(value = "BuildSystemVersion",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The saker.build system version constraint."))
@NestFieldInformation(value = "NativeArchitecture",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The constraint on the native architecture."))
public interface DependencyConstraintsTaskOption {
	public Integer getJREMajorVersion();

	public String getRepositoryVersion();

	public String getBuildSystemVersion();

	public String getNativeArchitecture();
}
