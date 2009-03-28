/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.BugProperty;
import com.buglabs.dragonfly.model.DeleteServiceNode;
import com.buglabs.dragonfly.model.GetServiceNode;
import com.buglabs.dragonfly.model.IPackage;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.Module;
import com.buglabs.dragonfly.model.PostServiceNode;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.model.PutServiceNode;
import com.buglabs.dragonfly.model.ServiceNode;
import com.buglabs.util.XmlNode;
import com.buglabs.util.XmlParser;

/**
 * Web Service Access helper methods for accessing BUG from IDE.
 * 
 * @author ken
 * 
 */
public class BugWSHelper extends WSHelper {
	
	public static Map getModulePropertyDescriptorMap(Module module) throws IOException {
		// TODO: refactor this code to take advantage of XPath query.
		XmlParser parser = new XmlParser();
		Map props = new HashMap();

		URLConnection conn = module.getUrl().openConnection();

		String response = get(conn,true);

		XmlNode root = parser.parse(new StringReader(response));

		for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
			XmlNode e = (XmlNode) i.next();
			BugProperty p = new BugProperty(e.getAttribute("name"));
			p.setMutable(Boolean.valueOf(e.getAttribute("mutable")).booleanValue());
			p.setValue(e.getAttribute("value"));
			p.setType(e.getAttribute("type"));

			props.put(p.getName(), p);
		}

