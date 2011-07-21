/*******************************************************************************
 * Copyright (c) 2006, 2007, 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly;

/**
 * Return the version string for the BUG API generated project was designed for.
 *
 */
public class APIVersionManager {

	public static final String BUG_API_VERSION_MANIFEST_KEY = "BUG-API-Version";

	/**
	 * Change upon every API-changing BUG sw release.
	 */
	private static final String DEFAULT_API_VERSION = "2.0.2";

	public static String getSDKAPIVersion() {		
		return DEFAULT_API_VERSION;
	}
}
