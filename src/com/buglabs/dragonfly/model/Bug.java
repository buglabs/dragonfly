/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * A model class that represents a bug device.
 * 
 * @author ken
 * 
 */
public class Bug extends BaseTreeNode {
	private static final long serialVersionUID = -6726829616562590688L;
	
	/*
	 * Contants for the BUG version.
	 */
	public final static int BUG_R14 = 1;
	public final static int BUG_PRE_R14 = 2;
	public final static int BUG_UNKNOWN = 0;

	private URL baseUrl;

	private URL moduleUrl;

	private URL programUrl;

	private URL serviceUrl;

	private boolean connected = false;

	private URL eventUrl;
	
	private URL packageURL;

	private URL configAdminUrl;
	
	private int version;

	public Bug(String name, URL url) {
		super(name);
		this.baseUrl = url;

		// this.authData = new AuthenticationData(name);
	}

	public Collection getChildren() {
		if (!connected) {
			// Cleanup from XStream serialization.
			super.getChildren().clear();
			try {
				new ModuleFolderNode(this);
				new ApplicationFolderNode(this);
				new ServiceFolderNode(this);
				version = getBUGVersion();
			} catch (Exception e) {
				UIUtils.handleNonvisualError("Unable to load BUG.", e);
				return super.getChildren();
			}
		}

		return super.getChildren();
	}

	/**
	 * Examine running bundles on BUG to determine version of rootfs.
	 * @return An integer indicating the version of BUG rootfs software running on device.
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	private int getBUGVersion() throws MalformedURLException, Exception {
		List pkgs = BugWSHelper.getPrograms(this.getProgramURL());
		
		for (Iterator i = pkgs.iterator(); i.hasNext();) {
			IPackage pkg = (IPackage) i.next();
			
			//Somewhat of a hack.  If we find the BUG Audio bundle, we assume it's R1.4
			if (pkg.getName().toUpperCase().equals("BUG AUDIO")) {
				return BUG_R14;
			}
		}
		
		return BUG_PRE_R14;
	}

	/**
	 * @return A URL for the module ws api.
	 * @throws MalformedURLException
	 */
	public URL getModuleURL() throws MalformedURLException {
		if (moduleUrl == null) {
			// Make a URL to get the module list
			String moduleurl = baseUrl.getProtocol() + "://" + baseUrl.getHost() + ":" + baseUrl.getPort() + baseUrl.getPath() + "/module";
			moduleUrl = new URL(moduleurl);
		}

		return moduleUrl;
	}

	/**
	 * @return A URL for the Program WS API on the BUG.
	 * @throws MalformedURLException
	 */
	public URL getProgramURL() throws MalformedURLException {
		if (programUrl == null) {
			// Make a URL to get the program list
			String moduleurl = baseUrl.getProtocol() + "://" + baseUrl.getHost() + ":" + baseUrl.getPort() + baseUrl.getPath() + "/program";
			programUrl = new URL(moduleurl);
		}

		return programUrl;
	}

	public URL getServiceURL() throws MalformedURLException {
		if (serviceUrl == null) {
			// Make a URL to get the module list
			String moduleurl = baseUrl.getProtocol() + "://" + baseUrl.getHost() + ":" + baseUrl.getPort() + baseUrl.getPath() + "/service";
			serviceUrl = new URL(moduleurl);
		}

		return serviceUrl;
	}

	/**
	 * @return A URL for the Event API on the BUG.
	 * @throws MalformedURLException
	 */
	public URL getEventURL() throws MalformedURLException {
		if (eventUrl == null) {
			String surl = baseUrl.getProtocol() + "://" + baseUrl.getHost() + ":" + baseUrl.getPort() + baseUrl.getPath() + "/event";

			eventUrl = new URL(surl);
		}

		return eventUrl;
	}
	
	/**
	 * @return A URL for packages on the BUG.
	 * @throws MalformedURLException
	 */
	public URL getPackageURL() throws MalformedURLException{
		if (packageURL == null) {
			String surl = baseUrl.getProtocol() + "://" + baseUrl.getHost() + ":" + baseUrl.getPort() + baseUrl.getPath() + "/package";

			packageURL = new URL(surl);
		}

		return packageURL;
	}
	
	/**
	 * @return ConfigurationAdmin url
	 * @throws MalformedURLException
	 */
	public URL getConfigAdminURL() throws MalformedURLException{
		if (configAdminUrl == null) {
			String surl = baseUrl.getProtocol() + "://" + baseUrl.getHost() + ":" + baseUrl.getPort() + baseUrl.getPath() + "/configuration";

			configAdminUrl = new URL(surl);
		}

		return configAdminUrl;
	}

	public void disconnect() {
		connected = false;
		moduleUrl = null;
		programUrl = null;
		serviceUrl = null;
		packageURL = null;
		configAdminUrl = null;
	}

	public URL getUrl() {
		return baseUrl;
	}

	public void setUrl(URL url) {
		this.baseUrl = url;
		eventUrl = null;
		moduleUrl = null;
		programUrl = null;
		serviceUrl = null;
		packageURL = null;
		configAdminUrl = null;
	}
	
	/**
	 * @return Version of BUG.
	 */
	public int getVersion() {
		return version;
	}

	public Object getEditableValue() {
		return null;
	}

	public boolean isPropertySet(Object id) {
		return false;
	}

	public void resetPropertyValue(Object id) {
	}

	public void setPropertyValue(Object id, Object value) {
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}
