package com.buglabs.dragonfly.ui.actions;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BaseTreeNode;
import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ModelNodeChangeEvent;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.dialogs.BUGConnectionSelectionDialog;
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

	String bugUrl;

	private IProject project;

	private File jarFile;

	private IJobChangeListener jobListener;

	private int status;

	private BugConnection bugConnection;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (jarFile != null && jarFile.exists()) {
			Job job = new ExportJarToBugJob(bugUrl);

			if (jobListener != null) {
				job.addJobChangeListener(jobListener);
			}

			job.schedule();
		}
	}

	private String findBug() throws CoreException {
		final Display disp = PlatformUI.getWorkbench().getDisplay();
		bugConnection = null;
		status = -1;
		String bugURL = null;

		disp.syncExec(new Runnable() {

			public void run() {
				Shell shell = new Shell(disp);

				BaseTreeNode root = (BaseTreeNode) Activator.getDefault().getBugsViewRoot();
				
				// don't show BUG selection if we have one BUG
				if(root.getChildren().size() == 1){
					bugConnection = (BugConnection)root.getChildren().iterator().next();
				}
				else{
					BUGConnectionSelectionDialog conDialog = new BUGConnectionSelectionDialog(shell);
					status = conDialog.open();
					if (status == IStatus.OK) {
						bugConnection = conDialog.getSelectedBugConnection();
					}
				}
			}
		});

		if (bugConnection != null) {
			// if bug exists, ask user if bug should be updated
			if (bugExists()) {
				disp.syncExec(new Runnable() {

					public void run() {
						bugApplicationOverwrite = MessageDialog.openQuestion(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
								"Export BUG", "BUG Application '" + project.getName() + "' already exists in BUG '"
										+ bugConnection.getName() + "'\nAre you sure you want overwrite?");
					}

				});
				if (bugApplicationOverwrite) {
					bugURL = bugConnection.getUrl().toExternalForm();
				}
			} /*else {
				return bugConnection.getUrl().toExternalForm();
			}*/
		}

		return bugURL;
	}

	/**
	 * @return Returns true if bug exists, false otherwise
	 */
	private boolean bugExists() {
		List list = null;
		try {
			list = BugWSHelper.getPrograms(bugConnection.getProgramURL());
			if (list != null) {
				Iterator iterator = list.iterator();

				while (iterator.hasNext()) {
					ProgramNode node = (ProgramNode) iterator.next();
					if (node.getName().equals(project.getName())) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			UIUtils.handleVisualError("Unable connect to " + bugConnection.getName(), e);
			return false;
		}
		return false;
	}

	private class ExportJarToBugJob extends Job {

		private String url;

		/**
		 * 
		 * @param url
		 *            The URL of the Bug.
		 */
		public ExportJarToBugJob(String url) {
			super("Export Jar To BUG");
			this.url = url;
		}

		protected IStatus run(IProgressMonitor monitor) {
			IStatus ret = new Status(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, "", null);

			if (url == null) {
				try {
					url = findBug();
				} catch (CoreException e) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarAction.0"), e); //$NON-NLS-1$
				}
			}

			if (url != null) {
				try {
					if (bugName == null) {
						bugName = bugConnection.getName();
					}

					if(bugApplicationOverwrite){
						BugWSHelper.deleteProgram(new URL(url + "/program/" + project.getName().replace(' ', '+')).toExternalForm());
					}
					BugWSHelper.upsertBundle(jarFile, new URL(url + "/program/" + project.getName().replace(' ', '+'))); //$NON-NLS-1$
					ModelNodeChangeEvent event = new ModelNodeChangeEvent(this, new Bug(bugName, new URL(url)));
					DragonflyActivator.getDefault().fireModelChangeEvent(event);
				} catch (Exception e) {
					UIUtils.handleVisualError("Unable to upload jar file to BUG: " + url, e);
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarAction.0"), e); //$NON-NLS-1$
				}
			}
			return ret;
		}
	}
}
