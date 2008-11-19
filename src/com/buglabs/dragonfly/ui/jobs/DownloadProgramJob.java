/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.jobs;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.actions.ImportBundleFromStreamAction;
import com.buglabs.dragonfly.bugnet.BugnetWSHelper;

/**
 * This job downloads a program from bugnet and places it in the My Library
 * section of the Bug explorer. This class intentionally references interal PDE
 * functionality.
 * 
 * @author ken
 * 
 */
public class DownloadProgramJob extends Job {

	private final String user;

	private final String project;

	public DownloadProgramJob(String user, String project) {
		super(Messages.getString("DownloadProgramJob.0")); //$NON-NLS-1$
		this.user = user;
		this.project = project;
	}

	protected IStatus run(IProgressMonitor monitor) {

		try {
			InputStream jarContents = null;

			try {
				jarContents = BugnetWSHelper.getProgram(user, project);

				ImportBundleFromStreamAction importAction = new ImportBundleFromStreamAction(project, user, jarContents);
				importAction.run();
				jarContents.close();
			} catch (Exception e1) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e1.getMessage(), e1);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getMessage(), e);
		}

		return Status.OK_STATUS;
	}
}
