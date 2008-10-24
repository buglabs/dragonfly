package com.buglabs.dragonfly.ui.actions;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.ui.Activator;

public class MyBugRefreshAction extends RefreshBugAction {

	private TreeViewer viewer;

	private Bug bug;

	public MyBugRefreshAction(TreeViewer viewer) {
		this.viewer = viewer;
		setText("Refresh Connection");
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_CONNECTION_REFRESH));
	}

	public MyBugRefreshAction(Bug bug) {
		this.bug = bug;
		setText("Refresh Connection");
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_CONNECTION_REFRESH));
	}

	public void run() {
		Bug bug = (Bug) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		List noConnectList = Activator.getDefault().getNoConnectList();
		if (noConnectList.contains(bug)) {
			noConnectList.remove(bug);
		}
		setBug(bug);
		super.run();
	}

	public boolean isEnabled() {
		if (((IStructuredSelection) viewer.getSelection()).getFirstElement() instanceof BugConnection)
			return true;
		return false;
	}
}
