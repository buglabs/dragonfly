/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.cdt.preinstalled;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;

/**
 * Provides environment for pre-installed angstrom SDK.
 * @author kgilmer
 *
 */
public class PreInstalledProjectEnvironmentSupplier implements IProjectEnvironmentVariableSupplier {
	private static Map variableMap;

	public PreInstalledProjectEnvironmentSupplier() {
		variableMap = loadVariableMap();
	}

	private Map loadVariableMap() {
		Map m = new Hashtable();
		m.putAll(System.getenv());
		m.put("CPATH", PreInstalledToolchainSupportValidator.ANGSTROM_DEFAULT_TOOLCHAIN_ROOT + "/arm/arm-angstrom-linux-gnueabi/usr/include/");
		m.put("C_INCLUDE_PATH", 
				PreInstalledToolchainSupportValidator.ANGSTROM_DEFAULT_TOOLCHAIN_ROOT + "/arm/include" + File.pathSeparator + 
				PreInstalledToolchainSupportValidator.ANGSTROM_DEFAULT_TOOLCHAIN_ROOT + "/arm/arm-angstrom-linux-gnueabi/usr/include" + File.pathSeparator + 
				PreInstalledToolchainSupportValidator.ANGSTROM_DEFAULT_TOOLCHAIN_ROOT + "/arm/arm-angstrom-linux-gnueabi/include" + File.pathSeparator + 
				PreInstalledToolchainSupportValidator.ANGSTROM_DEFAULT_TOOLCHAIN_ROOT + "/arm/lib/gcc/arm-angstrom-linux-gnueabi/4.3.1/include" + File.pathSeparator + 
				PreInstalledToolchainSupportValidator.ANGSTROM_DEFAULT_TOOLCHAIN_ROOT + "/arm/include" + File.pathSeparator);
		m.put("LIBRARY_PATH", 
				PreInstalledToolchainSupportValidator.ANGSTROM_DEFAULT_TOOLCHAIN_ROOT + "/arm/arm-angstrom-linux-gnueabi/lib" + File.pathSeparator + 
				PreInstalledToolchainSupportValidator.ANGSTROM_DEFAULT_TOOLCHAIN_ROOT + "/arm/lib");

		return m;
	}

	@Override
	public IBuildEnvironmentVariable getVariable(final String variableName, IManagedProject project, IEnvironmentVariableProvider provider) {
		if (variableMap.containsKey(variableName)) {
			//System.out.println("Using predefined for " + variableName + "  value: " + variableMap.get(variableName));
			return new MapEntryEnvironmentVariable(variableMap, variableName);
		}

		if (variableName.startsWith("workspace_loc:")) {
			return new IBuildEnvironmentVariable() {

				@Override
				public String getValue() {
					return variableName.split(":")[1];
				}

				@Override
				public int getOperation() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public String getName() {
					return variableName.split(":")[0];
				}

				@Override
				public String getDelimiter() {
					return ":";
				}
			};
		}

		System.out.println("Looking for variable: " + variableName);

		return new IBuildEnvironmentVariable() {

			@Override
			public String getValue() {
				return null;
			}

			@Override
			public int getOperation() {
				return 0;
			}

			@Override
			public String getName() {
				return variableName;
			}

			@Override
			public String getDelimiter() {
				return ":";
			}
		};
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IManagedProject project, IEnvironmentVariableProvider provider) {
		List l = new ArrayList();

		for (Iterator i = variableMap.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			//System.out.println("Setting variable(s): " + key + " to " + variableMap.get(key));
			l.add(new MapEntryEnvironmentVariable(variableMap, key));
		}

		return (IBuildEnvironmentVariable[]) l.toArray(new IBuildEnvironmentVariable[l.size()]);
	}

	private class MapEntryEnvironmentVariable implements IBuildEnvironmentVariable {
		private final Map map;
		private final String key;

		public MapEntryEnvironmentVariable(Map map, String key) {
			this.map = map;
			this.key = key;
		}

		@Override
		public String getValue() {
			return (String) map.get(key);
		}

		@Override
		public int getOperation() {
			return 0;
		}

		@Override
		public String getName() {
			return key;
		}

		@Override
		public String getDelimiter() {
			return File.pathSeparator;
		}
	};

}
