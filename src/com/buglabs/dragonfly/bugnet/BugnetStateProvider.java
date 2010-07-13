/*******************************************************************************
 * Copyright (c) 2006, 2007, 2008 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.bugnet;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.graphics.Image;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.AuthenticationData;

/**
 * This class provides a layer of indirection between the WS API and the
 * Dragonfly context
 * 
 * using setInstance, we can use mock objects for testing.
 * 
 * @author Brian
 * 
 */
public class BugnetStateProvider {

	private static BugnetStateProvider _instance = null;

	protected BugnetStateProvider() {
	}

	public synchronized static BugnetStateProvider getInstance() {
		if (_instance == null) {
			_instance = new BugnetStateProvider();
		}
		return _instance;
	}

	/**
	 * Only use this in testing to set mock object
	 * 
	 * @param provider
	 */
	public static void setInstance(BugnetStateProvider provider) {
		_instance = provider;
	}

	public String getBugnetURL() {
		return DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_PROTOCOL)
				+ DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_SERVER_NAME);
	}

	public Image getImageFromRegistry(String location) {
		return DragonflyActivator.getDefault().getImageRegistry().get(location);
	}

	public void putImageInRegistry(String location, Image image) {
		DragonflyActivator.getDefault().getImageRegistry().put(location, image);
	}

	public AuthenticationData getAuthenticationData() {
		return DragonflyActivator.getDefault().getAuthenticationData();
	}

	public int getDefaultApplicationCount() {
		return DragonflyActivator.getDefault().getPluginPreferences().getInt(DragonflyActivator.PREF_BUGNET_NUM_OF_APPS);
	}

	public Preferences getPreferences() {
		return DragonflyActivator.getDefault().getPluginPreferences();
	}

}
