package com.buglabs.dragonfly.ui.jobs;

import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.util.BugWSHelper;

public class ConnectBugJob extends Job {

	private static final String MODULES_NODE_NAME = "Modules";
	private static final String APPLICATIONS_NODE_NAME = "Applications";
	private static final String SERVICES_NODE_NAME = "Services";

	private BugConnection bug;
	private boolean quiet;

	IStatus result = Status.OK_STATUS;

	public ConnectBugJob(BugConnection bug) {
		this(bug, false);
	}

	public ConnectBugJob(BugConnection bug, boolean quiet) {
		super("BUG Connection");
		this.bug = bug;
		this.quiet = quiet;
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			monitor.beginTask("Connecting to BUG " + bug.getName(), 100);

			bug.getChildren();
			ITreeNode modulesNode = (ITreeNode) bug.getChildren(MODULES_NODE_NAME).iterator().next();
			modulesNode.setChildren(BugWSHelper.getModuleList(modulesNode, bug.getModuleURL()));
			monitor.worked(50);

			ITreeNode programNode = (ITreeNode) bug.getChildren(APPLICATIONS_NODE_NAME).iterator().next();
			programNode.setChildren(BugWSHelper.getPrograms(bug.getProgramURL()));
			monitor.worked(50);

			ITreeNode serviceNode = (ITreeNode) bug.getChildren(SERVICES_NODE_NAME).iterator().next();
			serviceNode.setChildren(BugWSHelper.getServices(bug.getServiceURL()));
			monitor.worked(50);

			bug.setConnected(true);
			monitor.done();

		} catch (MalformedURLException e) {
			result = handleException(e);
		} catch (IOException e) {
			result = handleException(e);
		} catch (Exception e) {
			result = handleException(e);
		}
		return result;
	}

	private IStatus handleException(Exception e) {
		int status = IStatus.ERROR;
		if (quiet)
			status = IStatus.WARNING;
		return new Status(status, com.buglabs.dragonfly.ui.Activator.PLUGIN_ID, status, "Unable to connect to " + bug.getName(), new Throwable(e.toString()));
	}

	public boolean belongsTo(Object family) {
		return bug.equals(family);
	}
}
