/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm.service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.buglabs.dragonfly.dm.BUGResources;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.util.BugWSHelper;

import org.eclipse.rse.services.AbstractService;

public class BUGOSGiBundleService extends AbstractService implements IBUGOSGiBundleService {

	private BugConnection connection;
	private List bundles;

	public String getName() {
		return BUGResources.BUG_Service_Name;
	}

	public String getDescription() {
		return BUGResources.BUG_Service_Description;
	}

	public void setConnection(BugConnection connection) {
		this.connection = connection;
	}

	@Override
	public Object[] getBundles() {

		return bundles.toArray();
	}

	public void disconnect() {
		connection.disconnect();
	}

	public void loadBundles() throws MalformedURLException, Exception {
		List l = BugWSHelper.getPrograms(connection.getProgramURL());
		if (bundles == null) {
			bundles = new ArrayList();
		} else {
			bundles.clear();
		}

		for (Iterator i = l.iterator(); i.hasNext();) {
			bundles.add(new ProgramNodeAdapter((ProgramNode) i.next()));
		}
	}

}
