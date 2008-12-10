package com.buglabs.dragonfly.ui.views;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

import com.buglabs.dragonfly.bugnet.BugnetResultManager;

/**
 * The BugnetView, not to be confused with the old BUGnetView
 * This one uses the BugnetViewer and an MVC approach to draw a view
 * containing a list of bug applications
 * 
 * @author brian
 *
 */
public class BugnetView extends ViewPart {
	private Composite top;
	private BugnetViewer bugnetViewer;
	
	public void createPartControl(Composite parent) {
		// create top-level composite and put form in there
		top = new Composite(parent, SWT.None);
		top.setLayout(new FillLayout());
			
		bugnetViewer = new BugnetViewer(top);
		
		// query bugnet, when done, draw viewer
		QueryBugnetJob queryBugnetJob = new QueryBugnetJob();
		queryBugnetJob.addJobChangeListener(new IJobChangeListener(){
			// Don't need these methods
			public void aboutToRun(IJobChangeEvent event) {}
			public void awake(IJobChangeEvent event) {}
			public void running(IJobChangeEvent event) {}
			public void scheduled(IJobChangeEvent event) {}
			public void sleeping(IJobChangeEvent event) {}
			/**
			 *  Just need to know when we're done querying BUGnet
			 */
			public void done(IJobChangeEvent event) {
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						// Set the model on the viewer - this will cause the viewer to draw
						bugnetViewer.setInput(BugnetResultManager.getInstance().getApplications());
					}
				});
			}
			
		});
		queryBugnetJob.schedule();
	}

	public void setFocus() {
		top.setFocus();
	}

}
