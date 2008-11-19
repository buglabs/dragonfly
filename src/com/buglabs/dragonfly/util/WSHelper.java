/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.eclipse.swt.graphics.ImageData;

import com.buglabs.util.XmlNode;
import com.buglabs.util.XmlParser;

public class WSHelper {

	/**
	 * Gets data from {@link URLConnection}
	 * @param conn
	 * @param isBug specifies whether url refers to the BUG
	 * @return
	 * @throws IOException
	 */
	protected static String get(URLConnection conn, boolean isBug) throws IOException {
		return get(conn.getURL(), isBug);
	}

	public static ImageData getImage(URL url) throws IOException {

		InputStream is = getAsStream(url);
		byte[] buf = new byte[1024];
		int read = 0;

		DynamicByteBuffer dynBuf = new DynamicByteBuffer();
		while ((read = is.read(buf)) > 0) {
			for (int i = 0; i < read; ++i) {
				dynBuf.append(buf[i]);
			}
		}

		ImageData id = new ImageData(new ByteArrayInputStream(dynBuf.toArray()));

		return id;
	}

	protected static String get(String url, String token) throws IOException {
		HttpClient c = new HttpClient();

		GetMethod m = new GetMethod(url);
		m.setRequestHeader("Cookie", "token=" + token);

		c.executeMethod(m);

		return m.getResponseBodyAsString();
	}

	/**
	 * Gets data from {@link URL}
	 * @param url
	 * @param isBug specifies whether url refers to the BUG
	 * @return
	 * @throws IOException
	 */
	protected static String get(URL url, boolean isBug) throws IOException {
		if(isBug){
			return URLUtils.readFromStream(url.openStream());
		}
		// commented out line below because this method never calls BUGnet
		//  and the token is being phased out -BB 8/21/08
		//URL urlWithToken = URLUtils.appendTokenToURL(url.toString());
		return SSLUtils.getData(url);
	}

	protected static InputStream getAsStream(URL url) throws IOException {
		URLConnection conn = url.openConnection();

		conn.setDoInput(true);
		conn.setDoOutput(false);

		return conn.getInputStream();
	}

	protected static String post(URL url, Map props) throws IOException {
		String propstr = new String();

		for (Iterator i = props.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			propstr = propstr + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode((String) props.get(key), "UTF-8");
			if (i.hasNext()) {
				propstr = propstr + "&";
			}
		}

		SSLUtils.verifyHost();
		
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);

		OutputStreamWriter osr = new OutputStreamWriter(conn.getOutputStream());
		osr.write(propstr);
		osr.flush();

		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line, resp = new String("");
		while ((line = rd.readLine()) != null) {
			resp = resp + line + "\n";
		}
		osr.close();
		rd.close();

		return resp;
	}

	/**
	 * Post contents of input stream to URL.
	 * 
	 * @param url
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected static String post(URL url, InputStream stream) throws IOException {
		URLConnection conn = url.openConnection();
		// /conn.setRequestProperty("Content-Type", "application/java-archive");
		conn.setDoOutput(true);

		pipe(stream, new BufferedOutputStream(conn.getOutputStream()));

		stream.close();
		conn.getOutputStream().flush();
		conn.getOutputStream().close();

		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line, resp = new String("");
		while ((line = rd.readLine()) != null) {
			resp = resp + line + "\n";
		}
		rd.close();

		return resp;
	}

	/**
	 * Post contents of input stream to URL.
	 * 
	 * @param url
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected static String postBase64(URL url, InputStream stream) throws IOException {
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);

		byte[] buff = streamToByteArray(stream);

		String em = Base64.encodeBytes(buff);
		OutputStreamWriter osr = new OutputStreamWriter(conn.getOutputStream());
		osr.write(em);
		osr.flush();

		stream.close();
		conn.getOutputStream().flush();
		conn.getOutputStream().close();

		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line, resp = new String("");
		while ((line = rd.readLine()) != null) {
			resp = resp + line + "\n";
		}
		rd.close();

		return resp;
	}

	public static int pipe(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[4096];
		int nread;
		int total = 0;

		synchronized (in) {
			while ((nread = in.read(buf)) > 0) {
				out.write(buf, 0, nread);
				total += nread;
			}
		}
		out.flush();
		buf = null;

		return total;
	}

	/*
	 * public byte[] getImage() { InputStream pic = getImageInputStream();
	 * 
	 * ByteArrayOutputStream os = new ByteArrayOutputStream();
	 * 
	 * int read = 0; byte[] buff = new byte[4096];
	 * 
	 * try { while((read = pic.read(buff)) > 0) { os.write(buff, 0, read); } }
	 * catch (IOException e1) { // TODO Auto-generated catch block
	 * e1.printStackTrace(); }
	 * 
	 * return os.toByteArray(); }
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return os.toByteArray();
	}

	protected static String post(URL url, String payload, Map props) throws IOException {
		String propstr = new String();

		for (Iterator i = props.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			propstr = propstr + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode((String) props.get(key), "UTF-8");
			if (i.hasNext()) {
				propstr = propstr + "&";
			}
		}

		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);

		OutputStreamWriter osr = new OutputStreamWriter(conn.getOutputStream());
		osr.write(propstr);
		osr.flush();

		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line, resp = new String("");
		while ((line = rd.readLine()) != null) {
			resp = resp + line + "\n";
		}
		osr.close();
		rd.close();

		return resp;
	}

	/**
	 * Refer to
	 * http://lurcher/wiki/index.php/IDE_Web_Service_Interface#Using_a_Token
	 * 
	 * @param url
	 * @param token
	 * @param payload
	 * @return
	 * @throws IOException
	 */
	protected static String post(String url, String token, String payload) throws IOException {
		HttpClient c = new HttpClient();

		PostMethod m = new PostMethod(url);
		m.setRequestHeader("Cookie", "token=" + token);
		m.setRequestEntity(new StringRequestEntity(payload));
		c.executeMethod(m);

		return m.getResponseBodyAsString();
	}
	
	/**
	 * @param url
	 * @param payload
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	protected static String put(String url, String payload) throws HttpException, IOException {
		HttpClient c = new HttpClient();

		PutMethod m = new PutMethod(url);
		m.setRequestEntity(new StringRequestEntity(payload));
		c.executeMethod(m);

		return m.getResponseBodyAsString();
	}

	protected static String putBase64(String url, FileInputStream stream) throws HttpException, IOException {
		HttpClient c = new HttpClient();

		PutMethod m = new PutMethod(url);
		byte[] buff = streamToByteArray(stream);

		String em = Base64.encodeBytes(buff);

		m.setRequestEntity(new StringRequestEntity(em));
		c.executeMethod(m);

		return m.getResponseBodyAsString();
	}
	
	protected static String delete(String url) throws HttpException, IOException{
		HttpClient c = new HttpClient();
		DeleteMethod delete = new DeleteMethod(url);
		c.executeMethod(delete);
		
		return delete.getResponseBodyAsString();
	}

	/**
	 * Check response string from http request for error message.
	 * 
	 * @param response
	 * @throws IOException
	 */
	public static void checkForError(String response) throws IOException {
		if (response.startsWith("<html><head><title>404 Not Found")) {
			throw new IOException(response);
		}
	}

	public WSHelper() {
		super();
	}

}