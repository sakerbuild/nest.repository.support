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
public class DependencyFileResolveTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private static final SakerPath PATH_BUNDLES_DIRECTORY = PATH_WORKING_DIRECTORY.resolve("bundles");

	@Override
	protected void runTestImpl() throws Throwable {

		NavigableMap<String, Set<Class<?>>> bundleclasses = TestUtils.<String, Set<Class<?>>>treeMapBuilder()//
				.put("simple.bundle-v1", ObjectUtils.newHashSet())//
				.put("dep.bundle-v1", ObjectUtils.newHashSet())//
				.put("dep.bundle-v2", ObjectUtils.newHashSet())//
				.build();
		NestIntegrationTestUtils.createAllJarsFromDirectoriesWithClasses(files, PATH_BUNDLES_DIRECTORY,
				SakerPathFiles.getPathKey(files, PATH_WORKING_DIRECTORY), bundleclasses);

		String originalparambundlesparam = parameters.getUserParameters().get("nest.params.bundles");
		NestIntegrationTestUtils.addUserParam(parameters, "nest.params.bundles",
				originalparambundlesparam + ";" + PATH_WORKING_DIRECTORY.resolve("simple.bundle-v1.jar") + ";"
						+ PATH_WORKING_DIRECTORY.resolve("dep.bundle-v1.jar") + ";"
						+ PATH_WORKING_DIRECTORY.resolve("dep.bundle-v2.jar"));

		CombinedTargetTaskResult res;

		res = runScriptTask("test");
		assertEquals(bundlesToStringList(res.getTargetTaskResult("output")), listOf("simple.bundle-v1"));

		res = runScriptTask("test");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(bundlesToStringList(res.getTargetTaskResult("output")), listOf("simple.bundle-v1"));

		//update the dependencies file
		files.putFile(PATH_WORKING_DIRECTORY.resolve("dependencies"),
				files.getAllBytes(PATH_WORKING_DIRECTORY.resolve("dependencies2")));

		res = runScriptTask("test");
		assertEquals(bundlesToStringList(res.getTargetTaskResult("output")),
				listOf("simple.bundle-v1", "dep.bundle-v2"));

		res = runScriptTask("pinnedtest");
		assertEquals(bundlesToStringList(res.getTargetTaskResult("output")),
				listOf("dep.bundle-v1", "simple.bundle-v1"));
	}

	private static Collection<String> bundlesToStringList(Object obj) throws Exception {
		List<String> result = new ArrayList<>();
		for (Object o : (Iterable<?>) obj) {
			result.add(o.getClass().getMethod("getBundleIdentifier").invoke(o).toString());
		}
		return result;
	}
}
