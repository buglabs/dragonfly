package com.buglabs.dragonfly.ui.actions;

import java.beans.PropertyChangeEvent;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.util.BugWSHelper;

/**
 * Removes selected bundles from the BUG and the view
 * @author akravets
 *
 */
public class RemoveBundleJob extends Job{
	public static final String ACTION_ID = "com.buglabs.dragonfly.ui.actions.RemoveBundleActionDelegate"; //$NON-NLS-1$
	private static final IStatus OK = new Status(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, "", null);
	
	private boolean isRemoveBundle = false;
	private ProgramNode[] nodes; // array of nodes representing bundles to be removed
	
	/**
	 * Constructor
	 * @param name name of this job
	 * @param nodes array of bundles to be removed
	 */
	public RemoveBundleJob(String name, ProgramNode[] nodes) {
		super(name);
		this.nodes = nodes;
	}

	protected IStatus run(IProgressMonitor monitor) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				String message = "Remove ";
				if(nodes.length > 1)
					message += nodes.length + " applications?";
				else
					message += nodes[0].getName() + "?";
				isRemoveBundle  = MessageDialog.openQuestion(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
						"Remove application", message);
			}

		});

		if(!isRemoveBundle)
			return OK;

		try {
			monitor.beginTask("Removing applications", nodes.length);
			
			for(int i = 0; i < nodes.length; i++){
				ProgramNode application = (ProgramNode)nodes[i];

				String packageUrl = application.getPackageUrl().toExternalForm();
				String url = packageUrl.substring(0, packageUrl.lastIndexOf("/") + 1); //$NON-NLS-1$

				BugWSHelper.deleteProgram(new URL(url + application.getName().replace(' ', '+')).toExternalForm()); //$NON-NLS-1$
				monitor.worked(1);
				DragonflyActivator.getDefault().fireModelChangeEvent(new PropertyChangeEvent(this, "remove_bundle", null, application));						
			}
			monitor.done();
			return OK;
		} catch (Exception e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Unable to remove application(s)", e); //$NON-NLS-1$
		}
	}

}
