package com.buglabs.dragonfly.ui.jobs;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.util.BugWSHelper;

public class TestConnectionJob extends Job{
	private String address;
	public TestConnectionJob(String name){
		super("Connection testing for: " + name);
		address = name;
	}
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Testing connection...", 100);
		try {
			BugWSHelper.getPrograms(new URL(address + "/program"));
			monitor.worked(33);
			BugWSHelper.getModuleList(null,new URL(address + "/module"));
			monitor.worked(33);
			BugWSHelper.getServices(new URL(address + "/service"));
			monitor.worked(33);
		} catch (Exception e) {
			return new Status(IStatus.ERROR,DragonflyActivator.PLUGIN_ID,"Error connecting to " + address,new Throwable(e));
		}
		return Status.OK_STATUS;
	}
}