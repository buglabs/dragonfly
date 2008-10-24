package com.buglabs.dragonfly.ui.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.AuthenticationData;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.BugnetAuthenticationHelper;
import com.buglabs.dragonfly.bugnet.BugnetWSHelper;
import com.buglabs.dragonfly.exception.BugnetAuthenticationException;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.dragonfly.validator.BUGApplicationProjectValidator;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;

public class ExportJarToBUGNetAction extends Action {

	private IProject project;

	private IJobChangeListener jobListener;

	public final String errorMessage = "Unable to upload Jar to BUGnet.";

	private UploadJarToBUGNetJob job;

	public ExportJarToBUGNetAction(IProject proj, IJobChangeListener jobListener) {
		this.project = proj;
		this.jobListener = jobListener;
	}

	public void run() {

		try {
			boolean valid = BUGApplicationProjectValidator.validate(project, true);

			if (!valid) {
				return;
			}
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		File location = Activator.getDefault().getStateLocation().toFile();
		File jarFile = null;

		try {
			jarFile = ProjectUtils.exporToJar(location, project);
		} catch (CoreException e) {
			UIUtils.handleVisualError("Unable to export project as jar file.", e);
			return;
		}

		if (jarFile != null) {
			job = new UploadJarToBUGNetJob(jarFile);

			if (jobListener != null) {
				job.addJobChangeListener(jobListener);
			}
			job.schedule();
		}
	}

	private class UploadJarToBUGNetJob extends Job {

		File jar;

		public UploadJarToBUGNetJob(File jarFile) {
			super("Upload Jar To BUGnet");
			jar = jarFile;
		}

		protected IStatus run(IProgressMonitor monitor) {
			IStatus okStatus = new Status(
					IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, "Successful upload to Jar to BUGnet", null); //$NON-NLS-1$

			try {
				// make sure credentials are good
				if (!BugnetAuthenticationHelper.login()) {
					// if we didn't log in, means we canceled or clicked
					// signup link.  Either way, let's get out of here
					job.removeJobChangeListener(jobListener);
					return okStatus;
				}
					
				BugnetWSHelper.addProgram(jar, project.getName());
				
			} catch (BugnetAuthenticationException e2) {
				String myMessage = errorMessage + "  An application of the same name may already exist."; //$NON-NLS-1$
				job.removeJobChangeListener(jobListener);
				return createErrorStatus(myMessage, e2);
			} catch (IOException e) {
				job.removeJobChangeListener(jobListener);
				return createErrorStatus("Unable to upload to BUGnet", e);			
			} catch (Exception e) {
				job.removeJobChangeListener(jobListener);
				return createErrorStatus(errorMessage, e);
			}
			
			return okStatus;
		}

	}

	protected IStatus createErrorStatus(String message, Exception e) {
		IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, message, e);
		return errorStatus;
	}
}
