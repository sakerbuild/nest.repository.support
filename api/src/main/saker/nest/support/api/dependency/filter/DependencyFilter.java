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
package saker.nest.support.api.dependency.filter;

import java.io.Externalizable;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import saker.nest.bundle.BundleDependencyInformation;
import saker.nest.bundle.BundleDependencyList;
import saker.nest.bundle.BundleKey;
import saker.nest.support.impl.dependency.filter.ChainDependencyFilter;
import saker.nest.support.impl.dependency.filter.IdentityDependencyFilter;
import saker.nest.support.impl.dependency.filter.NoneDependencyFilter;

/**
 * Interface for filtering the dependencies.
 * <p>
 * A dependency filter is used to limit or extend the dependencies for a given operation. They are most of the time used
 * by the dependency resolution task to select the dependencies that should be part of the resolution.
 * <p>
 * The interface defines a stateless strategy that shouldn't depend on external information, only on the one that is
 * passed to the {@link #filterBundleDependency(BundleKey, BundleDependencyInformation)} function.
 * <p>
 * This interface is also an external extension point for the dependency resolution task.
 * <p>
 * Clients should implement this interface. When doing so, make sure to adhere to the contract specified by
 * {@link #hashCode()} and {@link #equals(Object)}. Implementations are also recommended to implement the
 * {@link Externalizable} interface.
 * <p>
 * Common dependency filters can be retrieved using the static factory methods in this interface.
 */
public interface DependencyFilter {
	/**
	 * Filters the dependency information.
	 * <p>
	 * The method implementation should examine the input dependency information and may perform arbitrary operations to
	 * transform the dependencies.
	 * <p>
	 * If the <code>owner</code> argument is <code>null</code>, then the dependency information is the root dependencies
	 * that were passed as a direct input from the user.
	 * 
	 * @param owner
	 *            The owner of the dependency information. If <code>null</code>, then this is the root dependency
	 *            information for the operation.
	 * @param depinfo
	 *            The dependency information to transform or filter.
	 * @return The filtered dependency information, or <code>null</code> to omit all dependencies. May also be
	 *             {@linkplain BundleDependencyInformation#EMPTY empty}.
	 * @see BundleDependencyInformation#filter(BiFunction)
	 * @see BundleDependencyList#filter(Function)
	 */
	public BundleDependencyInformation filterBundleDependency(BundleKey owner, BundleDependencyInformation depinfo);

	@Override
	public int hashCode();

	/**
	 * Checks if this filter equals to the argument.
	 * <p>
	 * Two dependency filters equal if they produce the same results for the same inputs given the same circumstances.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj);

	/**
	 * Gets the identity dependency filter.
	 * <p>
	 * The identity dependency filter returns the input dependency information without modification.
	 * 
	 * @return The filter.
	 */
	public static DependencyFilter identity() {
		return IdentityDependencyFilter.INSTANCE;
	}

	/**
	 * Gets the none dependency filter.
	 * <p>
	 * The none dependency filter will omit all dependencies. It always returns
	 * {@link BundleDependencyInformation#EMPTY}.
	 * 
	 * @return The filter.
	 */
	public static DependencyFilter none() {
		return NoneDependencyFilter.INSTANCE;
	}

	/**
	 * Creates a chain dependency filter.
	 * <p>
	 * A chain dependency filter will call all filters in succession with the output of the previous filter as the input
	 * to the next. If any of the filters return <code>null</code>, the invocation chain will break, and
	 * <code>null</code> is returned.
	 * 
	 * @param filters
	 *            The dependency filters.
	 * @return The filter.
	 * @throws NullPointerException
	 *             If the argument or any of the filters are <code>null</code>.
	 */
	public static DependencyFilter chain(Collection<? extends DependencyFilter> filters) throws NullPointerException {
		return ChainDependencyFilter.create(filters);
	}
}
