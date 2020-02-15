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
package test.nest.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.build.tests.TestUtils;
import testing.saker.nest.util.NestIntegrationTestUtils;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class ResolveBundleInputBugTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	//bug test for the scenario where the dependency resolution task wasn't reinvoked when the global variable was modified
	//the bug only occurred again if there was an additional static variable (even if its unused)

	private static final SakerPath PATH_BUNDLES_DIRECTORY = PATH_WORKING_DIRECTORY.resolve("bundles");

	@Override
	protected void runTestImpl() throws Throwable {
		NavigableMap<String, Set<Class<?>>> bundleclasses = TestUtils.<String, Set<Class<?>>>treeMapBuilder()//
				.put("simple.bundle-v1", ObjectUtils.newHashSet())//
				.put("dep.bundle-v1", ObjectUtils.newHashSet())//
				.build();
		NestIntegrationTestUtils.createAllJarsFromDirectoriesWithClasses(files, PATH_BUNDLES_DIRECTORY,
				SakerPathFiles.getPathKey(files, PATH_WORKING_DIRECTORY), bundleclasses);

		String originalparambundlesparam = parameters.getUserParameters().get("nest.params.bundles");
		NestIntegrationTestUtils.addUserParam(parameters, "nest.params.bundles",
				originalparambundlesparam + ";" + PATH_WORKING_DIRECTORY.resolve("simple.bundle-v1.jar") + ";"
						+ PATH_WORKING_DIRECTORY.resolve("dep.bundle-v1.jar"));

		CombinedTargetTaskResult res;

		res = runScriptTask("build");
		assertEquals(toStringList(res.getTargetTaskResult("deps")), listOf("simple.bundle"));
		assertEquals(bundlesToStringList(res.getTargetTaskResult("output")), listOf("simple.bundle-v1"));

		SakerPath buildfilepath = PATH_WORKING_DIRECTORY.resolve(DEFAULT_BUILD_FILE_NAME);
		files.putFile(buildfilepath,
				files.getAllBytes(buildfilepath).toString().replace("simple.bundle", "#simple.bundle"));
		res = runScriptTask("build");
		assertEquals(toStringList(res.getTargetTaskResult("deps")), listOf());
		assertEquals(bundlesToStringList(res.getTargetTaskResult("output")), listOf());

		//the bug occurred at this step
		files.putFile(buildfilepath,
				files.getAllBytes(buildfilepath).toString().replace("#simple.bundle", "simple.bundle"));
		res = runScriptTask("build");
		getMetric().getRunTaskIdDeltas().entrySet().forEach(System.out::println);
		assertEquals(toStringList(res.getTargetTaskResult("deps")), listOf("simple.bundle"));
		assertEquals(bundlesToStringList(res.getTargetTaskResult("output")), listOf("simple.bundle-v1"));
	}

	private static Collection<String> toStringList(Object obj) throws Exception {
		List<String> result = new ArrayList<>();
		for (Object o : (Iterable<?>) obj) {
			result.add(o.toString());
		}
		return result;
	}

	private static Collection<String> bundlesToStringList(Object obj) throws Exception {
		List<String> result = new ArrayList<>();
		for (Object o : (Iterable<?>) obj) {
			result.add(o.getClass().getMethod("getBundleIdentifier").invoke(o).toString());
		}
		return result;
	}
}
