/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.ui.Activator;

/**
 * EditorInput for editors that work on BUG model objects.
 * 
 * @author ken
 * 
 */
public class BugEditorInput implements IEditorInput {

	private Bug bug;

	private List modules;

	public BugEditorInput(Bug bug) {
		this.bug = bug;
	}

	public Bug getBug() {
		return bug;
	}

	public boolean exists() {

		return true;
	}

	public ImageDescriptor getImageDescriptor() {

		return Activator.getImageDescriptor("icons/sample.gif"); //$NON-NLS-1$
	}

	public String getName() {

		return bug.getName();
	}

	public IPersistableElement getPersistable() {

		return null;
	}

	public String getToolTipText() {
		return "";
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean equals(Object arg0) {
		if (arg0 instanceof BugEditorInput) {
			BugEditorInput otherInput = (BugEditorInput) arg0;

			if (otherInput.getName().equals(this.getName())) {
				return true;
			}
		}

		return false;
	}

	public List getModules() {
		if (modules == null) {
			modules = new ArrayList();
			List list = new ArrayList();
			list.addAll(bug.getChildren("Modules"));

			if (list.size() > 0) {
				modules.addAll(((ITreeNode) list.get(0)).getChildren());
			}
		}

		return modules;
	}
}
