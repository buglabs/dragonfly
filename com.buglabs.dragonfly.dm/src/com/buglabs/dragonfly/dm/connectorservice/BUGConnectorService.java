/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm.connectorservice;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.buglabs.dragonfly.dm.BUGResources;
import com.buglabs.dragonfly.dm.service.BUGOSGiBundleService;
import com.buglabs.dragonfly.dm.service.IBUGOSGiBundleService;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.StaticBugConnection;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.BasicConnectorService;

public class BUGConnectorService extends BasicConnectorService {

	private boolean fIsConnected = false;
	private BUGOSGiBundleService fDaytimeService;
	private final IHost host;

	public BUGConnectorService(IHost host) {
		super(BUGResources.BUG_Connector_Name, BUGResources.BUG_Connector_Description, host, 13);
		this.host = host;
		fDaytimeService = new BUGOSGiBundleService();
	}

	protected void internalConnect(IProgressMonitor monitor) throws Exception {
		try {
			BugConnection bc = new StaticBugConnection(BUGResources.BUG_Connector_Name, new URL("http://" + host.getHostName() + ":8082"));
			fDaytimeService.setConnection(bc);
			fDaytimeService.loadBundles();
		} catch (Exception e) {
			String message = NLS.bind(BUGResources.DaytimeConnectorService_NotAvailable, getHostName());
			throw new Exception(message);
		}
		//if no exception is thrown, we consider ourselves connected!
		fIsConnected = true;
		// Fire comm event to signal state changed -- 
		// Not really necessary since SubSystem.connect(Shell, boolean) does
		// SystemRegistry.connectedStatusChange(this, true, false) at the end
		notifyConnection();
	}

	public IBUGOSGiBundleService getDaytimeService() {
		return fDaytimeService;
	}

	public boolean isConnected() {
		return fIsConnected;
	}

	protected void internalDisconnect(IProgressMonitor monitor) throws Exception {
		fDaytimeService.disconnect();
		fIsConnected = false;
	}

}
