package com.buglabs.dragonfly.ui.jobs;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.ui.views.BUGNetView;

public class BUGNetRefreshJob implements IJobChangeListener {

	public void aboutToRun(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public void awake(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public void done(IJobChangeEvent event) {
		// A hack to help resolve a sync issue with the web site: Bug #398
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; ++i) {
			IViewReference[] refs = windows[i].getActivePage().getViewReferences();
			for (int j = 0; j < refs.length; ++j) {
				if (refs[j].getId().equals(BUGNetView.VIEW_ID)) {
					((BUGNetView) refs[j].getView(true)).refresh();
				}
			}
		}
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
