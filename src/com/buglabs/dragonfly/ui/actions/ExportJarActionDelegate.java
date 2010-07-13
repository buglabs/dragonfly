/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;

public class ExportJarActionDelegate implements IObjectActionDelegate {
	IProject project;

	public ExportJarActionDelegate() {
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}

	public void run(IAction action) {
		UpsertToBugAction exportAction = new UpsertToBugAction(null, null, project, null);
		exportAction.run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(true);
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IProject) {
				project = (IProject) element;
			}

			// disable sending to BUG if project has errors or there are no active BUGs
			try {
				if (ProjectUtils.existsProblems(project) || (BugConnectionManager.getInstance().getBugConnections().size() == 0)) {
					action.setEnabled(false);
				}
			} catch (CoreException e) {
				action.setEnabled(true);
			}
		}
	}

}
