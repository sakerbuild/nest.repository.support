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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.SakerFile;
import saker.build.file.SakerFileBase;
import saker.build.file.content.ContentDescriptor;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.StreamUtils;
import saker.nest.bundle.NestRepositoryBundle;

public class TestInstrumentedSakerFile extends SakerFileBase {
	private static final FileTime FILETIME_ZERO = FileTime.fromMillis(0);
	private SakerFile archiveFile;
	private NestRepositoryBundle nestTestInstrumentationBundle;
	private ContentDescriptor contentDescriptor;

	public TestInstrumentedSakerFile(String name, SakerFile archiveFile, NestRepositoryBundle bundle,
			ContentDescriptor contentDescriptor) throws NullPointerException, InvalidPathFormatException {
		super(name);
		this.archiveFile = archiveFile;
		this.nestTestInstrumentationBundle = bundle;
		this.contentDescriptor = contentDescriptor;
	}

	@Override
	public ContentDescriptor getContentDescriptor() {
		return contentDescriptor;
	}

	@Override
	public void writeToStreamImpl(OutputStream os) throws IOException, NullPointerException {
		try (ZipOutputStream zos = new ZipOutputStream(StreamUtils.closeProtectedOutputStream(os))) {
			byte[] copybuffer = new byte[1024 * 8];
			try (InputStream archivein = archiveFile.openInputStream();
					ZipInputStream archivezis = new ZipInputStream(archivein)) {

				ZipEntry entry;
				while ((entry = archivezis.getNextEntry()) != null) {
					zos.putNextEntry(new ZipEntry(entry));
					StreamUtils.copyStream(archivezis, zos, copybuffer);
					zos.closeEntry();
				}
			}
			//copy the class files from the instrumentation bundle
			for (String bentryname : nestTestInstrumentationBundle.getEntryNames()) {
				if (!StringUtils.endsWithIgnoreCase(bentryname, ".class")) {
					continue;
				}
				ZipEntry nextentry = new ZipEntry(bentryname);
				nextentry.setLastAccessTime(FILETIME_ZERO);
				nextentry.setLastModifiedTime(FILETIME_ZERO);
				nextentry.setCreationTime(FILETIME_ZERO);
				zos.putNextEntry(nextentry);
				try (InputStream entryis = nestTestInstrumentationBundle.openEntry(bentryname)) {
					StreamUtils.copyStream(entryis, zos, copybuffer);
				}
			}
		}
	}

}
