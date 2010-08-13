/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.cdt;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;

public class ConfigEnvSupplier implements IConfigurationEnvironmentVariableSupplier {

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration, IEnvironmentVariableProvider provider) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration, IEnvironmentVariableProvider provider) {
		// TODO Auto-generated method stub
		return null;
	}

}
