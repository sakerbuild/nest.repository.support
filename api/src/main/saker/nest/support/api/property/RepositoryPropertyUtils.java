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
package saker.nest.support.api.property;

import saker.build.runtime.execution.ExecutionProperty;
import saker.nest.bundle.DependencyConstraintConfiguration;
import saker.nest.bundle.NestBundleStorageConfiguration;
import saker.nest.bundle.storage.LocalBundleStorageView;
import saker.nest.bundle.storage.ServerBundleStorageView;
import saker.nest.support.impl.util.DefaultLocalStorageNameExecutionProperty;
import saker.nest.support.impl.util.DefaultServerStorageNameExecutionProperty;
import saker.nest.support.impl.util.RepositoryDependencyConstraintsExecutionProperty;

/**
 * Utility class for working with properties related to saker.nest repository storage configuration.
 * 
 * @see NestBundleStorageConfiguration
 */
public class RepositoryPropertyUtils {
	private RepositoryPropertyUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets an execution property that returns the current {@link DependencyConstraintConfiguration}.
	 * 
	 * @return The execution property.
	 * @see NestBundleStorageConfiguration#getDependencyConstraintConfiguration()
	 */
	public static ExecutionProperty<? extends DependencyConstraintConfiguration> getRepositoryDependencyConstraingConfigurationExecutionProperty() {
		return RepositoryDependencyConstraintsExecutionProperty.INSTANCE;
	}

	/**
	 * Gets an execution property that returns the name of the default local bundle storage if it can be determined.
	 * <p>
	 * If the name cannot be uniquely determined, the property computation throws an exception.
	 * 
	 * @return The execution property.
	 * @see NestBundleStorageConfiguration#getLocalStorages()
	 * @see LocalBundleStorageView
	 */
	public static ExecutionProperty<String> getDefaultLocalStorageNameExecutionProperty() {
		return DefaultLocalStorageNameExecutionProperty.INSTANCE;
	}

	/**
	 * Gets an execution property that returns the name of the default server bundle storage if it can be determined.
	 * <p>
	 * If the name cannot be uniquely determined, the property computation throws an exception.
	 * 
	 * @return The execution property.
	 * @see NestBundleStorageConfiguration#getServerStorages()
	 * @see ServerBundleStorageView
	 */
	public static ExecutionProperty<String> getDefaultServerStorageNameExecutionProperty() {
		return DefaultServerStorageNameExecutionProperty.INSTANCE;
	}
}
