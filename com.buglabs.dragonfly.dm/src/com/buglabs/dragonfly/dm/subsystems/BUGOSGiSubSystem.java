/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import com.buglabs.dragonfly.dm.Activator;
import com.buglabs.dragonfly.dm.service.IBUGOSGiBundleService;

import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.model.ISystemRegistryUI;

public class BUGOSGiSubSystem extends SubSystem {

	private IBUGOSGiBundleService bundleService;

	public BUGOSGiSubSystem(IHost host, IConnectorService connectorService, IBUGOSGiBundleService bundleService) {
		super(host, connectorService);
		this.bundleService = bundleService;
	}

	public void initializeSubSystem(IProgressMonitor monitor) throws SystemMessageException {
		super.initializeSubSystem(monitor);

		final ISystemRegistryUI sr = RSEUIPlugin.getTheSystemRegistryUI();
		final SystemResourceChangeEvent event = new SystemResourceChangeEvent(this, ISystemResourceChangeEvents.EVENT_SELECT_EXPAND, null);
	
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				sr.postEvent(event);
			}
		});
	}

	public boolean hasChildren() {
		return isConnected();
	}

	public IBUGOSGiBundleService getBundleService() {
		return bundleService;
	}

	public Object[] getChildren() {
		if (isConnected()) {
			try {
				return bundleService.getBundles();
			} catch (Exception e) {
				String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, getHostName());
				SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID, ICommonMessageIds.MSG_CONNECT_FAILED, IStatus.ERROR, msgTxt, e);
				SystemMessageObject msgobj = new SystemMessageObject(msg, ISystemMessageObject.MSGTYPE_ERROR, this);
				return new Object[] { msgobj };
			}
		} else {
			return new Object[0];
		}
	}

	public void uninitializeSubSystem(IProgressMonitor monitor) {
		super.uninitializeSubSystem(monitor);
	}

	public Class getServiceType() {
		return IBUGOSGiBundleService.class;
	}

	public void switchServiceFactory(ISubSystemConfiguration factory) {
		// not applicable here

	}

}
