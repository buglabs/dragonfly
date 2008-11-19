/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import java.beans.PropertyChangeEvent;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.ApplicationFolderNode;
import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.FolderNode;
import com.buglabs.dragonfly.model.IModelNode;
import com.buglabs.dragonfly.model.IPackage;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.PackageImpl;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.SerializerFactory;
import com.buglabs.dragonfly.util.WSHelper;

/**
 * Create a new program. Works w/ bug or local model.
 * 
 * @author ken
 * 
 */
public class NewProgramAction extends Action {

	private ISelection selection;

	public NewProgramAction() {
		this.setText(Messages.getString("NewProgramAction.0")); //$NON-NLS-1$
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_CONNECTION_ADD)); //$NON-NLS-1$
	}

	public NewProgramAction(ISelection selection) {
		this();
		this.selection = selection;
	}

	private void refreshProject(Bug b) {
		DragonflyActivator.getDefault().fireModelChangeEvent(new PropertyChangeEvent(this, "Applications", null, b)); //$NON-NLS-1$

	}

	private void refreshProject(IModelNode node) {
		DragonflyActivator.getDefault().fireModelChangeEvent(new PropertyChangeEvent(this, "Applications", null, node)); //$NON-NLS-1$
	}

	private void createEmptyPackage(Bug bug) throws MalformedURLException, Exception {
		IPackage pkg = new PackageImpl(Messages.getString("NewProgramAction.4"), new ArrayList()); //$NON-NLS-1$

		String resp = BugWSHelper.insertPackage(pkg, SerializerFactory.getSerializer("xml"), bug.getProgramURL()); //$NON-NLS-1$

		WSHelper.checkForError(resp);
	}

	private void createEmptyPackage(ITreeNode parent) throws MalformedURLException, Exception {
		int index = 1;
		IPackage pkg = new PackageImpl(Messages.getString("NewProgramAction.6") + index, new ArrayList()); //$NON-NLS-1$

		ProgramNode pn = new ProgramNode(pkg, null);

		while (parent.childExists(pn)) {
			index++;
			pkg.setName(Messages.getString("NewProgramAction.7") + index); //$NON-NLS-1$
		}

		parent.addChild(pn);
	}

	public void setSelection(ISelection selection) {
		this.selection = selection;
	}

	public void run() {
		if (selection instanceof IStructuredSelection) {
			Object sel = ((IStructuredSelection) selection).getFirstElement();

			if (sel instanceof FolderNode) {
				FolderNode fnode = (FolderNode) sel;

				try {
					Bug b = (Bug) fnode.getFirstParentOfType(Bug.class.getName());

					if (b != null) {
						createEmptyPackage(b);
						refreshProject(b);
					} else {
						// This is a local node.
						createEmptyPackage(fnode);
						refreshProject(fnode);
					}

					if (fnode instanceof ApplicationFolderNode) {
						((ApplicationFolderNode) fnode).setLoaded(false);
					}

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
