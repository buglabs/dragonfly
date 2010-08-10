/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm.subsystems;

import java.util.HashMap;
import java.util.Map;

import com.buglabs.dragonfly.dm.connectorservice.BUGConnectorService;
import com.buglabs.dragonfly.dm.connectorservice.BUGConnectorServiceManager;
import com.buglabs.dragonfly.dm.service.IBUGOSGiBundleService;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.services.IService;

/**
 * The BUGOSGiSubSystemConfiguration implements the main API for registering a
 * new subsystem type. It gives the RSE framework basic configuration data about
 * enabled or disabled options, and is responsible for instanciating the actual
 * Daytime subsystem as well as the UI-less configuration layer (service).
 */
public class BUGOSGiSubSystemConfiguration extends SubSystemConfiguration {

	private Map fServices = new HashMap();

	public BUGOSGiSubSystemConfiguration() {
		super();
	}

	public boolean supportsFilters() {
		return false;
	}

	public boolean supportsSubSystemConnect() {
		//TODO for now, we have to connect in order to pass the hostname to the service
		//This should not be necessary in an ideal world
		return true;
	}

	public boolean isPortEditable() {
		return false;
	}

	public boolean isFactoryFor(Class subSystemType) {
		return BUGOSGiSubSystem.class.equals(subSystemType);
	}

	/**
	 * Instantiate and return an instance of OUR subystem. Do not populate it
	 * yet though!
	 * 
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(IHost)
	 */
	public ISubSystem createSubSystemInternal(IHost host) {
		IConnectorService connectorService = getConnectorService(host);
		ISubSystem subsys = new BUGOSGiSubSystem(host, connectorService, createDaytimeService(host)); // DWD need to provide the subsystem with a name and id too.
		return subsys;
	}

	public IConnectorService getConnectorService(IHost host) {
		return BUGConnectorServiceManager.getInstance().getConnectorService(host, IBUGOSGiBundleService.class);
	}

	public void setConnectorService(IHost host, IConnectorService connectorService) {
		BUGConnectorServiceManager.getInstance().setConnectorService(host, IBUGOSGiBundleService.class, connectorService);
	}

	public IBUGOSGiBundleService createDaytimeService(IHost host) {
		BUGConnectorService connectorService = (BUGConnectorService) getConnectorService(host);
		return connectorService.getDaytimeService();
	}

	public final IService getService(IHost host) {
		IBUGOSGiBundleService service = (IBUGOSGiBundleService) fServices.get(host);
		if (service == null) {
			service = createDaytimeService(host);
			fServices.put(host, service);
		}
		return service;
	}

	public final Class getServiceType() {
		return IBUGOSGiBundleService.class;
	}

	public Class getServiceImplType() {
		return IBUGOSGiBundleService.class;
	}

}
