package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.jobs.ConnectBugHelper;


public class ConnectAndRefreshBugAction extends Action {
	private static final String ACTION_TEXT = "Refresh Connection";
	private TreeViewer viewer;

	public ConnectAndRefreshBugAction(TreeViewer viewer) {
		this.viewer = viewer;
		setText(ACTION_TEXT);
		this.setImageDescriptor(Activator.getDefault()
				.getImageRegistry().getDescriptor(Activator.IMAGE_CONNECTION_REFRESH));
	}
	
	@Override
	public void run() {
		final BugConnection bug = (BugConnection) 
				((IStructuredSelection) viewer.getSelection()).getFirstElement();
		if (bug == null) return;
		ConnectBugHelper.connectToBug(bug, false);
	}

}
