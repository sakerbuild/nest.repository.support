package saker.nest.support.impl.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.nest.bundle.NestBundleClassLoader;

public class DefaultLocalStorageNameExecutionProperty implements ExecutionProperty<String>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final ExecutionProperty<String> INSTANCE = new DefaultLocalStorageNameExecutionProperty();

	/**
	 * For {@link Externalizable}.
	 */
	public DefaultLocalStorageNameExecutionProperty() {
	}

	@Override
	public String getCurrentValue(ExecutionContext executioncontext) throws Exception {
		NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
		Map<String, ?> storages = cl.getBundleStorageConfiguration().getLocalStorages();
		if (storages.isEmpty()) {
			throw new IllegalArgumentException("No local bundle storages configured.");
		}
		if (storages.size() > 1) {
			throw new IllegalArgumentException(
					"Cannot determine default local storage, multiple configured: " + storages.keySet());
		}
		return storages.keySet().iterator().next();
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
