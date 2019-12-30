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
