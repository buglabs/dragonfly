package com.buglabs.dragonfly.ui.jobs;

import java.beans.PropertyChangeEvent;
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

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.exception.NodeNotUniqueException;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.actions.Messages;
import com.buglabs.dragonfly.util.SLPListener;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Job that will load BUGs from persisted location as well as all connected BUGs
 * 
 * @author akravets
 * 
 */
public class LoadBugsJob extends Job {

	private ITreeNode root;

	private File bugsFileName;

	private SLPListener slpListener;

	private IProgressMonitor jobMonitor;

	public static final String BUGS_TYPE = "bugs"; //$NON-NLS-1$

	public static final String BUG_TYPE = "bug"; //$NON-NLS-1$

	public static final String BUG_NAME = "name"; //$NON-NLS-1$

	public static final String BUG_URL = "url"; //$NON-NLS-1$	

	public LoadBugsJob(File bugsFileName, ITreeNode root, SLPListener slpListener) {
		super("Loading BUGs");
		this.root = root;
		this.bugsFileName = bugsFileName;
		this.slpListener = slpListener;
	}

	protected IStatus run(IProgressMonitor monitor) {
		jobMonitor = monitor;
		jobMonitor.beginTask("Loading BUGs", 100);
		jobMonitor.worked(10);

		try {
			// slpListener = new SLPListener(context, root, discoveredBugs);
			slpListener.start();

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

	public SLPListener getSLPListener() {
		return slpListener;
	}

	private void loadSavedBugs(IProgressMonitor monitor) throws WorkbenchException, IOException {
		FileReader reader = new FileReader(bugsFileName);

		// start reading from file only if it's available
		if (reader.ready()) {
			IMemento memento = XMLMemento.createReadRoot(reader);
			IMemento[] bugs = memento.getChildren(BUG_TYPE);

			for (int i = 0; i < bugs.length; i++) {
				String bugName = bugs[i].getString(BUG_NAME);
				if (root.getChildren(bugName).size() == 0) {
					URL url;
					try {
						url = new URL(bugs[i].getString(BUG_URL));
					} catch (MalformedURLException e) {
						url = new URL("http://localhost:" + DragonflyActivator.getDefault().getHttpPort());
					}
					try {
						BugConnection bug = new StaticBugConnection(bugName, url);
						// UIUtils.removeLoadingMessageBug(root, jobMonitor);
						root.addChild(bug);
						DragonflyActivator.getDefault().fireModelChangeEvent(new PropertyChangeEvent(this, "Root", null, root));
						monitor.worked(i);
					} catch (NodeNotUniqueException e) {
						Activator.getDefault().setBugsLoaded(true);
						UIUtils.handleNonvisualError("Duplicate bugs detected!", e);
					}
				}
			}
			Activator.getDefault().setBugsLoaded(true);
		}
	}
}
