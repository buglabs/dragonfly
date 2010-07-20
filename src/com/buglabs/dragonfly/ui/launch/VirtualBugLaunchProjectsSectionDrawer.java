package com.buglabs.dragonfly.ui.launch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.buglabs.dragonfly.ui.util.BugProjectUtil;

/**
 * Dialog to select the workspace projects you'd like to load Modifies
 * BugProjectSelectionManager's list of projects
 * 
 * @author brian
 * 
 */
public class VirtualBugLaunchProjectsSectionDrawer {

	private static final String HYPERLINKCOLOR = "HYPERLINKCOLOR";
	private static final String DIALOG_TITLE_TXT = "Automatically launch with all workspace applications";
	private static final String SELECT_ALL_TXT = "Select All";
	private static final String SELECT_NONE_TXT = "Select None";
	private static int VIEWER_HEIGHT_HINT = 75;
	private static ColorRegistry color_registry = null;
	private Button launch_all_button = null;
	private CheckboxTableViewer projects_viewer = null;
	private Table projects_table = null;
	private List<ILaunchProjectSelectionListener> selection_listeners = new ArrayList<ILaunchProjectSelectionListener>();

	/**
	 * Local method to handle the static color registry
	 * 
	 * @param parent
	 */
	private void createColorRegistry(Composite parent) {
		if (color_registry == null)
			color_registry = new ColorRegistry(parent.getDisplay());

		if (!color_registry.hasValueFor(HYPERLINKCOLOR))
			color_registry.put(HYPERLINKCOLOR, new RGB(98, 83, 125));
	}

	/**
	 * Call this to draw the Launch Projects Secion in the given Composite
	 * 
	 * @param parent
	 */
	protected void draw(Composite parent) {
		createColorRegistry(parent);

		Composite projectsControl = new Composite(parent, SWT.NONE);
		GridData gdProjects = new GridData(GridData.FILL_BOTH);
		gdProjects.horizontalSpan = 2;
		projectsControl.setLayoutData(gdProjects);
		projectsControl.setLayout(new GridLayout(1, false));

		launch_all_button = new Button(projectsControl, SWT.CHECK);
		launch_all_button.setText(DIALOG_TITLE_TXT);
		launch_all_button.setSelection(true);
		launch_all_button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				updateProjectsTable();
				notifyChangeListeners();
			}
		});

		projects_table = new Table(projectsControl, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		projects_table.setHeaderVisible(false);

		TableColumn col1 = new TableColumn(projects_table, SWT.NONE);
		col1.setWidth(200);

		TableLayout tableLayout = new TableLayout();
		projects_table.setLayout(tableLayout);

		GridData viewerData = new GridData(GridData.FILL_HORIZONTAL);
		viewerData.horizontalSpan = 1;
		viewerData.heightHint = VIEWER_HEIGHT_HINT;

		// set up jface component
		projects_viewer = new CheckboxTableViewer(projects_table);
		projects_viewer.getControl().setLayoutData(viewerData);
		projects_viewer.setContentProvider(new ProjectsContentProvider());
		projects_viewer.setLabelProvider(new ProjectsLabelProvider());
		List<String> projectNames = BugProjectUtil.getWSBugProjectNames();
		projects_viewer.setInput((String[]) projectNames.toArray(new String[projectNames.size()]));
		projects_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				notifyChangeListeners();
			}
		});

		// draw select all/none hyperlinks
		Composite hyperLinkHolder = new Composite(projectsControl, SWT.NONE);

		GridLayout layoutHL = new GridLayout(2, false);
		layoutHL.marginTop = layoutHL.marginBottom = 0;
		layoutHL.verticalSpacing = 0;
		layoutHL.marginRight = 5;
		hyperLinkHolder.setLayout(layoutHL);
		GridData gdHL = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		gdHL.horizontalAlignment = SWT.LEFT;
		hyperLinkHolder.setLayoutData(gdHL);

		Hyperlink selectAllLink = new Hyperlink(hyperLinkHolder, SWT.LEFT);
		selectAllLink.setText(SELECT_ALL_TXT);
		selectAllLink.setToolTipText(SELECT_ALL_TXT);
		selectAllLink.setUnderlined(true);
		selectAllLink.setForeground(color_registry.get(HYPERLINKCOLOR));
		selectAllLink.addHyperlinkListener(new SelectionHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				if (projects_table.isEnabled()) {
					projects_viewer.setAllChecked(true);
					notifyChangeListeners();
				}
			}
		});

		Hyperlink selectNoneLink = new Hyperlink(hyperLinkHolder, SWT.LEFT);
		selectNoneLink.setText(SELECT_NONE_TXT);
		selectNoneLink.setToolTipText(SELECT_NONE_TXT);
		selectNoneLink.setUnderlined(true);
		selectNoneLink.setForeground(color_registry.get(HYPERLINKCOLOR));
		selectNoneLink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		selectNoneLink.addHyperlinkListener(new SelectionHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				if (projects_table.isEnabled()) {
					projects_viewer.setAllChecked(false);
					notifyChangeListeners();
				}
			}
		});

	}

	/**
	 * Keep listeners who will respond to changes in the Launch-Projects
	 * Selection section
	 * 
	 * @param listener
	 */
	public void addChangeListener(ILaunchProjectSelectionListener listener) {
		if (!selection_listeners.contains(listener))
			selection_listeners.add(listener);
	}

	public void setSelectedProjects(List<String> selectedProjects) {
		if (projects_viewer == null)
			return;
		projects_viewer.setCheckedElements(selectedProjects.toArray());
	}

	public List<String> getSelectedProjects() {
		Object[] elements = projects_viewer.getCheckedElements();
		String[] names = new String[elements.length];
		for (int i = 0; i < elements.length; i++) {
			names[i] = (String) elements[i];
		}
		return Arrays.asList(names);
	}

	/**
	 * When this flag is set, it means to launch the BUG Simulator with all
	 * workspace projects selected This is the default
	 * 
	 * @param val
	 */
	public void setLaunchAllProjectsFlag(boolean val) {
		if (launch_all_button != null)
			launch_all_button.setSelection(val);
		updateProjectsTable();
	}

	public boolean getLaunchAllProjectsFlag() {
		if (launch_all_button == null)
			return false;
		return launch_all_button.getSelection();
	}

	private void notifyChangeListeners() {
		for (ILaunchProjectSelectionListener listener : selection_listeners) {
			listener.projectSelectionChanged();
		}
	}

	private void updateProjectsTable() {
		if (projects_table != null && launch_all_button != null)
			projects_table.setEnabled(!launch_all_button.getSelection());
	}

	/**
	 * abstract class to hide unimplemented methods and make in-line listener
	 * creation less ugly
	 */
	abstract class SelectionHyperlinkListener implements IHyperlinkListener {
		// not needed for this functionality
		public void linkEntered(HyperlinkEvent e) {
		}

		public void linkExited(HyperlinkEvent e) {
		}
	}

	class ProjectsContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object input) {
			if (input instanceof String[]) {
				return (String[]) input;
			}
			return null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// don't need to hang onto input for this example, so do nothing
		}

		public void dispose() {
		}
	}

	class ProjectsLabelProvider extends LabelProvider implements ITableLabelProvider {
		private final static String DELIM = "."; //$NON-NLS-1$

		public String getColumnText(Object element, int columnIndex) {
			String casted = (String) element;
			switch (columnIndex) {
			case 0:
				return casted;
			default:
				return "";
			}
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	interface ILaunchProjectSelectionListener {
		public void projectSelectionChanged();
	}
}
