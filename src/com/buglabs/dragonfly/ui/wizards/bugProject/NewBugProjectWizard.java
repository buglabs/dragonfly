/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/

package com.buglabs.dragonfly.ui.wizards.bugProject;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.buglabs.dragonfly.model.BugProjectInfo;
import com.buglabs.dragonfly.ui.jobs.CreateBugProjectJob;
import com.buglabs.dragonfly.ui.util.BugProjectUtil;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Creates a new plugin project.
 * 
 * @author Angel Roman
 * 
 */
public class NewBugProjectWizard extends Wizard implements INewWizard {

	private NewProjectMainPage pg1;

	private BugProjectInfo pinfo;

	public NewBugProjectWizard() {
		pinfo = new BugProjectInfo();
	}

	public boolean performFinish() {
		CreateBugProjectJob job = new CreateBugProjectJob(pinfo);
		try {
			getContainer().run(false, false, job);
		} catch (Exception e) {
			UIUtils.handleVisualError(e.getCause().getMessage(), new Exception(e.getCause()));
			return false;
		}
		return true;
	}

	public static String getClassName(String projName) {
		return BugProjectUtil.formatProjectNameAsPackage(projName) + ".Activator";
	}

	public void addPages() {
		pg1 = new NewProjectMainPage(pinfo);
		addPage(pg1);
		addPage(new CodeGenerationPage(pinfo));
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}
}