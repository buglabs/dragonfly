package com.buglabs.dragonfly.util;

import java.beans.PropertyChangeEvent;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.internal.runtime.IRuntimeConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.exception.NodeNotUniqueException;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ITreeNode;

public abstract class BugListener extends Thread {
	/**
	 * the interval between scans of service providers.
	 */
	private int BUG_SCAN_INTERVAL_MILLIS;
	
	public static final String REMOVE_BUG = "remove_bug";
	
	public static final String ADD_BUG = "add_bug";

	private static final int MAX_ERROR_COUNT = 5;

	private ITreeNode root;

	private List model;

	volatile private boolean terminated = false;

	private int errorCount = 0;

	public BugListener(ITreeNode root, List list) {
		this.root = root;
		model = list;
		try{
			BUG_SCAN_INTERVAL_MILLIS = Integer.parseInt(System.getProperty("com.buglabs.bug.emulator.scaninterval"));
		}
		catch(Exception e){
			BUG_SCAN_INTERVAL_MILLIS = 7000;
		}
	}

	public void run() {
		while (!terminated) {
			try {
				 List discoveredBugs = getBugs();

				if (model.size() != 0) {
					removedDisconnectedBugs(model, discoveredBugs);
				}

				for (int i = 0; i < discoveredBugs.size(); i++) {
					final BugConnection bug = (BugConnection) discoveredBugs.get(i);
					synchronized (model) {
						if (!model.contains(bug)) {
							model.add(bug);
						}
						if (!root.childExists(bug)) {
							URL url = bug.getUrl();
							TestConnectionJob con = new TestConnectionJob(url.toString());
							con.schedule();
							con.addJobChangeListener(new IJobChangeListener(){

								public void aboutToRun(IJobChangeEvent event) {
									// TODO Auto-generated method stub

								}

								public void awake(IJobChangeEvent event) {
									// TODO Auto-generated method stub

								}

								public void done(IJobChangeEvent event) {
									if (event.getResult().isOK()){
										try {
											root.addChild(bug);
											DragonflyActivator.getDefault().fireModelChangeEvent(new PropertyChangeEvent(this, ADD_BUG, null, bug));
										} catch (NodeNotUniqueException e) {
											UIUtils.handleNonvisualError("Node already exists", e);
										}
									}
								}

								public void running(IJobChangeEvent event) {
									// TODO Auto-generated method stub

								}

								public void scheduled(IJobChangeEvent event) {
									// TODO Auto-generated method stub

								}

								public void sleeping(IJobChangeEvent event) {
									// TODO Auto-generated method stub

								}

							});
						}
					}
				}
				Thread.sleep(BUG_SCAN_INTERVAL_MILLIS);
				errorCount = 0;
			} catch (InterruptedException e) {
				terminated = true;
			} catch (Exception e) {
				UIUtils.handleNonvisualError("Error occurred while updating My BUGs model.", e);
				errorCount++;
				
				if (errorCount > MAX_ERROR_COUNT) {
					terminated = true;
				}
			}
		}
	}

	protected abstract boolean isValidType(Object o);

	private synchronized void removedDisconnectedBugs(List model, List bugs) {
		Stack waste = new Stack();
		BugConnection bug = null;
		for (Iterator i = model.iterator(); i.hasNext();) {
			bug = (BugConnection) i.next();
			if (isValidType(bug)) {
				if (!bugs.contains(bug)) {
					waste.push(bug);
				}
			}
		}

		while (!waste.isEmpty()) {
			BugConnection bugToRemove = (BugConnection) waste.pop();
			model.remove(bugToRemove);
			bugToRemove.setConnected(false);
			if (DragonflyActivator.getDefault() != null && root.removeChild(bugToRemove) != null) {
				DragonflyActivator.getDefault().fireModelChangeEvent(new PropertyChangeEvent(this, REMOVE_BUG, null, bugToRemove));
			} else {
				model.remove(bug);
			}
		}
	}

	protected abstract List getBugs() throws Exception;

	public void terminate() {
		terminated = true;
	}

	public List getDiscoveredBugs() {
		return model;
	}
	
	private class TestConnectionJob extends Job{
		private String address;
		public TestConnectionJob(String name){
			super("Connecting to: " + name);
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
				monitor.done();
			} catch (Exception e) {
				 // not relevant here, we care only about good connections
			}
			return Status.OK_STATUS;
		}
	}
}
