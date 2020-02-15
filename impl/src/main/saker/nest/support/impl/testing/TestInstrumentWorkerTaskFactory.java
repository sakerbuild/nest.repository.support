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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.MultiContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.trace.BuildTrace;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.NestRepositoryBundle;
import saker.nest.support.api.property.BundleContentDescriptorPropertyValue;
import saker.nest.support.impl.util.BundleKeyContentDescriptorExecutionProperty;
import saker.nest.support.main.testing.TestInstrumentTaskFactory;

public class TestInstrumentWorkerTaskFactory implements TaskFactory<SakerPath>, Task<SakerPath>, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath outputFilePath;
	private SakerPath archive;
	private BundleKey nestBundle;

	/**
	 * For {@link Externalizable}.
	 */
	public TestInstrumentWorkerTaskFactory() {
	}

	public TestInstrumentWorkerTaskFactory(SakerPath outputFilePath, SakerPath archive, BundleKey nestBundle) {
		this.outputFilePath = outputFilePath;
		this.archive = archive;
		this.nestBundle = nestBundle;
	}

	@Override
	public Task<? extends SakerPath> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public SakerPath run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
		}
		taskcontext.setStandardOutDisplayIdentifier(TestInstrumentTaskFactory.TASK_NAME);

		SakerFile archivefile = taskcontext.getTaskUtilities().resolveFileAtPath(archive);
		if (archivefile == null) {
			taskcontext.reportInputFileDependency(null, archive, CommonTaskContentDescriptors.IS_NOT_FILE);
			taskcontext.abortExecution(
					new FileNotFoundException("Archive file to test instrument was not found: " + archive));
			return null;
		}
		taskcontext.getTaskUtilities().reportInputFileDependency(null, archivefile);

		SakerDirectory outdir = taskcontext.getTaskUtilities().resolveDirectoryAtPathCreate(SakerPathFiles
				.requireBuildDirectory(taskcontext).getDirectoryCreate(TestInstrumentTaskFactory.TASK_NAME),
				outputFilePath.getParent());
		BundleContentDescriptorPropertyValue bundlepropvalue = taskcontext.getTaskUtilities()
				.getReportExecutionDependency(new BundleKeyContentDescriptorExecutionProperty(nestBundle));
		NestRepositoryBundle testbundle = bundlepropvalue.getBundle();
		ContentDescriptor filecd = MultiContentDescriptor.create(archivefile.getContentDescriptor(),
				bundlepropvalue.getContentDescriptor());
		TestInstrumentedSakerFile addfile = new TestInstrumentedSakerFile(outputFilePath.getFileName(), archivefile,
				testbundle, filecd);

		outdir.add(addfile);
		addfile.synchronize();
		SakerPath outputpath = addfile.getSakerPath();
		taskcontext.reportOutputFileDependency(null, outputpath, filecd);
		return outputpath;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(outputFilePath);
		out.writeObject(archive);
		out.writeObject(nestBundle);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		outputFilePath = (SakerPath) in.readObject();
		archive = (SakerPath) in.readObject();
		nestBundle = (BundleKey) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((archive == null) ? 0 : archive.hashCode());
		result = prime * result + ((nestBundle == null) ? 0 : nestBundle.hashCode());
		result = prime * result + ((outputFilePath == null) ? 0 : outputFilePath.hashCode());
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
		TestInstrumentWorkerTaskFactory other = (TestInstrumentWorkerTaskFactory) obj;
		if (archive == null) {
			if (other.archive != null)
				return false;
		} else if (!archive.equals(other.archive))
			return false;
		if (nestBundle == null) {
			if (other.nestBundle != null)
				return false;
		} else if (!nestBundle.equals(other.nestBundle))
			return false;
		if (outputFilePath == null) {
			if (other.outputFilePath != null)
				return false;
		} else if (!outputFilePath.equals(other.outputFilePath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TestInstrumentWorkerTaskFactory[" + (archive != null ? "archive=" + archive + ", " : "")
				+ (nestBundle != null ? "nestBundle=" + nestBundle : "") + "]";
	}

}
