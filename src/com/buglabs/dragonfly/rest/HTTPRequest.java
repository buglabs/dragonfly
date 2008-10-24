/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import com.buglabs.dragonfly.util.Base64;

/**
 * 
 * class for dealing RESTfully with HTTP Requests
 * 
 * 
 * @author Brian
 *
 */
public class HTTPRequest {
	////////////////////////////////////////////////  HTTP REQUEST METHODS	
	
	private IConnectionProvider _connectionProvider;
	
	/**
	 * constructor where client provides connectionProvider
	 */
	public HTTPRequest(IConnectionProvider connectionProvider) {
		_connectionProvider = connectionProvider;
	}
	
	/**
	 * constructor that uses default connection provider
	 */
	public HTTPRequest() {
		_connectionProvider = new DefaultConnectionProvider();
	}
	
    /**
     * Do an authenticated HTTP GET from url
     * 
     * @param url   String URL to connect to
     * @return      HttpURLConnection ready with response data
     */
	public HTTPResponse get(String url) throws IOException {
		HttpURLConnection conn = _connectionProvider.getConnection(url);
		conn.setDoInput(true);
		conn.setDoOutput(false);
		return connect(conn);
	}
	
    /**
     * Do an authenticated HTTP POST to url
     * 
     * @param url   String URL to connect to
     * @param data  String data to post 
     * @return      HttpURLConnection ready with response data
     */
	public HTTPResponse post(String url, String data) throws IOException {
		HttpURLConnection conn = _connectionProvider.getConnection(url);
		conn.setDoOutput(true);
		OutputStreamWriter osr = new OutputStreamWriter(conn.getOutputStream());
		osr.write(data);
		osr.flush();
		osr.close();
		return connect(conn);
	}

    /**
     * Do an authenticated HTTP POST to url
     * 
     * @param url       String URL to connect to
     * @param stream    InputStream data to post 
     * @return          HttpURLConnection ready with response data
     */
	public HTTPResponse post(String url, InputStream stream) throws IOException {
		byte[] buff = streamToByteArray(stream);
		String data = Base64.encodeBytes(buff);
		return post(url, data);
	}	
	
	/**
	 * Do an authenticated HTTP PUT to url
	 * 
	 * @param url  String URL to connect to
	 * @param data String data to post 
	 * @return     HttpURLConnection ready with response data
	 */
	public HTTPResponse put(String url, String data) throws IOException {
		HttpURLConnection connection = _connectionProvider.getConnection(url);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		OutputStreamWriter osr = new OutputStreamWriter(connection.getOutputStream());
		osr.write(data);
		osr.flush();
		osr.close();
		return connect(connection);
	}

	/**
     * Do an authenticated HTTP PUT to url
     * 
     * @param url       String URL to connect to
     * @param stream    InputStream data to put 
     * @return          HttpURLConnection ready with response data
     */	
	public HTTPResponse put(String url, InputStream stream) throws IOException {
		byte[] buff = streamToByteArray(stream);
		String data = Base64.encodeBytes(buff);
		return put(url, data);		
	}	
	

    /**
     * Do an authenticated HTTP HEAD to url
     * 
     * @param url       String URL to connect to 
     * @return          HttpURLConnection ready with response data
     */ 
	public HTTPResponse head(String url) throws IOException {
		HttpURLConnection connection = _connectionProvider.getConnection(url);
		connection.setDoOutput(true);
		connection.setRequestMethod("HEAD");
		return connect(connection);
	}
	
	
	////////////////////////////////////////////////////////////// THESE HELP

    
	/**
	 * Connect to server, check the status, and return the new HTTPResponse
	 */
	private HTTPResponse connect(HttpURLConnection connection) throws HTTPException, IOException {
		HTTPResponse response = new HTTPResponse(connection);
		response.checkStatus();
		return response;
	}
	

    /**
     * A simple helper function
     * 
     * @param in    InputStream to turn into a byte array 
     * @return      byte array (byte[]) w/ contents of input stream
     */ 
	public static byte[] streamToByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int read = 0;
		byte[] buff = new byte[4096];
		try {
			while ((read = in.read(buff)) > 0) {
				os.write(buff, 0, read);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return os.toByteArray();
	}	

}
