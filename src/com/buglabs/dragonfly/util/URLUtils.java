/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.AuthenticationData;

/**
 * A set of static helper classes to create BUGNet URLS. All URL creating should
 * be defined in this class.
 * 
 * @author ken
 * 
 */
public class URLUtils {

	/**
	 * Get loging URL to bugnet.
	 * 
	 * @param baseUrl
	 * @param frob
	 * @return
	 * @throws MalformedURLException
	 */
	public static URL getLoginUrl(String baseUrl, String frob) throws MalformedURLException {
		return new URL(baseUrl + "user/login?frob=" + frob + "&source=ide");
	}

	/**
	 * @param host
	 * @return Returns network interface address.
	 */
	public static String getNetworkIterfaceAddress(String host) {
		String nicIP = "";
		try {
			Enumeration ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				Enumeration addresses = iface.getInetAddresses();

				while (addresses.hasMoreElements()) {
					InetAddress address = (InetAddress) addresses.nextElement();
					if (address instanceof Inet4Address) {
						nicIP = address.toString().substring(1);
						if (nicIP.equals(host)) {
							return nicIP;
						}
					}
				}
			}
		} catch (SocketException e) {
			return null;
		}
		return nicIP;
	}

	
	/**
	 * 
	 * Handles, generally, adding a property to the querystring of a URL.
	 * 
	 * @param url
	 * @param key
	 * @param value
	 * @return
	 * @throws MalformedURLException
	 */
	public static URL addPropertyToURL(String url, String key, String value) throws MalformedURLException {
		String urlStr = url.toString();
		// if url does not contain ?, add it. Otherwise use &
		String par = "&";
		if(urlStr.indexOf("?") == -1)
			par = "?";
		urlStr += par + key + "=" + value; //$NON-NLS-1$
		return new URL(urlStr);
	}
	
	/**
	 * @param url
	 * @return Returns data from the server with a given url
	 * @throws IOException
	 */
	public static String getDataFromServer(URL url) throws IOException {
		java.net.URLConnection conn = url.openConnection();
		StringBuffer sb = new StringBuffer();
		conn.setDoInput(true);
		conn.setDoOutput(false);

		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			if (line.trim().length() > 0) {
				sb.append(line);
			}
		}

		rd.close();

		return sb.toString();
	}
	
	/**
	 * Reads data from specified stream
	 * @param stream - Stream from which data should be read
	 * @return Result from server response
	 * @throws IOException
	 */
	public static String readFromStream(InputStream stream) throws IOException {
		StringBuffer sb = null;
		BufferedReader in = null;
		
		in = new BufferedReader(new InputStreamReader(stream));
		
		String line;
		sb = new StringBuffer();
		while ((line = in.readLine()) != null) {
			sb.append(line);
		}
		
		in.close();
		
		return sb.toString();
	}
}
