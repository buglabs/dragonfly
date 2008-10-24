/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.dnd;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ResourceTransfer;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.ui.actions.ExportJarAction;
import com.buglabs.dragonfly.ui.actions.RefreshBugAction;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;

public class BugDropTargetAdapter extends DropTargetAdapter implements IJobChangeListener {

	private String bugURL;

	private Bug bug;

	private Control control;

	public BugDropTargetAdapter(Control control, Bug bug) {
		bugURL = bug.getUrl().toString();
		this.bug = bug;
		this.control = control;
		initDND();
	}

	public BugDropTargetAdapter(Control control, String bugURL) {
		this.bugURL = bugURL;
		this.control = control;
		initDND();
	}

	private void initDND() {
		DropTarget target = new DropTarget(control, DND.DROP_MOVE);
		target.setTransfer(new Transfer[] { ResourceTransfer.getInstance() });

		target.addDropListener(this);
	}

	public void drop(DropTargetEvent event) {
		if (ResourceTransfer.getInstance().isSupportedType(event.currentDataType)) {
			IResource[] resources = (IResource[]) event.data;
			if (resources.length > 0) {
				IProject proj = (IProject) resources[0].getAdapter(IProject.class);
				if (proj != null) {
					try {
						/*if(ProjectUtils.existsProblems(proj)){
							IStatus status = new Status(IStatus.ERROR,DragonflyActivator.PLUGIN_ID,"Application '" + proj.getName() + "' contains errors. Please fix errors before uploading.",null);
							throw new CoreException(status);
						}*/
						if (proj.hasNature("org.eclipse.pde.PluginNature")) { //$NON-NLS-1$
							ExportJarAction action = new ExportJarAction(proj, bugURL, this);
							action.run();
						} else {
							MessageDialog.openInformation(control.getShell(),
									Messages.getString("BugDropTargetAdapter.1"), Messages.getString("BugDropTargetAdapter.2")); //$NON-NLS-1$ //$NON-NLS-2$

						}
					} catch (CoreException e) {
						UIUtils.handleVisualError(e.getMessage(), e);
						return;
					}
				}
			}
		}
	}

	public void dropAccept(DropTargetEvent event) {

	}

	public void aboutToRun(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public void awake(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public void done(IJobChangeEvent event) {
		if (bug != null) {
			RefreshBugAction ref = new RefreshBugAction(bug);
			ref.run();
		}
	}

	public void running(IJobChangeEvent event) {

	}

	public void scheduled(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public void sleeping(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}
}
