package com.buglabs.dragonfly.ui.dialogs;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.buglabs.dragonfly.ui.util.BugProjectSelectionManager;
import com.buglabs.dragonfly.ui.util.BugProjectUtil;



public class SelectWorkspaceProjectsDialog extends Dialog {

	private String[] bug_project_names;
	CheckboxTableViewer viewer;
	
	public SelectWorkspaceProjectsDialog(Shell parentShell) {
		super(parentShell);
		bug_project_names = (String[])BugProjectUtil.getBugProjectNames().toArray(new String[0]);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		Group projectsGroup = new Group(parent, SWT.NONE);
		projectsGroup.setText("Select Applications to Run");
		GridData gdProjects = new GridData(GridData.FILL_BOTH);
		projectsGroup.setLayoutData(gdProjects);
		projectsGroup.setLayout(new GridLayout(1, false));
		
		
		// table with list of services to choose from
		Table modulesTable = new Table(projectsGroup ,SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		modulesTable.setHeaderVisible(false);

		TableColumn col1 = new TableColumn(modulesTable, SWT.NONE);
		col1.setWidth(200);
		col1.setText("Project Name");

		TableLayout tableLayout = new TableLayout();
		modulesTable.setLayout(tableLayout);

		GridData viewerData = new GridData(GridData.FILL_BOTH);
		viewerData.horizontalSpan = 1;
		viewerData.heightHint = 200;

		viewer = new CheckboxTableViewer(modulesTable);
		viewer.getControl().setLayoutData(viewerData);
		viewer.setContentProvider(new ProjectsContentProvider());
		viewer.setLabelProvider(new ProjectsLabelProvider());
		viewer.setInput(bug_project_names);
		
		String[] selectedProjects = BugProjectSelectionManager.getInstance().getSelectedProjectNames();
		if (selectedProjects == null)
			viewer.setAllChecked(true);
		else 
			viewer.setCheckedElements(selectedProjects);
		
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				BugProjectSelectionManager.getInstance().setSelectedProjectNames(
						Arrays.asList(viewer.getCheckedElements()).toArray(new String[0]));
			}
		});
		
		return projectsGroup;
	}

	class ProjectsContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object input) {
			if (input instanceof String[]) {
				return (String[])input;
			}
			return null;
		}
		public void inputChanged(Viewer viewer,	Object oldInput, Object newInput) {
			// don't need to hang onto input for this example, so do nothing
		}
		public void dispose() {
		}
	}

	class ProjectsLabelProvider extends LabelProvider implements ITableLabelProvider {
		private final static String DELIM = "."; //$NON-NLS-1$
		public String getColumnText(Object element, int columnIndex) {
			String casted = (String)element;
			switch (columnIndex) {
			case 0 :
				return casted;
			default :
				return "";
			}
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}	
	
}
