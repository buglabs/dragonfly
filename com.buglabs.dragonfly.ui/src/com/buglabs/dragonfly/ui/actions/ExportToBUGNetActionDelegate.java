/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPart;

import com.buglabs.dragonfly.ui.jobs.BUGNetRefreshJob;

public class ExportToBUGNetActionDelegate extends AbstractBUGNetActionDelegate {
	public ExportToBUGNetActionDelegate() {
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		ExportJarToBUGNetAction exportAction = new ExportJarToBUGNetAction(project, new BUGNetRefreshJob());
		exportAction.run();
	}
}
