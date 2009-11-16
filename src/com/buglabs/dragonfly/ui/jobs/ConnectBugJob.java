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

	private BugConnection bug;

	IStatus result = Status.OK_STATUS;

	public ConnectBugJob(BugConnection bug) {
		super("BUG Connection");
		this.bug = bug;
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			monitor.beginTask("Connecting to BUG " + bug.getName(), 100);

			bug.getChildren();
			ITreeNode modulesNode = (ITreeNode) bug.getChildren(Messages.getString("BugContentProvider.3")).iterator().next();
			modulesNode.setChildren(BugWSHelper.getModuleList(modulesNode, bug.getModuleURL()));
			monitor.worked(50);

			ITreeNode programNode = (ITreeNode) bug.getChildren(Messages.getString("BugContentProvider.4")).iterator().next();
			programNode.setChildren(BugWSHelper.getPrograms(bug.getProgramURL()));
			monitor.worked(50);

			ITreeNode serviceNode = (ITreeNode) bug.getChildren(Messages.getString("BugContentProvider.6")).iterator().next();
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
		return new Status(IStatus.ERROR, com.buglabs.dragonfly.ui.Activator.PLUGIN_ID, IStatus.ERROR, "Unable to connect to "
				+ bug.getName(), new Throwable(e.toString()));
	}

	public boolean belongsTo(Object family) {
		return bug.equals(family);
	}
}
