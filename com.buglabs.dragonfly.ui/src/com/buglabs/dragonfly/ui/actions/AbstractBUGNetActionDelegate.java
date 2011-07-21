package com.buglabs.dragonfly.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.felix.launch.ProjectUtils;
import com.buglabs.dragonfly.ui.Activator;

/**
 * An action delegate that responds to current state of BUGnet's enable status.
 * Actions that extends this class will be disabled if BUGnet view is disabled
 * in preferences, will be enabled otherwise. <br>
 * Clients that wish to take advantage of this functionality, should implement
 * </code>IActionDelegate</code>'s run() method.
 * 
 * @author akravets
 * 
 */
public abstract class AbstractBUGNetActionDelegate implements IActionDelegate {

	protected IProject project;

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection && DragonflyActivator.getDefault().getPluginPreferences().getBoolean(DragonflyActivator.PREF_BUGNET_ENABLED)
				&& Activator.getDefault().isConnectionAvailable()) {
			action.setEnabled(true);
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IAdaptable) {
				project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
			}
		} else {
			action.setEnabled(false);
		}

		if (action.isEnabled()) {
			// disable sending to BUG if project has errors
			try {
				if (ProjectUtils.existsProblems(project)) {
					action.setEnabled(false);
					//UIUtils.handleNonvisualError("Uploading to BUGnet and BUG has been suspended due to errors with project " + project.getName() + ". Please fix errors and try again.", null);
				}
			} catch (CoreException e) {
				action.setEnabled(true);
			}
		}
	}

}
