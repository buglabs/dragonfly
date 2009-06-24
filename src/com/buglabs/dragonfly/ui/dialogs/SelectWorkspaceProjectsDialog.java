package com.buglabs.dragonfly.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ColorRegistry;
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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

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

	private static final String HYPERLINKCOLOR 		= "HYPERLINKCOLOR";
	private static final String DIALOG_TITLE_TXT	= "Select Applications to Run";
	private static final String SELECT_ALL_TXT 		= "Select All";
	private static final String SELECT_NONE_TXT		= "Select None";
	private static ColorRegistry color_registry = null;
	private String[] 			bug_project_names;
	private CheckboxTableViewer projects_viewer;
	
	public SelectWorkspaceProjectsDialog(Shell parentShell) {
		super(parentShell);
		List projectNames = BugProjectUtil.getBugProjectNames();
		// initialize with all apps listed
		bug_project_names = 
			(String[]) projectNames.toArray(new String[projectNames.size()]);
	}

	private void createColorRegistry(Composite parent) {
		if (color_registry == null)
			color_registry = new ColorRegistry(parent.getDisplay());
		
		if (!color_registry.hasValueFor(HYPERLINKCOLOR))
			color_registry.put(HYPERLINKCOLOR, new RGB(98,83,125));
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		createColorRegistry(parent);
		
		Composite projectsControl = new Composite(parent, SWT.NONE);
		GridData gdProjects = new GridData(GridData.FILL_BOTH);
		projectsControl.setLayoutData(gdProjects);
		projectsControl.setLayout(new GridLayout(1, false));
		
		Label label = new Label(projectsControl, SWT.TOP);
		label.setText(DIALOG_TITLE_TXT);
		
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
		if (selectedProjects == null) selectAll();
		else projects_viewer.setCheckedElements(selectedProjects);
		
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
		
		// draw select all/none hyperlinks
		Composite hyperLinkHolder = new Composite(projectsControl, SWT.NONE);
		
	    GridLayout layoutHL = new GridLayout(2, false);
	    layoutHL.marginTop = layoutHL.marginBottom = 0;
	    layoutHL.verticalSpacing = 0;
	    layoutHL.marginRight = 5;
		hyperLinkHolder.setLayout(layoutHL);
		GridData gdHL = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		gdHL.horizontalAlignment = SWT.FILL;
		hyperLinkHolder.setLayoutData(gdHL);	
		
		Hyperlink selectAllLink = new Hyperlink(hyperLinkHolder, SWT.LEFT);
		selectAllLink.setText(SELECT_ALL_TXT);
		selectAllLink.setToolTipText(SELECT_ALL_TXT);
		selectAllLink.setUnderlined(true);
		selectAllLink.setForeground(color_registry.get(HYPERLINKCOLOR));
		selectAllLink.addHyperlinkListener(new SelectionHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				selectAll();
			}
		});

		Hyperlink selectNoneLink = new Hyperlink(hyperLinkHolder, SWT.RIGHT);
		selectNoneLink.setText(SELECT_NONE_TXT);
		selectNoneLink.setToolTipText(SELECT_NONE_TXT);
		selectNoneLink.setUnderlined(true);
		selectNoneLink.setForeground(color_registry.get(HYPERLINKCOLOR));
		selectNoneLink.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		selectNoneLink.addHyperlinkListener(new SelectionHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				selectNone();
			}
		});
		
		return projectsControl;
	}

	private void selectAll() {
		projects_viewer.setAllChecked(true);
		BugProjectSelectionManager.getInstance().setSelectedProjectNames(bug_project_names);
	}
	
	private void selectNone() {
		projects_viewer.setAllChecked(false);
		BugProjectSelectionManager.getInstance().setSelectedProjectNames(new String[0]);
	}
	
	
	abstract class SelectionHyperlinkListener implements IHyperlinkListener {
		// not needed for this functionality
		public void linkEntered(HyperlinkEvent e) {}
		public void linkExited(HyperlinkEvent e) {}
		
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
