/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm.model;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;

import org.eclipse.rse.ui.view.AbstractSystemRemoteAdapterFactory;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;

public class BUGAdapterFactory extends AbstractSystemRemoteAdapterFactory implements IAdapterFactory {

	private BUGResourceAdapter daytimeAdapter = new BUGResourceAdapter();

	public BUGAdapterFactory() {
		super();
	}

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		ISystemViewElementAdapter adapter = null;
		if (adaptableObject instanceof BUGResource) {
			adapter = daytimeAdapter;
		}
		// these lines are very important! 
		if ((adapter != null) && (adapterType == IPropertySource.class)) {
			adapter.setPropertySourceInput(adaptableObject);
		}
		return adapter;
	}

}
