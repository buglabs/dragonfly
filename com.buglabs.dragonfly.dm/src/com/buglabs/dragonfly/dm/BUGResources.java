/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm;

import org.eclipse.osgi.util.NLS;

public class BUGResources extends NLS {
	private static String BUNDLE_NAME = "com.buglabs.dragonfly.dm.BUGResources";//$NON-NLS-1$

	public static String BUG_Service_Name;
	public static String BUG_Service_Description;
	public static String BUG_Connector_Name;
	public static String BUG_Connector_Description;
	public static String BUG_Resource_Type;

	public static String DaytimeConnectorService_NotAvailable;


	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, BUGResources.class);
	}

}
