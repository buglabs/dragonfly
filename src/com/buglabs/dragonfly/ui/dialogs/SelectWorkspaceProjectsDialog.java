package com.buglabs.dragonfly.ui.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.buglabs.dragonfly.ui.util.BugProjectSelectionManager;
import com.buglabs.dragonfly.ui.util.BugProjectUtil;


/**
 * Dialog to select the workspace projects you'd like to load
 * Modifies BugProjectSelectionManager's list of projects
 * 
 * @author brian
 *
 */
public class SelectWorkspaceProjectsDialog extends Dialog {

	private String[] 	bug_project_names;
	CheckboxTableViewer projects_viewer;
	
	public SelectWorkspaceProjectsDialog(Shell parentShell) {
		super(parentShell);
		List projectNames = BugProjectUtil.getBugProjectNames();
		// initialize with all apps listed
		bug_project_names = 
			(String[]) projectNames.toArray(new String[projectNames.size()]);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite projectsControl = new Composite(parent, SWT.NONE);
		GridData gdProjects = new GridData(GridData.FILL_BOTH);
		projectsControl.setLayoutData(gdProjects);
		projectsControl.setLayout(new GridLayout(1, false));
		
		Label label = new Label(projectsControl, SWT.TOP);
		label.setText("Select Applications to Run");
		
		Table projectsTable = new Table(projectsControl ,SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		projectsTable.setHeaderVisible(false);

		TableColumn col1 = new TableColumn(projectsTable, SWT.NONE);
		col1.setWidth(200);

		TableLayout tableLayout = new TableLayout();
		projectsTable.setLayout(tableLayout);

		GridData viewerData = new GridData(GridData.FILL_HORIZONTAL);
		viewerData.horizontalSpan = 1;
		viewerData.heightHint = 200;

		// set up jface component
		projects_viewer = new CheckboxTableViewer(projectsTable);
		projects_viewer.getControl().setLayoutData(viewerData);
		projects_viewer.setContentProvider(new ProjectsContentProvider());
		projects_viewer.setLabelProvider(new ProjectsLabelProvider());
		projects_viewer.setInput(bug_project_names);
		
		// get the stored state of selected elements or select all
		String[] selectedProjects = 
			BugProjectSelectionManager.getInstance().getSelectedProjectNames();
		if (selectedProjects == null)
			projects_viewer.setAllChecked(true);
		else 
			projects_viewer.setCheckedElements(selectedProjects);
		
		// update the selection manager when things checked/unchecked
		projects_viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object[] elements = projects_viewer.getCheckedElements();
				String[] names = new String[elements.length];
				for (int i = 0; i < elements.length; i++) {
					names[i] = (String) elements[i];
				}
				BugProjectSelectionManager.getInstance().setSelectedProjectNames(names);
			}
		});
		
		return projectsControl;
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
