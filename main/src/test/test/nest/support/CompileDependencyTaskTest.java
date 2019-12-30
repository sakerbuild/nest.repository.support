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
public class CompileDependencyTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private static final SakerPath PATH_BUNDLES_DIRECTORY = PATH_WORKING_DIRECTORY.resolve("bundles");

	@Override
	protected void runTestImpl() throws Throwable {
		NavigableMap<String, Set<Class<?>>> bundleclasses = TestUtils.<String, Set<Class<?>>>treeMapBuilder()//
				.put("simple.bundle-v1", ObjectUtils.newHashSet())//
				.put("simple.bundle-api-v1", ObjectUtils.newHashSet())//
				.put("simple.bundle-aimpl-v1", ObjectUtils.newHashSet())//
				.build();
		NestIntegrationTestUtils.createAllJarsFromDirectoriesWithClasses(files, PATH_BUNDLES_DIRECTORY,
				SakerPathFiles.getPathKey(files, PATH_WORKING_DIRECTORY), bundleclasses);

		String originalparambundlesparam = parameters.getUserParameters().get("nest.params.bundles");
		NestIntegrationTestUtils.addUserParam(parameters, "nest.params.bundles",
				originalparambundlesparam + ";" + PATH_WORKING_DIRECTORY.resolve("simple.bundle-v1.jar") + ";"
						+ PATH_WORKING_DIRECTORY.resolve("simple.bundle-api-v1.jar") + ";"
						+ PATH_WORKING_DIRECTORY.resolve("simple.bundle-impl-v1.jar"));

		CombinedTargetTaskResult res;

		res = runScriptTask("test");
		assertEquals(bundlesToStringList(res.getTargetTaskResult("output")),
				listOf("simple.bundle-v1", "simple.bundle-api-v1", "simple.bundle-impl-v1"));

		res = runScriptTask("test");
		assertEmpty(getMetric().getRunTaskIdFactories());

		res = runScriptTask("nontransitive");
		assertEquals(bundlesToStringList(res.getTargetTaskResult("output")),
				listOf("simple.bundle-v1", "simple.bundle-api-v1"));

		res = runScriptTask("nontransitive");
		assertEmpty(getMetric().getRunTaskIdFactories());
	}

	private static Collection<String> bundlesToStringList(Object obj) throws Exception {
		List<String> result = new ArrayList<>();
		for (Object o : (Iterable<?>) obj) {
			result.add(o.getClass().getMethod("getBundleIdentifier").invoke(o).toString());
		}
		return result;
	}
}
