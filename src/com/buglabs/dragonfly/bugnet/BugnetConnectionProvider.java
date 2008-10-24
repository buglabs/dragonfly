/*******************************************************************************
 * Copyright (c) 2006, 2007, 2008 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.bugnet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.buglabs.dragonfly.model.AuthenticationData;
import com.buglabs.dragonfly.rest.IConnectionProvider;
import com.buglabs.dragonfly.util.SSLUtils;
import com.buglabs.util.Base64;

/**
 * 
 * Helps BugnetWSHelper communicate w/ web service by setting up
 * header variables used for each connection, namely authentication
 * 
 * @author brian
 *
 */
public class BugnetConnectionProvider implements IConnectionProvider {
	
	String credentials = "";

	
	public BugnetConnectionProvider() {
		AuthenticationData data = BugnetStateProvider.getInstance().getAuthenticationData();
		setCredentials(data.getUsername(), data.getPassword());
	}
	
	public BugnetConnectionProvider(String username, String password) {
		setCredentials(username, password);
	}

	public HttpURLConnection getConnection(String urlStr) throws IOException {
		SSLUtils.verifyHost();
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Authorization", "Basic " + credentials);		
		return connection;
	}
	
	private void setCredentials(String username, String password) {
		String rawCreds = username + ":" + password;
		credentials = Base64.encodeBytes(rawCreds.getBytes());		
	}
		
}
