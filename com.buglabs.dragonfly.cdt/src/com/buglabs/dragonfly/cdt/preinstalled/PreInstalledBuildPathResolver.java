/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.cdt.preinstalled;

import java.io.File;

import org.eclipse.cdt.managedbuilder.core.IBuildPathResolver;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;

/**
 * Buildpath resolver for pre-existing Angstrom SDK.
 * @author kgilmer
 *
 */
public class PreInstalledBuildPathResolver implements IBuildPathResolver {

	private static final String ANGSTROM_LIB_PATH = PreInstalledToolchainSupportValidator.ANGSTROM_DEFAULT_TOOLCHAIN_ROOT + "/arm/arm-angstrom-linux-gnueabi/lib";
	
	private static final String[] DEFAULT_LIBRARY_PATHS = { 
		ANGSTROM_LIB_PATH,
	//"/usr/local/angstrom/arm/lib",
	};

	public String[] resolveBuildPaths(int pathType, String variableName, String variableValue, IConfiguration configuration) {
		if (variableName.equals("LIBRARY_PATH")) {
			return DEFAULT_LIBRARY_PATHS;
		}

		return variableValue.split(File.pathSeparator);
	}
}
