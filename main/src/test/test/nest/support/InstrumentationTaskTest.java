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

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.ByteSource;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class InstrumentationTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	private static final SakerPath OUTPUT_PATH_TEST_JAR = PATH_BUILD_DIRECTORY.resolve("nest.test.instrument/test.jar");

	@Override
	@SuppressWarnings("try")
	protected void runTestImpl() throws Throwable {
		try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
			try (JarOutputStream jaros = new JarOutputStream(baos)) {
				//just create basic JAR format, empty.
			}
			ByteArrayRegion jarbytes = baos.toByteArrayRegion();
			files.putFile(PATH_WORKING_DIRECTORY.resolve("test.jar"), jarbytes);
		}
		runScriptTask("build");
		assertHasEntryWithName(OUTPUT_PATH_TEST_JAR, "testing/saker/nest/TestFlag.class");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
			try (JarOutputStream jaros = new JarOutputStream(baos)) {
				jaros.putNextEntry(new ZipEntry("my_entry"));
				jaros.closeEntry();
			}
			ByteArrayRegion jarbytes = baos.toByteArrayRegion();
			files.putFile(PATH_WORKING_DIRECTORY.resolve("test.jar"), jarbytes);
		}

		runScriptTask("build");
		assertHasEntryWithName(OUTPUT_PATH_TEST_JAR, "testing/saker/nest/TestFlag.class");
		assertHasEntryWithName(OUTPUT_PATH_TEST_JAR, "my_entry");

		files.delete(OUTPUT_PATH_TEST_JAR);
		runScriptTask("build");
		assertHasEntryWithName(OUTPUT_PATH_TEST_JAR, "testing/saker/nest/TestFlag.class");
		assertHasEntryWithName(OUTPUT_PATH_TEST_JAR, "my_entry");

		runScriptTask("sakerbuild_instrument");
		assertHasEntryWithName(PATH_BUILD_DIRECTORY.resolve("nest.test.instrument/saker.build_instrument.jar"),
				"testing/saker/nest/TestFlag.class");
		assertHasEntryWithName(PATH_BUILD_DIRECTORY.resolve("nest.test.instrument/saker.build_instrument.jar"),
				"saker/build/meta/Versions.class");
	}

	private void assertHasEntryWithName(SakerPath filepath, String entryname) throws IOException, NoSuchFileException {
		try (ZipInputStream zis = new ZipInputStream(ByteSource.toInputStream(files.openInput(filepath)))) {
			for (ZipEntry entry; (entry = zis.getNextEntry()) != null;) {
				if (entry.getName().equals(entryname)) {
					return;
				}
			}
		}
		fail("Zip entry not found: " + entryname + " in " + filepath);
	}

}
