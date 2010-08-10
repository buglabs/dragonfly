/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm.connectorservice;

import com.buglabs.dragonfly.dm.subsystems.IBUGOSGiSubSystem;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * This class manages our BUGConnectorService objects, so that if we ever
 * have multiple subsystem factories, different subsystems can share the same
 * ConnectorService if they share the communication layer.
 */
public class BUGConnectorServiceManager extends AbstractConnectorServiceManager {

	private static BUGConnectorServiceManager fInstance;

	public BUGConnectorServiceManager() {
		super();
	}

	/**
	 * Return singleton instance
	 * 
	 * @return the singleton instance
	 */
	public static BUGConnectorServiceManager getInstance() {
		if (fInstance == null) {
			fInstance = new BUGConnectorServiceManager();
		}
		return fInstance;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#createConnectorService(org.eclipse.rse.core.model.IHost)
	 */
	public IConnectorService createConnectorService(IHost host) {
		return new BUGConnectorService(host);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#sharesSystem(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public boolean sharesSystem(ISubSystem otherSubSystem) {
		return (otherSubSystem instanceof IBUGOSGiSubSystem);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#getSubSystemCommonInterface(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public Class getSubSystemCommonInterface(ISubSystem subsystem) {
		return IBUGOSGiSubSystem.class;
	}

}
