package com.buglabs.dragonfly.ui.views.bugnet;

import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.bugnet.BugnetResultManager;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * A simple job to query bugnet. TODO - Still need to fill some of this stuff
 * out
 * 
 * @author brian
 * 
 */
public class QueryBugnetJob extends Job {

	public static final String ERROR_STATUS_MESSAGE = "There was an error contacting BUGnet";
	public static final String DISABLED_STATUS_MESSAGE = "BUGnet is disabled. You can enable BUGnet in your preferences";
	public static final String OK_STATUS_MESSAGE = "Query BUGnet successful";
	public static final String JOB_TITLE = "Querying BUGnet";

	//Messages.getString("QueryBugnetJob.0");

	public QueryBugnetJob() {
		super(JOB_TITLE);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// First make sure BUGnet is activated
		if (!DragonflyActivator.getDefault().getPluginPreferences().getBoolean(DragonflyActivator.PREF_BUGNET_ENABLED)) {
			Exception e = new NoRouteToHostException(DISABLED_STATUS_MESSAGE);
			UIUtils.handleNonvisualError(ERROR_STATUS_MESSAGE, e);
			return new Status(IStatus.WARNING, Activator.PLUGIN_ID, DISABLED_STATUS_MESSAGE, e);
		}

		try {
			BugnetResultManager.getInstance().doQuery();
			// may need to tailor status for different exception
			// to provide user with better feedback
			// call can throw (at least)
			// BugnetAuthenticationException, BugnetException, and IOException			
		} catch (UnknownHostException e) {
			//Unable to resolve host, machine is probably offline.
			return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, OK_STATUS_MESSAGE);
		} catch (Exception e) {
			UIUtils.handleNonvisualError(ERROR_STATUS_MESSAGE, e);
			return new Status(IStatus.WARNING, Activator.PLUGIN_ID, ERROR_STATUS_MESSAGE, e);
		}
		return new Status(IStatus.OK, Activator.PLUGIN_ID, OK_STATUS_MESSAGE);
	}

}
