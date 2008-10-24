/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

public class RegisterEventListenerJob extends Job {
	private final BugConnection bugNode;

	public RegisterEventListenerJob(BugConnection bugNode) {
		super(Messages.getString("RegisterEventListenerJob.0")); //$NON-NLS-1$
		this.bugNode = bugNode;
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			BugWSHelper.subscribeToBug(bugNode);
		} catch (Exception e) {
			UIUtils.handleNonvisualError(Messages.getString("RegisterEventListenerJob.1"), e); //$NON-NLS-1$
		}

		return Status.OK_STATUS;
	}

}
