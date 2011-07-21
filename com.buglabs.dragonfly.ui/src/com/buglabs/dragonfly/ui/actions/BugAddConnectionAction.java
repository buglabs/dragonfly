package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.wizards.bug.NewBugConnectionWizard;

public class BugAddConnectionAction extends Action {

	public BugAddConnectionAction() {
		this.setText("New BUG Connection"); //$NON-NLS-1$
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_CONNECTION_PROJECT));
	}

	public void run() {
		NewBugConnectionWizard wizard = new NewBugConnectionWizard();
		wizard.init(PlatformUI.getWorkbench(), null);
		WizardDialog dg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dg.open();
	}

}
