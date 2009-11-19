package com.buglabs.dragonfly.ui.views.mybugs;

import java.beans.PropertyChangeEvent;
import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.IModelChangeListener;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.Module;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.model.ServiceNode;
import com.buglabs.dragonfly.ui.jobs.ConnectBugHelper;
import com.buglabs.dragonfly.ui.jobs.Messages;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * @author akravets
 * 
 */
public class MyBugsViewContentProvider implements ITreeContentProvider, IModelChangeListener {

	private Viewer viewer;

	public MyBugsViewContentProvider() {
		DragonflyActivator.getDefault().addListener(this);
	}

	public Object[] getChildren(Object parentElement) {
		// try to connect to bug when bug is selected
		if (parentElement instanceof BugConnection 
				&& !((BugConnection) parentElement).isConnected()) {
			ConnectBugHelper.connectToBug((BugConnection) parentElement, false);
		} 
		
		else if (parentElement instanceof ITreeNode) {
			Collection children = ((ITreeNode) parentElement).getChildren();
			if (children == null) {
				UIUtils.handleVisualError(Messages.getString("BugContentProvider.5"), null); //$NON-NLS-1$
				return null;
			}
			return children.toArray();
		}
		return null;
	}

	public Object getParent(Object element) {
		if (element instanceof ITreeNode) {
			return ((ITreeNode) element).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ProgramNode 
				|| element instanceof Module 
				|| element instanceof ServiceNode)
			return false;
		if (element instanceof BugConnection 
				&& !((BugConnection) element).isConnected())
			return false;
		return true;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	/**
	 * This is IModelListener event used to keep MyBugs View up to date
	 */
	public void propertyChange(final PropertyChangeEvent event) {
		if (viewer == null || viewer.getControl().isDisposed())
			return;
		viewer.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				if (event.getNewValue() instanceof BugConnection) {
					BugConnection leBug = (BugConnection) event.getNewValue();
					// Add Bug, connect to it, connectToBug will handle refresh
					if (event.getPropertyName().equals(BugConnectionManager.ADD_BUG)) {
						ConnectBugHelper.connectToBug(leBug, true);
						return;
					}
				}
				viewer.refresh();
			}
		});
	}

}
