/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.views.BUGNetView;

/**
 * Refresh contents of BugNetView
 * 
 * @author Angel Roman
 * 
 */
public class RefreshBugNetViewAction extends Action {

	BUGNetView view;

	public RefreshBugNetViewAction(BUGNetView view) {
		this.view = view;
		setToolTipText("Refresh BUGnet View");
	}

	public void run() {
		synchronized (this) {
			view.refresh();
		}
	}

	public ImageDescriptor getImageDescriptor() {
		return Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_CONNECTION_REFRESH);
	}
}
