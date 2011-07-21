/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.model.Bug;

public class RefreshBugAction extends Action {
	private Bug bug;

	public RefreshBugAction(Bug bug) {
		this.bug = bug;

	}

	public RefreshBugAction() {
	}

	public Bug getBug() {
		return bug;
	}

	public void setBug(Bug bug) {
		this.bug = bug;
	}

	public void run() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				BugConnectionManager.getInstance().fireBugRefreshEvent(this.getClass(), bug);
			}
		});

	}
}
