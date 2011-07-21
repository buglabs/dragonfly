package com.buglabs.dragonfly.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.buglabs.dragonfly.ui.editors.PhysicalEditor;

public class RefreshPhysicalEditorAction implements IEditorActionDelegate {

	IEditorPart editor;
	private RefreshJob job;

	public RefreshPhysicalEditorAction() {
		// TODO Auto-generated constructor stub
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editor = targetEditor;
	}

	public void run(IAction action) {
		if (editor instanceof PhysicalEditor) {
			if (((PhysicalEditor) editor).isBUGConnected()) {
				IJobManager manager = Job.getJobManager();
				Job[] jobs = manager.find("Refresh"); //$NON-NLS-1$

				if (editor instanceof PhysicalEditor && jobs.length == 0) {
					job = new RefreshJob("Refresh", PhysicalEditor.REFRESH_FAMILY);
					job.schedule();
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * A job that calls refreshModules()
	 * 
	 * @author akravets
	 * 
	 */
	private class RefreshJob extends Job {

		private String family;

		public RefreshJob(String name, String family) {
			super(name);
			this.family = family;
		}

		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Refreshing editor", IProgressMonitor.UNKNOWN);
			((PhysicalEditor) editor).refresh();

			/* TODO - remove this code
			 *  physical editor used to affect the Bugnet view, but it no longer does
			 
			ModelNodeChangeEvent event = new ModelNodeChangeEvent(this.getClass(),PhysicalEditor.REFRESH, ((PhysicalEditor)editor).getBug());
			
			BUGNetView view = BUGNetView.getView();
			if(view != null){
				view.propertyChange(event);
			}
			*/

			monitor.done();
			return Status.OK_STATUS;
		}

		public boolean belongsTo(Object family) {
			return this.family.equals(family);
		}

	}
}
