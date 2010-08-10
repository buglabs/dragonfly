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

/**
 * This is our subsystem, which manages the remote connection and resources for
 * a particular Service (system connection) object.
 */
public class BUGOSGiSubSystem extends SubSystem {

	private IBUGOSGiBundleService fDaytimeService;

	public BUGOSGiSubSystem(IHost host, IConnectorService connectorService, IBUGOSGiBundleService daytimeService) {
		super(host, connectorService);
		fDaytimeService = daytimeService;
	}

	public void initializeSubSystem(IProgressMonitor monitor) throws SystemMessageException {
		//This is called after connect - expand the daytime node.
		// Always called in worker thread.
		super.initializeSubSystem(monitor);
		//TODO find a more elegant solution for expanding the item, e.g. use implicit connect like filters
		final ISystemRegistryUI sr = RSEUIPlugin.getTheSystemRegistryUI();
		final SystemResourceChangeEvent event = new SystemResourceChangeEvent(this, ISystemResourceChangeEvents.EVENT_SELECT_EXPAND, null);
		//TODO bug 150919: postEvent() should not be necessary asynchronously
		//sr.postEvent(event);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				sr.postEvent(event);
			}
		});
	}

	public boolean hasChildren() {
		return isConnected();
	}

	public IBUGOSGiBundleService getDaytimeService() {
		return fDaytimeService;
	}

	public Object[] getChildren() {
		if (isConnected()) {
			try {
				return fDaytimeService.getBundles();
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
