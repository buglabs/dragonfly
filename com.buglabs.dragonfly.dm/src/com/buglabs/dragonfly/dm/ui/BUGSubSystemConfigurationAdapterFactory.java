/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;

import com.buglabs.dragonfly.dm.subsystems.BUGOSGiSubSystemConfiguration;

import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;

public class BUGSubSystemConfigurationAdapterFactory implements IAdapterFactory {

	private ISubSystemConfigurationAdapter ssFactoryAdapter = new BUGSubSystemConfigurationAdapter();

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { ISubSystemConfigurationAdapter.class };
	}

	/**
	 * Called by our plugin's startup method to register our adaptable object
	 * types with the platform. We prefer to do it here to isolate/encapsulate
	 * all factory logic in this one place.
	 * 
	 * @param manager
	 *            Platform adapter manager to register with
	 */
	public void registerWithManager(IAdapterManager manager) {
		manager.registerAdapters(this, BUGOSGiSubSystemConfiguration.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		Object adapter = null;
		if (adaptableObject instanceof BUGOSGiSubSystemConfiguration)
			adapter = ssFactoryAdapter;

		return adapter;
	}

}
