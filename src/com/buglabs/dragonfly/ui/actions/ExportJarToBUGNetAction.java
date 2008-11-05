package com.buglabs.dragonfly.ui.actions;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.AuthenticationData;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.BugnetAuthenticationHelper;
import com.buglabs.dragonfly.ui.views.BUGNetView;
import com.buglabs.dragonfly.bugnet.BugnetResultManager;
import com.buglabs.dragonfly.bugnet.BugnetWSHelper;
import com.buglabs.dragonfly.exception.BugnetAuthenticationException;
import com.buglabs.dragonfly.exception.BugnetException;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.dragonfly.util.URLUtils;
import com.buglabs.dragonfly.validator.BUGApplicationProjectValidator;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;

public class ExportJarToBUGNetAction extends Action {

	private IProject project;

	private IJobChangeListener jobListener;
	private IJobChangeListener applicationUploadJobChangeListener;

	public final String errorMessage = "Unable to upload Jar to BUGnet.";

	private UploadJarToBUGNetJob job;
	
	private String location;

	public ExportJarToBUGNetAction(IProject proj, IJobChangeListener jobListener) {
		this.project = proj;
		this.jobListener = jobListener;
		applicationUploadJobChangeListener = new ApplicationUploadJobChangeListener();
		location = null;
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
			if (applicationUploadJobChangeListener != null) {
				job.addJobChangeListener(applicationUploadJobChangeListener);
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
					job.removeJobChangeListener(applicationUploadJobChangeListener);
					return okStatus;
				}
					
				location = BugnetWSHelper.addProgram(jar, project.getName());
				
			} catch (BugnetAuthenticationException e2) {
				String myMessage = errorMessage + "  An application of the same name may already exist."; //$NON-NLS-1$
				job.removeJobChangeListener(jobListener);
				job.removeJobChangeListener(applicationUploadJobChangeListener);
				return createErrorStatus(myMessage, e2);
			} catch (IOException e) {
				job.removeJobChangeListener(jobListener);
				job.removeJobChangeListener(applicationUploadJobChangeListener);
				return createErrorStatus("Unable to upload to BUGnet", e);			
			} catch (Exception e) {
				job.removeJobChangeListener(jobListener);
				job.removeJobChangeListener(applicationUploadJobChangeListener);
				return createErrorStatus(errorMessage, e);
			}
			
			return okStatus;
		}

	}

	protected IStatus createErrorStatus(String message, Exception e) {
		IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, message, e);
		return errorStatus;
	}
	
	/**
	 * After an application is uploaded, launch the version notes browser page
	 * 
	 * @author brian
	 *
	 */
	private class ApplicationUploadJobChangeListener implements IJobChangeListener {

		public void aboutToRun(IJobChangeEvent event) {}
		public void awake(IJobChangeEvent event) {}
		public void running(IJobChangeEvent event) {}
		public void scheduled(IJobChangeEvent event) {}
		public void sleeping(IJobChangeEvent event) {}

		public void done(IJobChangeEvent event) {
			// try to create and then open the version notes url
			if (location != null) {
				Job versionNotesJob = new LaunchVersionNotesJob();
				versionNotesJob.schedule();
			}
		}

	}
	
	/**
	 * Launch a browser window for editing the version notes
	 * 
	 * @author brian
	 *
	 */
	private class LaunchVersionNotesJob extends Job {

		private IStatus status = Status.OK_STATUS;
		
		public LaunchVersionNotesJob() {
			super("Launch Version Notes Job");
		}

		protected IStatus run(IProgressMonitor monitor) {
			// get the version notes url and a token
			// we need the token because editing version notes requires login
			URL tmp_url = null; 
			try {
				String tmp_token = BugnetWSHelper.getToken();
				if (tmp_token != null && tmp_token.length() > 0) {
					String urlStr = getVersionNotesUrl(project.getName(), tmp_token);
					if (urlStr != null) tmp_url = new URL(urlStr);
				}
			} catch (IOException e) {
				status = handleException(e);
			}
			
			// Launch the browser with the correct URL
			if (tmp_url != null && status.isOK()) {
				final URL url = tmp_url;
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						LaunchBrowserAction action = new LaunchBrowserAction(url, "Version Notes");
						action.run();
					}
				});
			}		
			return status;
		}
		
		
		/**
		 * Need to go to buglabs.net/applications/<programName>/version_notes?context=IDE&token=SOMETHING
		 * 	but SDK doesn't know about buglabs.net, only api.buglabs.net, so we need to go through
		 *  api.buglabs.net's helper controller
		 * 
		 * @param programName
		 * @return
		 */
		private String getVersionNotesUrl(String programName, String token) {
			String version_notes_url = null;
			try {
				programName = URLEncoder.encode(programName, "UTF-8");
				String path =  URLEncoder.encode("/applications/" 
						+ programName + "/version_notes?context=IDE&token=" + token, "UTF-8");
				version_notes_url = BugnetWSHelper.getBugNetBaseURL() + "helper/redirect?path=" + path;
			} catch (UnsupportedEncodingException e) {
				UIUtils.handleNonvisualError("Error generating url for updating version notes.", e);
			}
			return version_notes_url;
		}		
		
		/**
		 * lil' helper for handling exceptions in the job
		 * 
		 * @param e
		 * @return
		 */
		private IStatus handleException(Exception e) {
			UIUtils.handleVisualError(
					"There was an error launching the browser for editing version notes.", e);
			return createErrorStatus("There was an error launching the browser for editing version notes: " + e.getMessage(), e);		
		}		
	}
	
}
