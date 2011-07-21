package com.buglabs.dragonfly.ui.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BUGSupportInfoManager;
import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ModelNodeChangeEvent;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.dialogs.BUGConnectionSelectionDialog;
import com.buglabs.dragonfly.ui.util.BugProjectUtil;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * This action creates a JarFile of the specified of project and uploads it to a
 * Bug device.
 * 
 * @author Angel Roman
 * 
 */
public class UpsertToBugAction extends Action {

	private String bugUrl;
	private IProject project;
	private File jarFile;
	private IJobChangeListener jobListener;
	private String bugName;

	protected boolean bugApplicationOverwrite = false;

	public UpsertToBugAction(String bugUrl, String bugName, IProject project, IJobChangeListener jobListener) {
		this.bugUrl = bugUrl;
		this.project = project;
		this.jobListener = jobListener;
		this.bugName = bugName;
	}

	public void run() {
		try {
			jarFile = Activator.getDefault().exportToJar(project);
		} catch (CoreException e) {
			UIUtils.handleVisualError(Messages.getString("ExportJarToBugJob.UNABLE_TO_UPLOAD_JAR"), e); //$NON-NLS-1$
		}

		if (jarFile != null && jarFile.exists()) {
			Job job = new ExportJarToBugJob(bugUrl);

			if (jobListener != null) {
				job.addJobChangeListener(jobListener);
			}

			job.schedule();
		}
	}

	/**
	 * 
	 * @author each and every one of us
	 * 
	 */
	private class ExportJarToBugJob extends Job {

		// these are for the monitor progress bar
		private static final int TOTAL_WORK_UNITS = 100;
		private static final int WORKED_25_PERCENT = 25;

		private static final String JOB_TITLE = "Send Application to BUG"; //$NON-NLS-1$
		private static final String EXECUTION_ENVIRONMENT_KEY = "Bundle-RequiredExecutionEnvironment"; //$NON-NLS-1$		

		private String url;
		private BugConnection selectedBugConnection;

		/**
		 * 
		 * @param url
		 *            The URL of the Bug.
		 */
		public ExportJarToBugJob(String url) {
			super(JOB_TITLE);
			this.url = url;
		}

		/**
		 * Do all the work to get pump the application up to the BUG
		 */
		protected IStatus run(IProgressMonitor monitor) {
			// use the monitor to give us some feedback in the dialog //
			monitor.beginTask(JOB_TITLE, TOTAL_WORK_UNITS);

			// Get a bug connection either by passed url or dialog //
			final BugConnection bug = getBugConnection(url);
			if (bug == null) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarToBugJob.UNABLE_TO_LOCATE_BUG"), null); //$NON-NLS-1$
			} else if (bug instanceof CancelBugConnection) {
				return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, 0, "", null);
			}
			url = bug.getUrl().toExternalForm();

