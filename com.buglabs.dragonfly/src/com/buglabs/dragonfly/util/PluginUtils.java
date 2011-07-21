/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.buglabs.dragonfly.IProcessExtension;

public class PluginUtils {

	/**
	 * For a given Extension Point ID, retrieve the available extensions in the
	 * plugin runtime.
	 * 
	 * @param xpid
	 * @param processor
	 */
	public static void processExtenders(String xpid, IProcessExtension processor) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(xpid);
		IExtension[] extensions = extensionPoint.getExtensions();

		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elements = extension.getConfigurationElements();

			for (int j = 0; j < elements.length; j++) {
				IConfigurationElement element = elements[j];
				processor.process(extension, element);
			}
		}
	}
}
