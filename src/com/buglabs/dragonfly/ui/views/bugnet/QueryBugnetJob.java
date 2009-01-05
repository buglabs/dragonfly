package com.buglabs.dragonfly.ui.views.bugnet;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.buglabs.dragonfly.bugnet.BugnetResultManager;
import com.buglabs.dragonfly.exception.BugnetAuthenticationException;
import com.buglabs.dragonfly.exception.BugnetException;
import com.buglabs.dragonfly.ui.views.Messages;

/**
 * A simple job to query bugnet.
 * TODO - Still need to fill some of this stuff out
 * 
 * @author brian
 *
 */
public class QueryBugnetJob extends Job {

	public static final String JOB_TITLE = Messages.getString("QueryBugnetJob.0");
	
	public QueryBugnetJob() {
		super(JOB_TITLE);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			BugnetResultManager.getInstance().doQuery();
		} catch (BugnetAuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BugnetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

}
