package saker.nest.support.impl.dependency;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.lookup.LookupKey;

public class RootBundleLookupKeyExecutionProperty implements ExecutionProperty<LookupKey>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final RootBundleLookupKeyExecutionProperty INSTANCE = new RootBundleLookupKeyExecutionProperty();

	/**
	 * For {@link Externalizable}.
	 * 
	 * @see #INSTANCE
	 */
	public RootBundleLookupKeyExecutionProperty() {
	}

	@Override
	public LookupKey getCurrentValue(ExecutionContext executioncontext) {
		return ((NestBundleClassLoader) this.getClass().getClassLoader()).getBundleStorageConfiguration()
				.getBundleLookup().getLookupKey();
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
