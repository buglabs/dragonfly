package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.buglabs.dragonfly.ui.wizards.bugProject.NewBugProjectWizard;

/**
 * Launches the new bug project wizard
 * 
 * @author Angel Roman
 * 
 */
public class LaunchNewProjectWizard implements IWorkbenchWindowActionDelegate {
	IWorkbenchWindow win;

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		win = window;
	}

	public void run(IAction action) {
		WizardDialog wd = new WizardDialog(win.getShell(), new NewBugProjectWizard());
		wd.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
