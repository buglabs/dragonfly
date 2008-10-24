package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * Action delegate for bug connection properties. Properties dialog is only
 * valid for <code>BugConnection</code>
 * 
 * @author akravets
 * 
 */
public class PropertyDialogActionDelegate implements IObjectActionDelegate {

	private IWorkbenchPart targetPart;

	public PropertyDialogActionDelegate() {
		// TODO Auto-generated constructor stub
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {
		IShellProvider shell = targetPart.getSite();
		ISelectionProvider selectionProvider = targetPart.getSite().getSelectionProvider();
		PropertyDialogAction propertyAction = new PropertyDialogAction(shell, selectionProvider);
		propertyAction.run();

	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

}
