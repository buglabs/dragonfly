package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.ui.wizards.bug.NewBugConnectionWizard;

/**
 * Launches the new bug wizard.
 * 
 * @author Angel Roman
 * 
 */
public class LaunchNewBugWizard implements IWorkbenchWindowActionDelegate {

	IWorkbenchWindow win;

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		win = window;
	}

	public void run(IAction action) {
		NewBugConnectionWizard wizard = new NewBugConnectionWizard();
		wizard.init(PlatformUI.getWorkbench(), null);
		WizardDialog dg = new WizardDialog(win.getShell(), wizard);
		dg.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}
}
