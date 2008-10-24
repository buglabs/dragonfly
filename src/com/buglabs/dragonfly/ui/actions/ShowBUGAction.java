package com.buglabs.dragonfly.ui.actions;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;

import com.buglabs.dragonfly.ui.jobs.LaunchPhysicalEditorJob;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

public class ShowBUGAction extends BUGConnectionActionDelegate {

	public void run(IAction action) {
		if (getBugProjectNode() != null) {
			try {
				BugWSHelper.subscribeToBug(getBugProjectNode());

				Job job = new LaunchPhysicalEditorJob(getBugProjectNode());
				job.schedule();
			} catch (Exception e) {
				UIUtils.handleVisualError("Unable to establish connection with BUG at " + getBugProjectNode().getUrl(), e);
			}
		}
	}
}
