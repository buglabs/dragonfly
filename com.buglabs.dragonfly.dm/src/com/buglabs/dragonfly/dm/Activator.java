/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleContext;

import com.buglabs.dragonfly.dm.model.BUGAdapterFactory;
import com.buglabs.dragonfly.dm.model.BUGResource;

import org.eclipse.rse.ui.SystemBasePlugin;

public class Activator extends SystemBasePlugin {

	private static Activator plugin;
	public static String PLUGIN_ID = "com.buglabs.dragonfly.dm"; //$NON-NLS-1$

	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.SystemBasePlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		IAdapterManager manager = Platform.getAdapterManager();
		BUGAdapterFactory factory = new BUGAdapterFactory();
		manager.registerAdapters(factory, BUGResource.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.SystemBasePlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static final String ICON_ID_DAYTIME = "ICON_ID_DAYTIME"; //$NON-NLS-1$

	protected void initializeImageRegistry() {
		String path = getIconPath();
		putImageInRegistry(ICON_ID_DAYTIME, path + "full/obj16/daytime.gif"); //$NON-NLS-1$
	}

}
