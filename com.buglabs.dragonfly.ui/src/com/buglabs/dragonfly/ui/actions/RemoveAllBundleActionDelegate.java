package com.buglabs.dragonfly.ui.actions;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.buglabs.dragonfly.model.ApplicationFolderNode;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.ui.Activator;

/**
 * Bundles that calls {@link RemoveBundleJob} to remove all applications from
 * the BUG
 * 
 * @author akravets
 * 
 */
public class RemoveAllBundleActionDelegate implements IObjectActionDelegate {
	public static final String ACTION_ID = "com.buglabs.dragonfly.ui.actions.RemoveAllBundleActionDelegate"; //$NON-NLS-1$
	private static final IStatus OK = new Status(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, "", null);
	private ApplicationFolderNode applicationNode;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}

	public void run(IAction action) {
		List bundles = applicationNode.getBugBundles();
		ProgramNode[] applications = (ProgramNode[]) bundles.toArray(new ProgramNode[bundles.size()]);

		RemoveBundleJob job = new RemoveBundleJob("Removing applications", applications);
		job.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(true);
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof ApplicationFolderNode) {
				applicationNode = (ApplicationFolderNode) element;
				if (applicationNode.getBugBundles().size() == 0)
					action.setEnabled(false);
			}
		}
	}
}
