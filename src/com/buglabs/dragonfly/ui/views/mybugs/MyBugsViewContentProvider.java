package com.buglabs.dragonfly.ui.views.mybugs;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.IModelChangeListener;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.Module;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.model.ServiceNode;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.ui.actions.RefreshBugAction;
import com.buglabs.dragonfly.ui.jobs.ConnectBugJob;
import com.buglabs.dragonfly.ui.jobs.LaunchPhysicalEditorJob;
import com.buglabs.dragonfly.ui.jobs.RegisterEventListenerJob;
import com.buglabs.dragonfly.ui.jobs.Messages;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * @author akravets
 * 
 */
public class MyBugsViewContentProvider implements ITreeContentProvider, IModelChangeListener {

	private Viewer viewer;

	public MyBugsViewContentProvider() {
		DragonflyActivator.getDefault().addListener(this);
	}

	public Object[] getChildren(Object parentElement) {
		// try to connect to bug when bug is selected
		if (parentElement instanceof BugConnection 
				&& !((BugConnection) parentElement).isConnected()) {
				connectToBug((BugConnection) parentElement);
		} 
		else if (parentElement instanceof ITreeNode) {
			if(parentElement instanceof StaticBugConnection){
				try {
					BugWSHelper.getModuleList(
							(StaticBugConnection) parentElement, 
							((StaticBugConnection)parentElement).getModuleURL());
				} 
				catch (IOException e) {
					UIUtils.giveNonVisualInformation(
							"BUG connection associated with '" + 
							((StaticBugConnection)parentElement).getName() + "' is unreachable"); //$NON-NLS-1$ //$NON-NLS-2$
					((StaticBugConnection)parentElement).setConnected(false);
					viewer.refresh();
					return null;
				}
			}
			Collection children = ((ITreeNode) parentElement).getChildren();
			if (children == null) {
				UIUtils.handleVisualError(Messages.getString("BugContentProvider.5"), null); //$NON-NLS-1$
				return null;
			}
			return children.toArray();
		}
		return null;
	}

	public Object getParent(Object element) {
		if (element instanceof ITreeNode) {
			return ((ITreeNode) element).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ProgramNode 
				|| element instanceof Module 
				|| element instanceof ServiceNode)
			return false;
		return true;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.getControl().getDisplay().syncExec(new Runnable() {

				public void run() {
					viewer.refresh();
				}
			});
		}
	}

	private void connectToBug(final BugConnection bug) {
		List elements = new ArrayList();
		try {
			ConnectBugJob bugConnectJob = new ConnectBugJob(bug);

			// get job manager and check if there is this job is already
			// running, if so don't start another one
			IJobManager manager = Job.getJobManager();
			Job[] j = manager.find(bug);

			if (j.length == 0) {
				bugConnectJob.setPriority(Job.LONG);
				bugConnectJob.schedule();
			}

			final Job job = new LaunchPhysicalEditorJob(bug);
			bugConnectJob.addJobChangeListener(new JobChangeAdapter() {

				public void done(IJobChangeEvent event) {
					if (event.getResult().getCode() != Status.ERROR) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								((TreeViewer) viewer).expandToLevel(bug, 1);
							}
						});

						job.setPriority(Job.SHORT);
						job.schedule();
					}
				}

			});

			job.addJobChangeListener(new JobChangeAdapter() {

				public void done(IJobChangeEvent event) {
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						public void run() {
							new RefreshBugAction(bug).run();

							Job registerJob = new RegisterEventListenerJob(bug);
							registerJob.setPriority(Job.LONG);
							registerJob.schedule();
						}
					});
				}

			});

		} catch (Exception e) {
			bug.getChildren().clear();
			bug.setConnected(false);
			UIUtils.handleVisualError(Messages.getString("BugContentProvider.5"), e); //$NON-NLS-1$
		}
	}

}
