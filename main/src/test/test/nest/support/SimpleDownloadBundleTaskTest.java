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

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.build.tests.EnvironmentTestCase;
import testing.saker.build.tests.TestUtils;
import testing.saker.nest.util.NestIntegrationTestUtils;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class SimpleDownloadBundleTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	public static class AppendClass {
	}

	@Override
	protected void runTestImpl() throws Throwable {
		TreeMap<String, Set<Class<?>>> bundleclasses;
		bundleclasses = TestUtils.<String, Set<Class<?>>>treeMapBuilder()//
				.put("simple.bundle-v1", ObjectUtils.newHashSet())//
				.put("dep.bundle-v1", ObjectUtils.newHashSet())//
				.build();

		String classsubdirpath = getClass().getName().replace('.', '/');
		Path workdir = EnvironmentTestCase.getTestingBaseWorkingDirectory().resolve(classsubdirpath);
		Path bundleoutdir = EnvironmentTestCase.getTestingBaseBuildDirectory().resolve(classsubdirpath);
		NestIntegrationTestUtils.createAllJarsFromDirectoriesWithClasses(LocalFileProvider.getInstance(),
				SakerPath.valueOf(workdir).resolve("bundles"), bundleoutdir, bundleclasses);

		String originalstorageconfig = parameters.getUserParameters().get("nest.params.bundles");
		NestIntegrationTestUtils.appendToUserParam(parameters, "nest.params.bundles",
				";" + NestIntegrationTestUtils.createParameterBundlesParameter(bundleclasses.keySet(), bundleoutdir));
		String nstorageconfig = parameters.getUserParameters().get("nest.params.bundles");

		SakerPath simplebundletaskoutdir = PATH_BUILD_DIRECTORY.resolve("nest.bundle.download")
				.resolve("simple.bundle");
		SakerPath depbundletaskoutdir = PATH_BUILD_DIRECTORY.resolve("nest.bundle.download").resolve("dep.bundle");

		Set<String> entries;

		runScriptTask("download");
		entries = files.getDirectoryEntryNames(simplebundletaskoutdir);
		assertEquals(entries.size(), 1);

		SakerPath pathv1 = simplebundletaskoutdir.resolve(entries.iterator().next());

		runScriptTask("download");
		assertEmpty(getMetric().getRunTaskIdFactories());

		//make sure it was put back
		files.delete(pathv1);
		runScriptTask("download");
		files.getAllBytes(pathv1);

		//no config, the bundle shouldn't be there
		NestIntegrationTestUtils.addUserParam(parameters, "nest.params.bundles", originalstorageconfig);
		assertTaskException("saker.nest.exc.BundleLoadingFailedException", () -> runScriptTask("download"));

		NestIntegrationTestUtils.addUserParam(parameters, "nest.params.bundles", nstorageconfig);
		runScriptTask("download");

		//re-export bundle with update
		bundleclasses = TestUtils.<String, Set<Class<?>>>treeMapBuilder()//
				.put("simple.bundle-v1", ObjectUtils.newHashSet(AppendClass.class))//
				.build();
		NestIntegrationTestUtils.createAllJarsFromDirectoriesWithClasses(LocalFileProvider.getInstance(),
				SakerPath.valueOf(workdir).resolve("bundles"), bundleoutdir, bundleclasses);

		//the updated bundle will be downloaded to a new path
		runScriptTask("download");
		assertNotEmpty(getMetric().getRunTaskIdFactories());
		assertNotEquals(files.getDirectoryEntryNames(simplebundletaskoutdir), entries);

		files.clearDirectoryRecursively(PATH_BUILD_DIRECTORY);
		runScriptTask("resolvedownload");
		assertEquals(files.getDirectoryEntryNames(simplebundletaskoutdir).size(), 1);
		assertEquals(files.getDirectoryEntryNames(depbundletaskoutdir).size(), 1);

		files.clearDirectoryRecursively(PATH_BUILD_DIRECTORY);
		assertEquals(files.getDirectoryEntryNames(PATH_BUILD_DIRECTORY).size(), 0);
		runScriptTask("listdownload");
		assertEquals(files.getDirectoryEntryNames(simplebundletaskoutdir).size(), 1);
		assertEquals(files.getDirectoryEntryNames(depbundletaskoutdir).size(), 1);

		CombinedTargetTaskResult res;

		res = runScriptTask("bundlepathsout");
		res.getTargetTaskResult("paths");
	}
}
