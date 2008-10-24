package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.views.mybugs.MyBugsView;

public class BugDeleteConnectionAction extends Action {

	private TreeViewer viewer;

	public BugDeleteConnectionAction(TreeViewer viewer) {
		this.setText("Delete Connection");
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_CONNECTION_DELETE));
		this.viewer = viewer;
	}

	public void run() {
		String title = "Delete BUG Connection";
		String message = ""; //$NON-NLS-1$
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

		BugConnection element = (BugConnection) selection.getFirstElement();

		if (selection.size() > 1) {
			title += "s";
			message = "Are you sure you want to delete these " + selection.size() + " BUGs?";
		} else {
			message = "Are you sure you want to delete BUG Connection '" + element.getName() + "'?";
		}

		if (MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, message)) {
			if (selection.size() > 1) {
				MyBugsView.getRoot().getChildren().removeAll(selection.toList());
				Activator.getDefault().getNoConnectList().removeAll(selection.toList());
			} else {
				MyBugsView.getRoot().getChildren().remove(element);
			}
			viewer.refresh();
		}
	}

	public boolean isEnabled() {
		Object element = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		if (element instanceof StaticBugConnection)
			return true;
		return false;
	}
}
