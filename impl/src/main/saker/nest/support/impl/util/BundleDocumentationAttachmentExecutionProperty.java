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
package saker.nest.support.impl.util;

import java.io.Externalizable;

import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleInformation;
import saker.nest.bundle.BundleKey;

public class BundleDocumentationAttachmentExecutionProperty
		extends AbstractBundleInformationExecutionProperty<BundleIdentifier> {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleDocumentationAttachmentExecutionProperty() {
	}

	public BundleDocumentationAttachmentExecutionProperty(BundleKey bundleKey) {
		super(bundleKey);
	}

	@Override
	protected BundleIdentifier getPropertyValue(BundleInformation bundleinfo) {
		return bundleinfo.getDocumentationAttachmentBundleIdentifier();
	}

}
