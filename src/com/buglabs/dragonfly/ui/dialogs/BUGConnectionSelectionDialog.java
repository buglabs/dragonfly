package com.buglabs.dragonfly.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.model.BaseTreeNode;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.DiscoveredBugConnection;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.model.VirtualBUGConnection;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.views.mybugs.MyBugsViewComparator;

/**
 * Queries the workspace for existing BUG Connection projects and requests from
 * the user to select one.
 * 
 * @author Angel Roman
 * 
 */
public class BUGConnectionSelectionDialog extends Dialog {

	BugConnection selectedBugConnection;

	public BUGConnectionSelectionDialog(Shell parentShell) {
		super(parentShell);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("BUG Connections");

	}

	protected Control createDialogArea(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout(1, false));

		Label lblMessage = new Label(top, SWT.NONE);
		lblMessage.setText("Please specify a BUG Connection");

		GridData gdFillBoth = new GridData(GridData.FILL_BOTH);
		GridData gdViewer = GridDataFactory.createFrom(gdFillBoth).create();
		gdViewer.minimumWidth = 300;
		gdViewer.minimumHeight = 400;
		TreeViewer viewer = new TreeViewer(top, SWT.BORDER);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				selectedBugConnection = (BugConnection) ((ITreeSelection) event.getSelection()).getFirstElement();
			}
		});

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				BUGConnectionSelectionDialog.this.okPressed();
			}
		});
		viewer.getControl().setLayoutData(gdViewer);
		viewer.setContentProvider(new ITreeContentProvider() {

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof BaseTreeNode) {
					return ((BaseTreeNode) parentElement).getChildren().toArray();
				}

				return new Object[0];
			}

			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			public Object getParent(Object element) {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean hasChildren(Object element) {
				if (element instanceof BugConnection)
					return false;

				if (element instanceof BaseTreeNode) {
					if (((BaseTreeNode) element).hasChildren()) {
						return true;
					}
				}
				return false;
			}

			public void dispose() {
				// TODO Auto-generated method stub

			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub

			}

		});

		viewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof BugConnection) {
					return ((BugConnection) element).getName() + 
						" [" + ((BugConnection) element).getUrl().getHost() + "]";
				} else {
					return super.getText(element);
				}
			}

			public Image getImage(Object element) {
				if (element instanceof VirtualBUGConnection) {
					return Activator.getDefault().getImageRegistry().get(Activator.ICON_VIRTUAL_BUG);
				}
				else if(element instanceof StaticBugConnection){
					return Activator.getDefault().getImageRegistry().get(Activator.ICON_STATIC_BUG);
				}
				else if(element instanceof DiscoveredBugConnection){
					return Activator.getDefault().getImageRegistry().get(Activator.ICON_DISCOVERED_BUG);
				}
				return super.getImage(element);
			}
		});
		
		viewer.setComparator(new MyBugsViewComparator());

		BaseTreeNode root = 
			(BaseTreeNode) BugConnectionManager.getInstance().getBugConnectionsRoot();
		viewer.setInput(root);
		return top;
	}

	public BugConnection getSelectedBugConnection() {
		return selectedBugConnection;
	}
}
