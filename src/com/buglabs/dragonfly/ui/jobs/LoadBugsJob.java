package com.buglabs.dragonfly.ui.jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.actions.Messages;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Job that will load BUGs from persisted location
 * 
 * @author akravets
 * 
 */
public class LoadBugsJob extends Job {

	private File bugsFileName;

	private IProgressMonitor jobMonitor;

	public static final String BUGS_TYPE = "bugs"; //$NON-NLS-1$

	public static final String BUG_TYPE = "bug"; //$NON-NLS-1$

	public static final String BUG_NAME = "name"; //$NON-NLS-1$

	public static final String BUG_URL = "url"; //$NON-NLS-1$	

	public LoadBugsJob(File bugsFileName) {
		super("Loading BUGs");
		this.bugsFileName = bugsFileName;
	}

	protected IStatus run(IProgressMonitor monitor) {
		jobMonitor = monitor;
		jobMonitor.beginTask("Loading BUGs", 100);
		jobMonitor.worked(10);

		try {
			monitor.worked(10);
			loadSavedBugs(monitor);
		} catch (WorkbenchException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarAction.0"), e); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarAction.0"), e); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarAction.0"), e); //$NON-NLS-1$
		} catch (IOException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error occurred while reading static BUGs", e); //$NON-NLS-1$
		}
		jobMonitor.done();
		return new Status(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, "", null);
	}

	private void loadSavedBugs(IProgressMonitor monitor) throws WorkbenchException, IOException {
		FileReader reader = new FileReader(bugsFileName);

		// start reading from file only if it's available
		if (reader.ready()) {
			IMemento memento = XMLMemento.createReadRoot(reader);
			IMemento[] bugs = memento.getChildren(BUG_TYPE);

			for (int i = 0; i < bugs.length; i++) {
				String bugName = bugs[i].getString(BUG_NAME);
				if (!BugConnectionManager.getInstance().sameNameConnected(bugName)) {
					URL url = null;
					try {
						url = new URL(bugs[i].getString(BUG_URL));
					} catch (MalformedURLException e) {
						UIUtils.handleNonvisualError("URL is bad for BUG " + bugName + " url: " + bugs[i].getString(BUG_URL), e);
					}
					if (url == null)
						continue;

					BugConnectionManager.getInstance().addBugConnection(new StaticBugConnection(bugName, url));
					monitor.worked(i);
				}
			}
			Activator.getDefault().setBugsLoaded(true);
		}
	}
}
