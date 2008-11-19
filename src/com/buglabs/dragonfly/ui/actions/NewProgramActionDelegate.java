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
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Action delegate for retargetable (declaritive) New Program action.
 * 
 * @author ken
 * 
 */
public class NewProgramActionDelegate extends ActionDelegate {

	private ISelection selection;

	public NewProgramActionDelegate() {
	}

	public void run(IAction action) {
		NewProgramAction a = new NewProgramAction();
		a.setSelection(selection);
		a.run();
	}

	public void init(IAction action) {

	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;

	}
}
