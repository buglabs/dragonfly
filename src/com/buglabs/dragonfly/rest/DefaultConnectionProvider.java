/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * default connection provider used by HTTPRequest if none is provided
 * 
 * @author Brian
 * 
 */
public class DefaultConnectionProvider implements IConnectionProvider {

	public HttpURLConnection getConnection(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		return connection;
	}

}
