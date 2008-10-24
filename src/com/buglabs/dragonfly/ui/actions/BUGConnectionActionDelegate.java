package com.buglabs.dragonfly.ui.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.buglabs.dragonfly.model.BugProjectNode;
import com.buglabs.dragonfly.ui.wizards.bug.NewBugConnectionWizard;

public abstract class BUGConnectionActionDelegate implements IObjectActionDelegate {
	private BugProjectNode bpn;

	public BUGConnectionActionDelegate() {
		bpn = null;
	}

	public abstract void run(IAction action);

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}

	public BugProjectNode getBugProjectNode() {
		return bpn;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		IProject proj = null;

		if (selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			if (obj != null) {
				if (obj instanceof IAdaptable) {
					proj = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
				}
			}

			if (proj != null) {
				try {
					String bugURL = proj.getPersistentProperty(NewBugConnectionWizard.URL_PERSISTENT_PROPERTY);
					if (bugURL != null) {
						bpn = new BugProjectNode(proj.getName(), new URL(bugURL), proj);
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
