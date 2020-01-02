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
package saker.nest.support.api.dependency;

import java.util.Collection;

import saker.nest.bundle.BundleKey;

/**
 * Interface representing the output of the dependency resolution build task.
 * <p>
 * Provides access to the resolved bundles. The resolved bundles are represented using {@link BundleKey}s.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface DependencyResolutionTaskOutput {
	/**
	 * Gets the bundles that were resolved during the dependency resolution.
	 * 
	 * @return The bundle keys.
	 */
	public Collection<? extends BundleKey> getBundles();
}