			// Makes a webservice call to the load info from support xml on BUG //
			BUGSupportInfoManager info = BUGSupportInfoManager.load(bug);
			monitor.worked(WORKED_25_PERCENT);
			if (info == null) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarToBugJob.UNABLE_TO_LOCATE_BUG"), null); //$NON-NLS-1$
			}

			// Check Execution Environment //
			if (!checkExecutionEnvironment(project, info)) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, String.format(Messages.getString("ExportJarToBugJob.EXECUTION_ENVIRONMENT_NO_MATCHY"), //$NON-NLS-1$
						project.getName(), bug.getName(), info.getExecutionEnvironment()), null);
			}

			// see if the App is already there //
			if (info.getBundleList().contains(project.getName())) {
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						// app is already there so open dialog to query user //
						bugApplicationOverwrite = MessageDialog.openQuestion(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
								Messages.getString("ExportJarToBugJob.UPLOAD_TO_BUG"), String.format( //$NON-NLS-1$
										Messages.getString("ExportJarToBugJob.ARE_YOU_SURE_OVERWRITE"), //$NON-NLS-1$
										project.getName(), bug.getName()));
					}
				});
				if (!bugApplicationOverwrite)
					return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, IStatus.OK, "", null);
			}

			try {

				// Delete the application if user asked for overwrite //
				if (bugApplicationOverwrite) {
					BugWSHelper.deleteProgram(new URL(url + "/program/" + project.getName().replace(' ', '+')).toExternalForm()); //$NON-NLS-1$
				}

				monitor.worked(WORKED_25_PERCENT);
				monitor.worked(WORKED_25_PERCENT);

				// Actually do the upload //
				if (!monitor.isCanceled()) {
					monitor.subTask(Messages.getString("ExportJarToBugJob.SENDING_APPLICATION_MSG")); //$NON-NLS-1$
					// upload bundle - check version to determine upload method //
					BugWSHelper.upsertBundle(jarFile, new URL(url + "/program/" + project.getName().replace(' ', '+')), //$NON-NLS-1$
							info.getVersion().equals(BUGSupportInfoManager.BUG_VERSION_PRE_R14));
					ModelNodeChangeEvent event = new ModelNodeChangeEvent(this, new Bug(bug.getName(), new URL(url)));
					DragonflyActivator.getDefault().fireModelChangeEvent(event);
				}

			} catch (Exception e) {
				UIUtils.handleVisualError(Messages.getString("ExportJarToBugJob.UNABLE_TO_UPLOAD_JAR") + ": " + url, e); //$NON-NLS-1$
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarToBugJob.UNABLE_TO_UPLOAD_JAR"), e); //$NON-NLS-1$
			}

			monitor.worked(WORKED_25_PERCENT);
			return new Status(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, "", null);
		}

		/**
		 * Return true if the app's execution environment matches one supplied
		 * by the BUG
		 * 
		 * @param project
		 * @param info
		 * @return
		 */
		private boolean checkExecutionEnvironment(IProject project, BUGSupportInfoManager info) {
			String ee = null;
			try {
				ee = BugProjectUtil.getManifestEntry(project, EXECUTION_ENVIRONMENT_KEY);
			} catch (Exception e1) {
				UIUtils.handleNonvisualWarning("Unable to read Execution Environment for project " + project.getName(), e1, true); //$NON-NLS-1$
			}
			// No EE in App, assumed to be PhoneME
			if (ee == null || ee.length() == 0)
				ee = BUGSupportInfoManager.PHONEME_EXECUTION_ENV;

			return info.getExecutionEnvironment().contains(ee);
		}

		/**
		 * Helper to get a connection to a BUG to upload to either by using the
		 * passed url or by calling findBUG() - which displays a selector
		 * 
		 * @param url
		 * @return
		 */
		private BugConnection getBugConnection(String url) {
			BugConnection connection = null;
			if (url == null) {
				connection = findBug();
			} else {
				try {
					if (bugName == null)
						bugName = "default"; //$NON-NLS-1$
					connection = new StaticBugConnection(bugName, new URL(url));
				} catch (MalformedURLException e) {
					UIUtils.handleNonvisualWarning(Messages.getString("ExportJarToBugJob.UNABLE_TO_LOCATE_BUG"), e, true); //$NON-NLS-1$
				}
			}
			return connection;
		}

		/**
		 * Displays a UI that allows a user to select a bug to upload to
		 * 
		 * @return
		 */
		private BugConnection findBug() {
			selectedBugConnection = null;
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					/* 
					 *  We had been just sending the app to the BUG if there was only
					 *  one bug, but this lead to a lot of accidental sends, so
					 *  now we're forcing the dialog.
					 *  Code to skip the dialog is commented out
					 *  To see the old code, see SVN History before 10/19/2009
					 */
					BUGConnectionSelectionDialog conDialog = new BUGConnectionSelectionDialog(new Shell(PlatformUI.getWorkbench().getDisplay()));
					int status = conDialog.open();
					if (status == Window.OK) {
						selectedBugConnection = conDialog.getSelectedBugConnection();
					} else if (status == Window.CANCEL) {
						selectedBugConnection = new CancelBugConnection();
					}
				}
			});
			return selectedBugConnection;
		}
	}

	/**
	 * Little hacky CancelBugConnection to detect if we chose to cancel the bug
	 * connection in the dialog
	 * 
	 * See findBug() method
	 */
	private class CancelBugConnection extends BugConnection {
		private static final long serialVersionUID = 1L;

		public CancelBugConnection() {
			super(null, null);
		}
	}
}
