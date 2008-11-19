/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.buglabs.dragonfly.model.ProgramNode;

/**
 * ActionDelegate for importing BUG bundle into workspace for editing.
 * 
 * @author ken
 * 
 */
public class ImportBundleActionDelegate implements IObjectActionDelegate {
	public static final String ACTION_ID = "com.buglabs.dragonfly.ui.actions.ImportBundleAction"; //$NON-NLS-1$

	ProgramNode program;

	public ImportBundleActionDelegate() {
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}

	public void run(IAction action) {
		ImportBundleAction importAction = new ImportBundleAction(program);
		importAction.run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof ProgramNode) {
				program = (ProgramNode) element;
			}
		}
	}
}
