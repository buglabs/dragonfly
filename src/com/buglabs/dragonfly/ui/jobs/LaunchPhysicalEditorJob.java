/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/

package com.buglabs.dragonfly.ui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.ui.editors.BugEditorInput;
import com.buglabs.dragonfly.ui.editors.PhysicalEditor;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Job to launch the BUG physical editor based on a BUG model object.
 * 
 * @author ken
 * 
 */
public class LaunchPhysicalEditorJob extends Job {
	private Bug bug;

	private IEditorInput input;

	public LaunchPhysicalEditorJob(Bug bugNode) {
		super(Messages.getString("LaunchPhysicalEditorJob.0")); //$NON-NLS-1$
		this.bug = bugNode;
	}

	protected IStatus run(IProgressMonitor monitor) {
		input = new BugEditorInput(bug);
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, PhysicalEditor.ID);
				} catch (PartInitException e) {
					UIUtils.handleVisualError(Messages.getString("LaunchPhysicalEditorJob.1"), e); //$NON-NLS-1$
				}
			}

		});

		return Status.OK_STATUS;
	}

}