		return props;
	}

	public static List getModulePropertyDescriptors(Module module) throws IOException {
		XmlParser parser = new XmlParser();
		List props = new ArrayList();

		URLConnection conn = module.getUrl().openConnection();

		String response = get(conn,true);

		XmlNode root = parser.parse(new StringReader(response));

		for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
			XmlNode e = (XmlNode) i.next();
			props.add(xmlToIPropertyDescriptor(e));
		}

		return props;
	}

	private static IPropertyDescriptor xmlToIPropertyDescriptor(XmlNode e) {

		return new TextPropertyDescriptor(e.getAttribute("Name"), e.getAttribute("Name"));
	}

	/**
	 * Given a bug url, return a list of modules.
	 * 
	 * @param parent
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static List getModuleList(ITreeNode parent, URL u) throws IOException {
		XmlParser parser = new XmlParser();
		List modules = new ArrayList();

		URLConnection conn = u.openConnection();

		// conn.setConnectTimeout(TIMEOUT);

		String response = get(conn,true);

		XmlNode root = parser.parse(new StringReader(response));

		for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
			XmlNode e = (XmlNode) i.next();
			URL url = new URL(u.toExternalForm() + "/" + e.getAttribute("index"));
			// String s = e.getAttribute("Slot");
			// int slot = Integer.parseInt(e.getAttribute("Slot"));
			modules.add(new Module(parent, url, e.getAttribute("name"), Integer.parseInt(e.getAttribute("index"))));
		}

		return modules;
	}
	
	public static List getRawModules(URL url) throws IOException{
		XmlParser parser = new XmlParser();
		List modules = new ArrayList();

		URLConnection conn = url.openConnection();

		// conn.setConnectTimeout(TIMEOUT);

		String response = get(conn,true);

		XmlNode root = parser.parse(new StringReader(response));

		for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
			XmlNode e = (XmlNode) i.next();
			modules.add(e.getAttribute("name"));
		}

		return modules;
	}

	public static void updateProperty(Module module, BugProperty property, Object value) throws IOException {

		String data = URLEncoder.encode(property.getName(), "UTF-8") + "=" + URLEncoder.encode(value.toString(), "UTF-8");

		// Send data
		URLConnection conn = module.getUrl().openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();
		// wr.close();

		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		// String line;
		// while ((line = rd.readLine()) != null) {
		// System.out.println("Response: " + line);
		// }
		wr.close();
		rd.close();

	}

	/**
	 * Get a list of all the BUG bundles running on the bug.
	 * 
	 * @param programUrl
	 * @param serializer
	 * @return List of type IPackage
	 * @throws Exception
	 */
	public static List getPrograms(URL programUrl) throws Exception {
		XmlParser parser = new XmlParser();
		List pkgs = new ArrayList();

		ISerializer serializer = SerializerFactory.getSerializer("xml");

		URLConnection conn = programUrl.openConnection();

		// conn.setConnectTimeout(TIMEOUT);

		String response = get(conn,true);
		
		XmlNode root = parser.parse(new StringReader(response));

		for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
			XmlNode e = (XmlNode) i.next();

			IPackage pkg = serializer.getDeserializedPackage(e.toString());

			pkgs.add(new ProgramNode(pkg, new URL(programUrl + "/" + e.getAttribute("id"))));

		}

		return pkgs;
	}

	public static List getServices(URL serviceURL) throws IOException {
		XmlParser parser = new XmlParser();
		List services = new ArrayList();

		URLConnection conn = serviceURL.openConnection();

		String response = get(conn,true);

		XmlNode root = parser.parse(new StringReader(response));

		for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
			XmlNode node = (XmlNode) i.next();

			for (Iterator i2 = node.getChildren().iterator(); i2.hasNext();) {
				XmlNode operationsNode = (XmlNode) i2.next();
				ServiceNode sn;
				String httpOperation = operationsNode.getName().toUpperCase();
				if (httpOperation.equals("GET")) {
					sn = new GetServiceNode(operationsNode, serviceURL.toExternalForm());
				} else if (httpOperation.equals("PUT")) {
					sn = new PutServiceNode(operationsNode, serviceURL.toExternalForm());
				} else if (httpOperation.equals("DELETE")) {
					sn = new DeleteServiceNode(operationsNode, serviceURL.toExternalForm());
				} else if (httpOperation.equals("POST")) {
					sn = new PostServiceNode(operationsNode, serviceURL.toExternalForm());
				} else {
					throw new InvalidParameterException("Unknown or invalid HTTP operation: " + httpOperation);
				}
				services.add(sn);
			}
		}
		return services;
	}
	
	public static List getPackages(URL packageURL) throws IOException{
		XmlParser parser = new XmlParser();
		
		List packages = new ArrayList();

		URLConnection conn = packageURL.openConnection();

		String response = get(conn,true);
		
		XmlNode root = parser.parse(new StringReader(response));
		
		for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
			XmlNode node = (XmlNode) i.next();
			
			String packageName = node.getAttribute("name");
			if (packageName != null) {
			    packages.add(packageName.trim());
			}
		}
		
		return packages;
	}

	public static String updatePackage(ProgramNode pkn, ISerializer serializer) throws Exception {
		if (!pkn.isBUGNetPackage()) {
			throw new Exception("This package did not originate on BUGnet.");
		}

		String pkgStr = serializer.getSerializedPackage(pkn.getPackage());
		Map map = new Hashtable();

		map.put("Package", pkgStr);
		map.put("update", "");

		return post(pkn.getPackageUrl(), map);
	}

	public static String insertPackage(ProgramNode pkn, ISerializer serializer) throws Exception {
		if (!pkn.isBUGNetPackage()) {
			throw new Exception("This package did not originate on BUGnet.");
		}

		String pkgStr = serializer.getSerializedPackage(pkn.getPackage());
		Map map = new Hashtable();

		map.put("Package", pkgStr);

		return post(pkn.getPackageUrl(), map);
	}

	public static InputStream getProgramJar(ProgramNode pn) throws Exception {

		return getAsStream(pn.getPackageUrl());
	}

	public static String insertPackage(IPackage pkn, ISerializer serializer, URL bugPackageURL) throws Exception {
		String pkgStr = serializer.getSerializedPackage(pkn);
		Map map = new Hashtable();

		map.put("Package", pkgStr);

		return post(bugPackageURL, map);
	}

	public static void subscribeToBug(Bug bug) throws Exception {
		String surl = bug.getEventURL().toExternalForm();

		String[] ipAddresses = getIpAddresses();

		for (int i = 0; i < ipAddresses.length; ++i) {
			XmlNode sublist = new XmlNode("subscribers");

			XmlNode sub = new XmlNode(sublist, "subscriber");
			sub.addAttribute("url", "http://" + ipAddresses[i] + ":" + DragonflyActivator.MODEL_CHANGE_EVENT_LISTEN_PORT);
			sub.addAttribute("topic", "com/buglabs/event/module");

			put(surl, sublist.toString());
		}
	}

	private static String[] getIpAddresses() throws SocketException {
		List addresses = new ArrayList();

		Enumeration e = NetworkInterface.getNetworkInterfaces();

		while (e.hasMoreElements()) {
			NetworkInterface netface = (NetworkInterface) e.nextElement();

			Enumeration e2 = netface.getInetAddresses();

			while (e2.hasMoreElements()) {
				InetAddress ip = (InetAddress) e2.nextElement();
				if (isValidIpAddress(ip)) {
					addresses.add(ip.toString().replaceFirst("/", ""));
				}
			}
		}

		return (String[]) addresses.toArray(new String[addresses.size()]);
	}

	/**
	 * Determine if IP address is valid for BUG to use for event notification.
	 * 
	 * @param ip
	 * @return
	 */
	private static boolean isValidIpAddress(InetAddress ip) {
		if (ip instanceof Inet6Address) {
			return false;
		}

		// loopback
		if (ip.getAddress()[0] == 127) {
			return false;
		}

		return true;
	}

	/**
	 * Insert or update a BUG application on BUG.
	 * @param jarFile
	 * @param url
	 * @param encode if true, encode jar with base64
	 * @return
	 * @throws IOException
	 */
	public static String upsertBundle(File jarFile, URL url, boolean encode) throws IOException {
		if (encode) {
			return postBase64(url, new FileInputStream(jarFile));
		} 
		
		return post(url, new FileInputStream(jarFile));
	}

	public static List getAllServices(URL bugURL) throws Exception {
		List programs = getPrograms(bugURL);
		List services = new ArrayList();

		Iterator progIter = programs.iterator();

		while (progIter.hasNext()) {
			ProgramNode pn = (ProgramNode) progIter.next();
			List progServices = pn.getPackage().getEventURIs();
			Iterator progServicesIter = progServices.iterator();
			while (progServicesIter.hasNext()) {
				String serviceName = (String) progServicesIter.next();
				if (!services.contains(serviceName)) {
					services.add(serviceName);
				}
			}
		}

		return services;
	}

	/**
	 * @param bugURL
	 *            URL for this BUG
	 * @param monitor
	 *            Monitor to show progress of services retrieval
	 * @return Returns list of services for this BUG
	 * @throws Exception
	 */
	public static List getAllServices(URL bugURL, IProgressMonitor monitor) throws Exception {
		List programs = getPrograms(bugURL);
		List services = new ArrayList();

		Iterator progIter = programs.iterator();

		while (progIter.hasNext()) {
			ProgramNode pn = (ProgramNode) progIter.next();
			List progServices = pn.getPackage().getEventURIs();
			Iterator progServicesIter = progServices.iterator();

			int i = 0;
			while (progServicesIter.hasNext()) {
				String serviceName = (String) progServicesIter.next();
				if (!services.contains(serviceName)) {
					services.add(serviceName);
					monitor.worked(i);
					monitor.subTask("Getting Service: " + serviceName);
					i++;
				}
			}
		}
		monitor.done();
		return services;
	}
	
	public static String deleteProgram(String url) throws Exception{
		return delete(url);
	}
	
	/**
	 * 
	 * @param url {@link URL} pointing to BUG's /configAdmin
	 * @return XML representing BUG's current state
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String getConfigurationProprtiesAsXml(URL url) throws MalformedURLException, IOException{
		URLConnection conn = url.openConnection();
		return get(conn,true);
	}
	
	/**
	 * Updates BUG's configuration
	 * @param url {@link URL} of BUG's /configAdmin
	 * @param key key to be updated
	 * @param value new value for the key
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void setConfigurationProperty(URL url, String payload) throws MalformedURLException, IOException{
		 put(url.toString(), payload);
	}
}