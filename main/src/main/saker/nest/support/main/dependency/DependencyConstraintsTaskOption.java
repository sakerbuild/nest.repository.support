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
