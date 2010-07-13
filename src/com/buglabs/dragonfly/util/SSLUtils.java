/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/

/**
 *Created on Nov 6, 2007 by akravets
 */
package com.buglabs.dragonfly.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.buglabs.dragonfly.DragonflyActivator;

/**
 * A set of static helper classes to connect to HTTPS server
 * 
 * @author akravets
 */
public class SSLUtils {
	/**
	 * Returns data from the server with a give <code>URL</code>
	 * 
	 * @param url
	 * @return data from the server
	 * @throws IOException
	 * @throws IOException
	 * @throws IOException
	 * @throws IOException
	 */
	public static String getData(URL url) throws IOException {
		String data = null;
		// try ro read from the stream
		try {
			data = URLUtils.readFromStream(url.openStream());
		} catch (IOException e) {
			// unable to read from the stream, check if url is https, if so handle the case. Otherwise give up.
			if (url.toString().startsWith(DragonflyActivator.HTTPS)) {
				return SSLUtils.handleException(url);
			}
			throw e;
		}
		return data;
	}

	/**
	 * Returns data from the server
	 * 
	 * @param conn
	 * @return Returns <code>InputStream</code>
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static InputStream getDataAsStream(URLConnection conn) throws MalformedURLException, IOException {
		InputStream is = null;

		try {
			is = conn.getInputStream();
		} catch (IOException e) {
			URLConnection con = convertToHTTP(conn.getURL()).openConnection();
			return con.getInputStream();
		}
		return is;
	}

	public static String getSSLData(URLConnection conn) throws MalformedURLException, IOException {
		return URLUtils.readFromStream(getDataAsStream(conn));
	}

	private static String handleException(URL url) throws IOException, MalformedURLException {
		//DragonflyActivator.getDefault().setProtocol(DragonflyActivator.HTTP);

		// connect with new url
		return URLUtils.getDataFromServer(convertToHTTP(url));
	}

	/**
	 * Given <code>URL</code> of type "https://" will convert to "http://"
	 * 
	 * @param url
	 * @return Newly constructed <code>URL</code>
	 * @throws MalformedURLException
	 */
	public static URL convertToHTTP(URL url) throws MalformedURLException {
		return new URL(DragonflyActivator.HTTP + url.getHost() + url.getFile());
	}

	/**
	 * Constructs secure socket
	 * 
	 * @param host
	 *            name of the host to connect to
	 * @return Secure <code>Socket</code>
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static SSLSocket getSecureSocket(String host) throws UnknownHostException, IOException {
		SocketFactory factory = SSLSocketFactory.getDefault();
		return (SSLSocket) factory.createSocket(host, DragonflyActivator.HTTPS_PORT);
	}

	/**
	 * Verifies host's identity using <code>HostnameVerifier</code>
	 * 
	 */
	public static void verifyHost() {
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
				try {
					return java.net.InetAddress.getByName(urlHostName).equals(java.net.InetAddress.getByName(session.getPeerHost()));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("UnknownHostException Exception encountered", e);
				}
			}
		};

		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}
}
