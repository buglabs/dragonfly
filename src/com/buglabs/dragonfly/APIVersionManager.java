/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly;

public class APIVersionManager {

	public static final String BUG_API_VERSION_MANIFEST_KEY = "BUG-API-Version";

	/**
	 * This will only be set on some sort of error. This is the first version to
	 * contain this functionality
	 */
	private static final String DEFAULT_API_VERSION = "1.4.3";
	private static String sdk_api_version = null;

	public static String getSDKAPIVersion() {
		if (sdk_api_version == null) {
			synchronized (APIVersionManager.class) {
				if (sdk_api_version == null) {
					sdk_api_version = getSDKAPIVersionFromManifest();
				}
			}
		}

		if (sdk_api_version == null || sdk_api_version.length() == 0)
			sdk_api_version = DEFAULT_API_VERSION;

		return sdk_api_version;
	}

	private static String getSDKAPIVersionFromManifest() {
		return (String) DragonflyActivator.getDefault().getBundle().getHeaders().get(BUG_API_VERSION_MANIFEST_KEY);
	}

}
