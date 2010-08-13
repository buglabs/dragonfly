/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.cdt.preinstalled;

import java.io.File;

import org.eclipse.cdt.managedbuilder.core.IManagedIsToolChainSupported;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.core.runtime.PluginVersionIdentifier;

public class PreInstalledToolchainSupportValidator implements IManagedIsToolChainSupported {

	public static final String ANGSTROM_DEFAULT_TOOLCHAIN_ROOT = "/usr/local/angstrom";

	@Override
	public boolean isSupported(IToolChain toolChain, PluginVersionIdentifier version, String instance) {
		File f = new File(ANGSTROM_DEFAULT_TOOLCHAIN_ROOT);
		return (f.exists() && f.isDirectory());
	}

}
