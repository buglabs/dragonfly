package com.buglabs.dragonfly.dm.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.ui.actions.RemoveBundleJob;

/**
 * Uninstall an application bundle.
 * @author kgilmer
 *
 */
public class UninstallAction extends Action {
	private final IStructuredSelection selection;
	private final ProgramNode node;

	public UninstallAction(ProgramNode node, IStructuredSelection selection) {
		super("Uninstall Bundle");
		this.node = node;
		this.selection = selection;
	}

	@Override
	public void run() {
		RemoveBundleJob job = new RemoveBundleJob("Uninstall bundle " + node.getName(), new ProgramNode[] { node });
		job.schedule();
	}
}