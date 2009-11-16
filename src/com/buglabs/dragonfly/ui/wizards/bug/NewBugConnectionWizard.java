/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/

package com.buglabs.dragonfly.ui.wizards.bug;

import java.net.MalformedURLException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.exception.NodeNotUniqueException;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.ui.PerspectiveFactory;
import com.buglabs.dragonfly.ui.views.mybugs.MyBugsView;

/**
 * Wizard for new BUG connection
 * 
 * @author ken
 * 
 */
public class NewBugConnectionWizard extends Wizard implements INewWizard {
	ConnectBug_MainPage mainpage;

	public static QualifiedName URL_PERSISTENT_PROPERTY = new QualifiedName("com.buglabs", "url"); //$NON-NLS-1$ //$NON-NLS-2$

	public NewBugConnectionWizard() {
	}

	public void addPages() {
		mainpage = new ConnectBug_MainPage();
		addPage(mainpage);
	}

	public boolean performFinish() {
		try {
			BugConnection bug = new StaticBugConnection(mainpage.getProjectName(), mainpage.getBugURL());
			BugConnectionManager.getInstance().getBugConnectionsRoot().addChild(bug);
			if (MyBugsView.getViewer() != null) {
				MyBugsView.getViewer().refresh();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (NodeNotUniqueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			IPerspectiveDescriptor persp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
			if (!persp.getId().equals(PerspectiveFactory.PERSPECTIVE_ID)) {

				boolean canSwitch = MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages
						.getString("NewProjectWizard.3"), Messages.getString("NewProjectWizard.4")); //$NON-NLS-1$ //$NON-NLS-2$

				if (canSwitch) {
					PlatformUI.getWorkbench().showPerspective(PerspectiveFactory.PERSPECTIVE_ID,
							PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				}
			}
		} catch (WorkbenchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	protected IProject getNewProject() throws CoreException {
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(mainpage.getProjectName());
		proj.create(new NullProgressMonitor());
		proj.open(new NullProgressMonitor());

		return proj;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {}
}
