package saker.nest.support.impl.util;

import java.io.Externalizable;

import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleInformation;
import saker.nest.bundle.BundleKey;

public class BundleSouceAttachmentExecutionProperty
		extends AbstractBundleInformationExecutionProperty<BundleIdentifier> {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleSouceAttachmentExecutionProperty() {
	}

	public BundleSouceAttachmentExecutionProperty(BundleKey bundleKey) {
		super(bundleKey);
	}

	@Override
	protected BundleIdentifier getPropertyValue(BundleInformation bundleinfo) {
		return bundleinfo.getSourceAttachmentBundleIdentifier();
	}

}
