package com.buglabs.dragonfly.ui.wizards.bugProject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.buglabs.dragonfly.ui.info.BugProjectInfo;
import com.buglabs.dragonfly.ui.info.ServicePropertyHelper;

/**
 * A dialog for selecting properties for filtering in a Bug App's Service
 * Tracker
 * 
 * @author bballantine
 * 
 */
public class ServicePropertySelectorDialog extends Dialog {

	private static final String SERVICE_PROPERTIES_LABEL = "Add Properties to Filter for";
	private static final String WINDOW_TITLE = "Service Properties";
	private static final String KEY_LABEL = "Key";
	private static final String VALUE_LABEL = "Value";
	private static final char DELIM = '.';

	private static final int SERVICES_GROUP_HEIGHT_HINT = 300;
	private static final int SERVICES_GROUP_WIDTH_HINT = 400;
	private static final int SERVICES_GROUP_MARGIN = 10;

	private CheckboxTableViewer service_properties_viewer;
	private String selected_service = "";
	private BugProjectInfo pinfo;
	private PropertyValueEditingSupport editing_support;

	private List<ServicePropertyHelper> selected_service_properties;
	private List<ServicePropertyHelper> service_property_options;

	/**
	 * Creates instance of dialog, Needs a reference to List of
	 * ServicePropertyHelpers for loading TableViewer and a reference to
	 * BugProjectIngo for storing/managing data
	 * 
	 * @param parentShell
	 * @param selectedService
	 *            String representing the full service name of the selected
	 *            service (i.e. com.buglabs.module.pub.IModletFactory)
	 * @param servicePropertyHelpers
	 *            List of ServicePropertyHelper objects which are properties for
	 *            the given service. This is used as the input to the jface
	 *            tableviewer
	 * @param projectInfo
	 *            This is where the results of clicking stuff get stored
	 * 
	 */
	protected ServicePropertySelectorDialog(Shell parentShell, String selectedService, List<ServicePropertyHelper> servicePropertyOptions, BugProjectInfo projectInfo) {

		super(parentShell);
		selected_service = selectedService;
		pinfo = projectInfo;
		service_property_options = servicePropertyOptions;

		// initialize our tally of checked service properties
		if (pinfo.getServicePropertyHelperMap().containsKey(selectedService)) {
			selected_service_properties = new ArrayList<ServicePropertyHelper>(pinfo.getServicePropertyHelperMap().get(selectedService));
		} else {
			selected_service_properties = new ArrayList<ServicePropertyHelper>();
		}

	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(WINDOW_TITLE);
	}

	@Override
	protected void okPressed() {
		// commit changes in editing_support
		if (editing_support != null)
			editing_support.commitChanges();

		// update pinfo w/ selected services
		pinfo.getServicePropertyHelperMap().put(selected_service, selected_service_properties);
		super.okPressed();
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		// box that surrounds this section
		Group serviceProps = new Group(parent, SWT.NONE);
		serviceProps.setText(SERVICE_PROPERTIES_LABEL + " " + selected_service.substring(selected_service.lastIndexOf(DELIM) + 1, selected_service.length()));
		GridData gdServices = new GridData(GridData.FILL_BOTH);
		gdServices.heightHint = SERVICES_GROUP_HEIGHT_HINT;
		gdServices.widthHint = SERVICES_GROUP_WIDTH_HINT;
		gdServices.grabExcessHorizontalSpace = true;
		gdServices.verticalIndent = SERVICES_GROUP_MARGIN;
		serviceProps.setLayoutData(gdServices);
		GridLayout propsLayout = new GridLayout(1, false);
		propsLayout.marginWidth = propsLayout.marginHeight = SERVICES_GROUP_MARGIN;
		serviceProps.setLayout(propsLayout);

		// table with list of properties to choose from
		final Table propertiesTable = new Table(serviceProps, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		propertiesTable.setHeaderVisible(true);
		propertiesTable.setLinesVisible(true);

		TableLayout propTableLayout = new TableLayout();
		propTableLayout.addColumnData(new ColumnWeightData(90));
		propTableLayout.addColumnData(new ColumnWeightData(120));
		propertiesTable.setLayout(propTableLayout);

		GridData pViewerData = new GridData(GridData.FILL_BOTH);
		propertiesTable.setLayoutData(pViewerData);

		// viewer for services list
		service_properties_viewer = new CheckboxTableViewer(propertiesTable);
		service_properties_viewer.setContentProvider(new ServicePropsContentProvider());

		service_properties_viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateSelectedServiceProperties();
			}
		});

		// col0 is taken care of by checkboxtableviewer
		TableViewerColumn col0viewer = new TableViewerColumn(service_properties_viewer, SWT.FULL_SELECTION, 0);
		col0viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ServicePropertyHelper) element).getKey();
			}
		});
		col0viewer.getColumn().setText(KEY_LABEL);

		// col1 has custom cell editors defined in EditingSupport below
		TableViewerColumn col1viewer = new TableViewerColumn(service_properties_viewer, SWT.FULL_SELECTION, 1);
		editing_support = new PropertyValueEditingSupport(col1viewer.getViewer());
		col1viewer.setEditingSupport(editing_support);
		col1viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				// Editing support keeps track of temp values
				// so ask editing support for the label
				return editing_support.getLabel((ServicePropertyHelper) element);
			}
		});
		col1viewer.getColumn().setText(VALUE_LABEL);

		//set Input
		service_properties_viewer.setInput(service_property_options);

		// set the checked elements
		service_properties_viewer.setCheckedElements(selected_service_properties.toArray());

		return serviceProps;
	}

	/**
	 * Freshen the model based on the properties viewer
	 * 
	 * @param selected_service
	 */
	private void updateSelectedServiceProperties() {
		selected_service_properties.clear();
		for (Object element : service_properties_viewer.getCheckedElements()) {
			selected_service_properties.add((ServicePropertyHelper) element);
		}
	}

	/* INNER CLASSES */

	/**
	 * Content provider for Service Properties viewer
	 * 
	 * @author bballantine
	 * 
	 */
	class ServicePropsContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object input) {
			if (input instanceof List)
				return ((List) input).toArray();
			return null;
		}

		public void dispose() {/* unused */
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			/* unused */
		}
	}

}
