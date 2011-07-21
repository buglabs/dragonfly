package com.buglabs.dragonfly.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.ui.views.mybugs.MyBugsView;
import com.buglabs.util.BugBundleConstants;

/**
 * Delegate that calls {@link RemoveBundleJob} to remove selected bundles from
 * the BUG
 * 
 * @author akravets
 * 
 */
public class RemoveBundleActionDelegate implements IObjectActionDelegate {
	public static final String ACTION_ID = "com.buglabs.dragonfly.ui.actions.RemoveBundleActionDelegate"; //$NON-NLS-1$

	private ProgramNode[] application;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}

	public void run(IAction action) {
		String jobName = "Removing application";
		if (application.length > 1)
			jobName += "s";

		RemoveBundleJob job = new RemoveBundleJob(jobName, application);
		job.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		List list = new ArrayList();

		IStructuredSelection viewerSelection = (IStructuredSelection) MyBugsView.getViewer().getSelection();
		Object[] array = viewerSelection.toArray();

		String bundleType;
		for (int i = 0; i < array.length; i++) {
			if (array[i] instanceof ProgramNode) {
				bundleType = ((ProgramNode) array[i]).getBundleType();
				if (bundleType.equals(BugBundleConstants.BUG_BUNDLE_APPLICATION) || bundleType.equals(BugBundleConstants.BUG_BUNDLE_LIBRARY)) {
					list.add(array[i]);
				}
			}
		}
		application = (ProgramNode[]) list.toArray(new ProgramNode[list.size()]);
	}
}
