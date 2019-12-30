package test.nest.support;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class LocalInstallTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	public static class BundleTask implements TaskFactory<String>, Task<String>, Externalizable {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public BundleTask() {
		}

		@Override
		public String run(TaskContext taskcontext) throws Exception {
			return "hello";
		}

		@Override
		public Task<? extends String> createTask(ExecutionContext executioncontext) {
			return this;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		}

		@Override
		public int hashCode() {
			return getClass().getName().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return ObjectUtils.isSameClass(this, obj);
		}
	}

	@Override
	protected void runTestImpl() throws Throwable {
		ByteArrayRegion jarbytes;
		try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			manifest.getMainAttributes().putValue("Nest-Bundle-Format-Version", "1");
			manifest.getMainAttributes().putValue("Nest-Bundle-Identifier", "test.bundle-v1");
			try (JarOutputStream jaros = new JarOutputStream(baos, manifest)) {
				jaros.putNextEntry(new ZipEntry(BundleTask.class.getName().replace(".", "/") + ".class"));
				ReflectUtils.getClassBytesUsingClassLoader(BundleTask.class).writeTo(jaros);
				jaros.closeEntry();
				jaros.putNextEntry(new ZipEntry("META-INF/nest/tasks"));
				jaros.write(("test.bundle.task=" + BundleTask.class.getName()).getBytes(StandardCharsets.UTF_8));
				jaros.closeEntry();
			}
			jarbytes = baos.toByteArrayRegion();
		}
		files.putFile(PATH_WORKING_DIRECTORY.resolve("test.bundle.jar"), jarbytes);
		runScriptTask("install");

		runScriptTask("install");
		assertEmpty(getMetric().getRunTaskIdFactories());
		
		CombinedTargetTaskResult res;
		res = runScriptTask("use");
		assertEquals(res.getTargetTaskResult("output"), "hello");
		
		res = runScriptTask("use");
		assertEquals(res.getTargetTaskResult("output"), "hello");
		assertEmpty(getMetric().getRunTaskIdFactories());
	}

	@Override
	protected String getRepositoryStorageConfiguration() {
		return "[:params,:local]";
	}
}
