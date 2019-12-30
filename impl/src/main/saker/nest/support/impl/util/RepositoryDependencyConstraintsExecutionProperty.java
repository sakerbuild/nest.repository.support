package saker.nest.support.impl.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.nest.bundle.DependencyConstraintConfiguration;
import saker.nest.bundle.NestBundleClassLoader;

public final class RepositoryDependencyConstraintsExecutionProperty
		implements ExecutionProperty<DependencyConstraintConfiguration>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final RepositoryDependencyConstraintsExecutionProperty INSTANCE = new RepositoryDependencyConstraintsExecutionProperty();

	/**
	 * For {@link Externalizable}.
	 */
	public RepositoryDependencyConstraintsExecutionProperty() {
	}

	@Override
	public DependencyConstraintConfiguration getCurrentValue(ExecutionContext executioncontext) throws Exception {
		NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
		return cl.getBundleStorageConfiguration().getDependencyConstraintConfiguration();
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

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[]";
	}

}
