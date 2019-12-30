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

public class DefaultServerStorageNameExecutionProperty implements ExecutionProperty<String>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final ExecutionProperty<String> INSTANCE = new DefaultServerStorageNameExecutionProperty();

	/**
	 * For {@link Externalizable}.
	 */
	public DefaultServerStorageNameExecutionProperty() {
	}

	@Override
	public String getCurrentValue(ExecutionContext executioncontext) throws Exception {
		NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
		Map<String, ?> storages = cl.getBundleStorageConfiguration().getServerStorages();
		if (storages.isEmpty()) {
			throw new IllegalArgumentException("No server bundle storages configured.");
		}
		if (storages.size() > 1) {
			throw new IllegalArgumentException(
					"Cannot determine default server storage, multiple configured: " + storages.keySet());
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
