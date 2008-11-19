/*******************************************************************************
 * Copyright (c) 2006, 2007, 2008 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.bugnet;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
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
		// Handle authentication retrieval from outside java URL framework
		// so set default authenticator to one that just returns null
		// TODO - move existing authentication stuff into custom authenticator
		Authenticator.setDefault(new NullAuthenticator());
		connection.setRequestProperty("Authorization", "Basic " + credentials);		
		return connection;
	}
	
	private void setCredentials(String username, String password) {
		String rawCreds = username + ":" + password;
		credentials = Base64.encodeBytes(rawCreds.getBytes());		
	}
	
	/**
	 * Set this authenticator to return null on getPasswordAuthentication()
	 * This keeps the username password dialog from appearing from within the URL framework
	 * 
	 * TODO Rewrite authentication code to use a custom authenticator that shows a dialog.
	 * 		This would be cleaner than what we're doing now
	 * 
	 * @author brian
	 *
	 */
	private class NullAuthenticator extends Authenticator {
		protected PasswordAuthentication getPasswordAuthentication() {
			return null;
		}
	}
		
}
