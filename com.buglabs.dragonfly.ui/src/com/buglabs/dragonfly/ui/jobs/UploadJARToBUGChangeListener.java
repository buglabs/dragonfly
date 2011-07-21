package com.buglabs.dragonfly.ui.jobs;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.BugProjectNode;
import com.buglabs.dragonfly.ui.actions.RefreshBugAction;

public class UploadJARToBUGChangeListener implements IJobChangeListener {
	BugConnection proj;

	public UploadJARToBUGChangeListener(BugConnection proj) {
		this.proj = proj;
	}

	public UploadJARToBUGChangeListener(BugProjectNode bugProjNode) {
		// TODO Auto-generated constructor stub
	}

	public void aboutToRun(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public void awake(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public void done(IJobChangeEvent event) {
		RefreshBugAction refresh = new RefreshBugAction(proj);
		refresh.run();
	}

	public void running(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public void scheduled(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public void sleeping(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

}