package com.buglabs.dragonfly.ui.views.bugnet;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.buglabs.dragonfly.bugnet.BugnetResultManager;
import com.buglabs.dragonfly.exception.BugnetAuthenticationException;
import com.buglabs.dragonfly.exception.BugnetException;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.views.Messages;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * A simple job to query bugnet.
 * TODO - Still need to fill some of this stuff out
 * 
 * @author brian
 *
 */
public class QueryBugnetJob extends Job {

	public static final String ERROR_STATUS_MESSAGE = "There was an error contacting BUGnet";
	public static final String OK_STATUS_MESSAGE = "Query BUGnet successful";
	public static final String JOB_TITLE = "Querying BUGnet";
	//Messages.getString("QueryBugnetJob.0");
	
	public QueryBugnetJob() {
		super(JOB_TITLE);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			BugnetResultManager.getInstance().doQuery();
		// may need to tailor status for different exception
		// to provide user with better feedback
		// call can throw (at least)
		// BugnetAuthenticationException, BugnetException, and IOException			
		} catch (Exception e) {
			UIUtils.handleNonvisualError(ERROR_STATUS_MESSAGE, e);
			return new Status(IStatus.ERROR, 
					Activator.PLUGIN_ID, 
					ERROR_STATUS_MESSAGE, e);
		}
		return new Status(IStatus.OK, 
				Activator.PLUGIN_ID, OK_STATUS_MESSAGE, null);
	}

}
