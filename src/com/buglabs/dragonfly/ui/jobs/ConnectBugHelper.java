package com.buglabs.dragonfly.ui.jobs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.ui.actions.RefreshBugAction;
import com.buglabs.dragonfly.util.UIUtils;

public class ConnectBugHelper {

	/**
	 * This method creates a ConnectBugJob, attaches a listener for when job is
	 * complete to either register SDK w/ BUG for BUG events or, if no
	 * connection, set bug state. Finally, refreshes the view via
	 * RefreshBugAction
	 * 
	 * @param bug
	 *            - The bug to connect
	 * @param quiet
	 *            - Whether or not to show user an error if connection fails
	 */
	public static void connectToBug(final BugConnection bug, boolean quiet) {
		Job connectBugJob = new ConnectBugJob(bug, quiet);
		connectBugJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						Job job = event.getJob();

						if (!(job instanceof ConnectBugJob)) {
							throw new RuntimeException("Invalid job in job event handler.");
						}

						if (!((ConnectBugJob) job).failedQuietly()) {
							if (event.getResult().isOK()) {
								Job registerJob = new RegisterEventListenerJob(bug);
								registerJob.setPriority(Job.LONG);
								registerJob.schedule();
							} else {
								bug.setConnected(false);
								bug.getChildren().clear();
							}
							new RefreshBugAction(bug).run();
						}
					}
				});
				super.done(event);
			}
		});

		// Some logic to only schedule the job if it hasn't
		// already been scheduled
		IJobManager manager = Job.getJobManager();
		Job[] j = manager.find(bug);
		if (j.length == 0) {
			connectBugJob.setPriority(Job.LONG);
			connectBugJob.schedule();
		}
	}

	/**
	 * This is the old connectToBug method taken from the
	 * MyBugsViewContentProvider it is here just to keep track of what it used
	 * to do. It's been replaced by
	 * 
	 * ConnectBugHelper.connectToBug
	 * 
	 * @param bug
	 * @param quiet
	 */
	@Deprecated
	private void connectToBug1(final BugConnection bug, boolean quiet) {
		List elements = new ArrayList();
		try {
			ConnectBugJob bugConnectJob = new ConnectBugJob(bug, quiet);

			// get job manager and check if there is this job is already
			// running, if so don't start another one
			IJobManager manager = Job.getJobManager();
			Job[] j = manager.find(bug);

			if (j.length == 0) {
				bugConnectJob.setPriority(Job.LONG);
				bugConnectJob.schedule();
			}

			//final Job job = new LaunchPhysicalEditorJob(bug);
			bugConnectJob.addJobChangeListener(new JobChangeAdapter() {

				public void done(IJobChangeEvent event) {
					if (event.getResult().getCode() != Status.ERROR) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								//((TreeViewer) viewer).expandToLevel(bug, 1);
							}
						});
						Job registerJob = new RegisterEventListenerJob(bug);
						registerJob.setPriority(Job.LONG);
						registerJob.schedule();
						//job.setPriority(Job.SHORT);
						//job.schedule();
						new RefreshBugAction(bug).run();
					}
				}

			});

			/*
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
			*/

		} catch (Exception e) {
			bug.getChildren().clear();
			bug.setConnected(false);
			UIUtils.handleVisualError(Messages.getString("BugContentProvider.5"), e); //$NON-NLS-1$
		}
	}

}
