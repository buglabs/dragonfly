/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.dnd;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

import com.buglabs.dragonfly.BugApplicationNature;
import com.buglabs.dragonfly.model.BugProjectNode;
import com.buglabs.dragonfly.model.FolderNode;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.actions.UpsertToBugAction;
import com.buglabs.dragonfly.ui.jobs.UploadJARToBUGChangeListener;

public class ProgramDropAdapter extends CommonDropAdapterAssistant {

	FolderNode fnode;

	public ProgramDropAdapter() {
		// TODO Auto-generated constructor stub
	}

	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {

		if (aDropTargetEvent.data instanceof TreeSelection) {
			Object obj = ((TreeSelection) aDropTargetEvent.data).getFirstElement();

			if (obj instanceof IAdaptable) {
				IProject proj = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
				if (proj != null) {
					try {
						if (proj.hasNature(BugApplicationNature.ID)) { //$NON-NLS-1$
							BugProjectNode bugProjNode = (BugProjectNode) fnode.getParent();

							UpsertToBugAction action = new UpsertToBugAction(bugProjNode.getUrl().toString(), bugProjNode.getName(), proj,
									new UploadJARToBUGChangeListener(bugProjNode));

							action.run();
						}
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public IStatus validateDrop(Object target, int operation, TransferData transferType) {

		IStatus ok = new Status(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$

		if (target instanceof FolderNode) {
			fnode = (FolderNode) target;
			if (fnode.getName().equals(Messages.getString("ProgramDropAdapter.0"))) { //$NON-NLS-1$
				return ok;
			}
		} else if (target instanceof IWorkspaceRoot) {
			return ok;
		}
		return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
	}
}
