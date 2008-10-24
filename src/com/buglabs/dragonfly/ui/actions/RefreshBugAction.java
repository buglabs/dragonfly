/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.ModelNodeChangeEvent;

public class RefreshBugAction extends Action {
	private Bug bug;
	public static final String REFRESH_BUG = "refresh_bug";

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
				//if(bug.isConnected()){
					ModelNodeChangeEvent event = new ModelNodeChangeEvent(this.getClass(),REFRESH_BUG, bug);
					DragonflyActivator.getDefault().fireModelChangeEvent(event);
				//}
			}
		});

	}
}
