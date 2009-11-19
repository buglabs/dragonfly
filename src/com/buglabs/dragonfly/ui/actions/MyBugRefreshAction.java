package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.ui.Activator;

public class MyBugRefreshAction extends RefreshBugAction {

	private TreeViewer viewer;

	public MyBugRefreshAction(TreeViewer viewer) {
		this.viewer = viewer;
		setText("Refresh Connection");
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_CONNECTION_REFRESH));
	}

	public void run() {
		final Bug bug = (Bug) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		if (bug.isConnected()) {
			setBug(bug);
			super.run();
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					BugConnectionManager.getInstance().fireBugAddedEvent(this.getClass(), bug);
				}
			});
		}
	}

	public boolean isEnabled() {
		if (((IStructuredSelection) viewer.getSelection()).getFirstElement() instanceof BugConnection)
			return true;
		return false;
	}
}
