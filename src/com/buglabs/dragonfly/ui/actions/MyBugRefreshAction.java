package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

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
		Bug bug = (Bug) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		setBug(bug);
		super.run();
	}

	public boolean isEnabled() {
		if (((IStructuredSelection) viewer.getSelection()).getFirstElement() instanceof BugConnection)
			return true;
		return false;
	}
}
