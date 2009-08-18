/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.wizards.bugProject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BaseTreeNode;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ServiceDetail;
import com.buglabs.dragonfly.model.ServiceProperty;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.filters.ServiceFilter;
import com.buglabs.dragonfly.ui.info.BugProjectInfo;
import com.buglabs.dragonfly.ui.info.ServicePropertyHelper;
import com.buglabs.dragonfly.ui.launch.VirtualBugLaunchShortCut;
import com.buglabs.dragonfly.ui.views.mybugs.MyBugsViewContentProvider;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;
/**
 * Wizard page that displays currently running BUGs and allows user to include services from a particular
 * BUG in the project
 * 
 * @author akravets and then... bballantine!
 *
 */
public class CodeGenerationPage extends WizardPage {

	// titles, labels, tooltips, etc
	private static final String BUGLABS_EMULATOR_BUNDLE_NAME= "com.buglabs.bug.emulator";
	private static final String PAGE_NAME 					= "CodeGenerationPage";
	private static final String PAGE_TITLE 					= "Service Definition";
	private static final String TARGET_BUG_TITLE 			= "Target BUG";
	private static final String TARGET_BUG_INSTRUCTIONS 	= "Select a BUG from the list below to be used as a target";
	private static final String START_VIRTUAL_BUG_LABEL 	= "Start &Virtual BUG";
	private static final String START_VIRTUAL_BUG_TOOLTIP 	= "Start Virtual BUG and start consuming its services";
	private static final String REQUIRED_SERVICES_TITLE 	= "Required Services";
	
	private static final String NAME_LABEL 					= "Name";
	private static final String PACKAGE_LABEL 				= "Package";
	
	private static final String SELECT_ALL_LABEL 			= "&Select All";
	private static final String SELECT_ALL_TOOLTIP 			= "Select all services";
	private static final String DESELECT_ALL_LABEL 			= "&Deselect All";
	private static final String DESELECT_ALL_TOOLTIP		= "Deselect all services";
	private static final String REFRESH_SERVICES_LABEL 		= "&Refresh Services"; 
	private static final String REFRESH_SERVICES_TOOLTIP 	= "Refreshes services definitions provided by the target BUG";
	private static final String SERVICE_DESCRIPTION_LABEL 	= "Service Description";
	
	private static final String RELOAD_LIST_WARN_TITLE		= "Reloading Services List";
	private static final String RELOAD_LIST_WARN_MESSAGE	= "Any selections you've made will be lost.  Would you like to continue?";
	
	private static final String LAUNCH_VBUG_MESSAGE = "Launch Virtual BUG to select services that this project will consume.";
	private static final String SELECT_BUG_MESSAGE 	= "Select a BUG from Target BUG List to choose services that this project will consume.";
	
	private static final int BUGS_VIEWER_HEIGHT_HINT 		= 100;
	private static final int SERVICES_GROUP_HEIGHT_HINT 	= 350;
	private static final int SERVICES_GROUP_WIDTH_HINT 		= 550;
	private static final int DEPENDENCY_VIEWER_HEIGHT_HINT 	= 200;
	private static final int SERVICE_DESCRIPTION_AREA_HEIGHT= 100;

	// UI elements
	private TableViewer bugsViewer;
	private CheckboxTableViewer dependencyViewer;
	private CheckboxTableViewer servicePropertiesViewer;
	private Text serviceDescriptionArea;
	private Button btnStartVBUG;
	private Button btnGenerateThreadApp;
	private Button refreshServiceDefintions;
	
	// instance vars to keep track of stuff
	
	// this is used as the input for the service property viewer
	// it helps, keep track of the possible service properties values
	// for a given service (String hash key = service name)
	private Map<String, List<ServicePropertyHelper>> 
		servicePropertyOptionsMap = new HashMap<String, List<ServicePropertyHelper>>();
	private ServiceFilter serviceFilter = new ServiceFilter();
	private BugProjectInfo pinfo;
	private ISelection currentBugSelection = null;
	private String pageMessage = "";
	
	protected CodeGenerationPage(BugProjectInfo pinfo) {
		super(PAGE_NAME, PAGE_TITLE, 
				Activator.getDefault().getImageRegistry().getDescriptor(
						Activator.IMAGE_COLOR_DIALOG_PROJECT));
		setMessage(pageMessage);
		this.pinfo = pinfo;
	}

	/**
	 *  Draw all the parts on the wizard page
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		mainComposite.setLayout(layout);

		// this is the list of all the bugs you can see
		createTargetArea(mainComposite);
		// where you choose the services
		createServicesSection(mainComposite);
		// checkbox for creating application loop
		createApplicationLoop(mainComposite);
		setControl(mainComposite);
	}


	
	/* METHODS FOR DRAWING STUFF IN COMPOSITE */
	
	/**
	 * Creates TableViewer that has all BUGs currently available in MyBUGs view
	 * 
	 * @param composite
	 */
	private void createTargetArea(final Composite parent) {
		Group composite = new Group(parent, SWT.NONE);
		composite.setText(TARGET_BUG_TITLE);
		composite.setLayout(new GridLayout(2, false));

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;

		composite.setLayoutData(gridData);
		
		GridData gdLabel = new GridData(GridData.FILL_HORIZONTAL);
		gdLabel.horizontalSpan = 2;

		Label label = new Label(composite, SWT.NONE);
		label.setText(TARGET_BUG_INSTRUCTIONS);
		label.setLayoutData(gdLabel);

		GridData fillHorizontal = new GridData(GridData.FILL_HORIZONTAL);
		GridData gdViewer = GridDataFactory.createFrom(fillHorizontal).create();
		gdViewer.heightHint = BUGS_VIEWER_HEIGHT_HINT;
		bugsViewer = new TableViewer(composite, SWT.BORDER | SWT.V_SCROLL);
		bugsViewer.getTable().setLayoutData(gdViewer);
		
		// set up change listener
		bugsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				// not sure why this would be the case, but return if nothing there
				if (((BaseTreeNode)bugsViewer.getInput()).getChildren().size() == 0) return;
				
				// don't do anything if it's the same as the previous selection
				ISelection selection = event.getSelection();
				if (currentBugSelection != null && currentBugSelection.equals(selection)) return;
				
				if (!reloadListDialog(parent.getShell())) {
					if (currentBugSelection != null)
						event.getSelectionProvider().setSelection(currentBugSelection);
					return;
				}
				
				// Make sure we can connect to the given BUG
				final BugConnection connection = 
					(BugConnection) ((StructuredSelection) event.getSelection()).getFirstElement();
				if (connection == null) return;
				
				// set the saved currentBugSelection to the selection
				currentBugSelection = selection;
				
				// prepare to launch refresh services job
				refreshServiceDefintions.setEnabled(true);
				
				// clear selections
				clearSelections();
								
				launchRefreshServicesJob(connection);			
			}
			
		});
		
		bugsViewer.setContentProvider(new MyBugsViewContentProvider() {
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof BaseTreeNode) {
					return ((BaseTreeNode) parentElement).getChildren().toArray();
				}
				return new Object[0];
			}
		});

		bugsViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof BugConnection) {
					return ((BugConnection) element).getName();
				} else {
					return super.getText(element);
				}
			}

			public Image getImage(Object element) {
				if (element instanceof BugConnection) {
					return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_UPLOAD);
				}
				return super.getImage(element);
			}
		});

		BaseTreeNode root = (BaseTreeNode) Activator.getDefault().getBugsViewRoot();
		bugsViewer.setInput(root);

		btnStartVBUG = new Button(composite, SWT.PUSH);
		btnStartVBUG.setText(START_VIRTUAL_BUG_LABEL);
		btnStartVBUG.setToolTipText(START_VIRTUAL_BUG_TOOLTIP);
		GridData gdButton = new GridData();
		gdButton.verticalAlignment = SWT.TOP;
		btnStartVBUG.setLayoutData(gdButton);
		btnStartVBUG.addSelectionListener(
				((SelectionListener) new StartVBUGSelectionListener()));

		setPageMessage(root.getChildren().size());
	}

	/**
	 * Draws the section where you can filter services
	 * or select a service based on your selection above
	 * 
	 * @param mainComposite
	 */
	private void createServicesSection(final Composite mainComposite) {
		// set up
		Composite servicesComposite = new Composite(mainComposite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		servicesComposite.setLayout(layout);

		// box that surrounds this section
		Group compServices = new Group(servicesComposite, SWT.NONE);
		compServices.setText(REQUIRED_SERVICES_TITLE);
		GridData gdServices = new GridData(GridData.FILL_BOTH);
		gdServices.horizontalSpan = 2;
		gdServices.heightHint = SERVICES_GROUP_HEIGHT_HINT;
		gdServices.widthHint = SERVICES_GROUP_WIDTH_HINT;
		gdServices.grabExcessHorizontalSpace = true;
		compServices.setLayoutData(gdServices);
		compServices.setLayout(new GridLayout(3, false));

		// text box for services filtering
		Text txtFilter = new Text(compServices, SWT.BORDER);
		GridData gdFillH = new GridData(GridData.FILL_HORIZONTAL);
		servicesComposite.setLayoutData(gdFillH);
		GridData gdFillBoth = GridDataFactory.createFrom(gdFillH).create();
		gdFillBoth.horizontalSpan = layout.numColumns;
		txtFilter.setLayoutData(gdFillBoth);
		txtFilter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String filter = ((Text) e.widget).getText();
				filter =filter.replaceAll("\\*", ".*");
				serviceFilter.setPattern("(?i).*" + filter + ".*", 
						Arrays.asList(dependencyViewer.getCheckedElements()));
				dependencyViewer.refresh();
			}
		});

		// table with list of services to choose from
		Table modulesTable = new Table(
				compServices, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		modulesTable.setHeaderVisible(true);
		modulesTable.setLinesVisible(true);
		TableColumn col1 = new TableColumn(modulesTable, SWT.NONE);
		col1.setText(NAME_LABEL);
		TableColumn col2 = new TableColumn(modulesTable, SWT.NONE);
		col2.setText(PACKAGE_LABEL);
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(90));
		tableLayout.addColumnData(new ColumnWeightData(120));
		modulesTable.setLayout(tableLayout);

		// viewer for handling service dependency selection
		dependencyViewer = new CheckboxTableViewer(modulesTable);
		GridData viewerData = new GridData(GridData.FILL_BOTH);
		viewerData.horizontalSpan = layout.numColumns;
		viewerData.heightHint = DEPENDENCY_VIEWER_HEIGHT_HINT;
		dependencyViewer.getControl().setLayoutData(viewerData);
		dependencyViewer.setContentProvider(new ModulesContentProvider());
		dependencyViewer.setLabelProvider(new ModulesLabelProvider());
		dependencyViewer.setSorter(new ViewerSorter());
		dependencyViewer.addFilter(serviceFilter);
		
		dependencyViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				// return if null event
				if (event == null) return;
				ISelection selection = event.getSelection();
				
				// return if not IStructuredSelection
				if (!(selection instanceof IStructuredSelection)) return;
				String selectedService = 
					(String) ((IStructuredSelection) selection).getFirstElement();
				
				openServicePropertySelectorDialog(
						mainComposite.getShell(), selectedService, false);			
			}
		});
		
		dependencyViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event == null) return;
				String selectedService = (String) event.getElement();
				if (selectedService == null) return;
				
				// update the services list w/ new checked item
				updateModel();
				
				if (event.getChecked()) 
					openServicePropertySelectorDialog(
							mainComposite.getShell(), selectedService, true);	
			}
		});
		
		dependencyViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// return if null event
				if (event == null) return;
				ISelection selection = event.getSelection();
				
				// return if not IStructuredSelection
				if (!(selection instanceof IStructuredSelection)) return;
				String selectedService = 
					(String) ((IStructuredSelection) selection).getFirstElement();
				
				// return if selectedService is null
				if (selectedService == null) return;
				
				String descr = Activator.getServiceDescription(selectedService.trim());
				serviceDescriptionArea.setText(descr);
			}
		});
		
		Composite buttonComposite = new Composite(compServices, SWT.NONE);
		GridData buttonData = new GridData(GridData.FILL_VERTICAL);
		buttonData.verticalSpan = 3;
		buttonComposite.setLayoutData(buttonData);
		RowLayout buttonLayout = new RowLayout(SWT.VERTICAL);
		buttonLayout.fill = true;
		//buttonLayout.pack = false;
		buttonComposite.setLayout(buttonLayout);

		Button btnSelectAll = new Button(buttonComposite, SWT.PUSH);
		btnSelectAll.setText(SELECT_ALL_LABEL);
		btnSelectAll.setToolTipText(SELECT_ALL_TOOLTIP);
		btnSelectAll.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {/*unused here*/}
			public void widgetSelected(SelectionEvent e) {
				dependencyViewer.setAllChecked(true);
				updateModel();
			}
		});
		
		Button btnDeselectAll = new Button(buttonComposite, SWT.PUSH);
		btnDeselectAll.setText(DESELECT_ALL_LABEL);
		btnDeselectAll.setToolTipText(DESELECT_ALL_TOOLTIP);
		btnDeselectAll.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {/*unused here*/}
			public void widgetSelected(SelectionEvent e) {
				dependencyViewer.setAllChecked(false);
				updateModel();
			}
		});
		
		// refresh services button
		refreshServiceDefintions = new Button(buttonComposite, SWT.PUSH);
		refreshServiceDefintions.setText(REFRESH_SERVICES_LABEL);
		refreshServiceDefintions.setToolTipText(REFRESH_SERVICES_TOOLTIP);
		refreshServiceDefintions.setEnabled(false);
		refreshServiceDefintions.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {/*unused here*/}
			public void widgetSelected(SelectionEvent e) {
				final BugConnection connection = 
					(BugConnection) ((StructuredSelection) bugsViewer.getSelection()).getFirstElement();
				
				// if no connection, show error and return
				if (connection == null) {
					UIUtils.giveVisualInformation("Please select target BUG to refresh");
					return;
				}
				
				// warn user things are going to get cleared
				if (!reloadListDialog(mainComposite.getShell())) return;
				
				// clear pinfo's selections
				clearSelections();
				
				// must be connection, kick off job
				launchRefreshServicesJob(connection);	
			}
		});

		// set up service description area
		serviceDescriptionArea = new Text(compServices, 
				SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL );
		GridData descData = new GridData(GridData.FILL_BOTH);
		descData.heightHint = SERVICE_DESCRIPTION_AREA_HEIGHT;
		descData.horizontalSpan = 2;
		serviceDescriptionArea.setLayoutData(descData);
		serviceDescriptionArea.setEditable(false);
		serviceDescriptionArea.setForeground(
				compServices.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		serviceDescriptionArea.setText(SERVICE_DESCRIPTION_LABEL);

	}	

	/**
	 * Creates application loop button
	 * 
	 * @param mainComposite
	 */
	private void createApplicationLoop(Composite mainComposite) {
		btnGenerateThreadApp = new Button(mainComposite, SWT.CHECK);
		GridData genAppGD = new GridData(GridData.FILL_HORIZONTAL);
		genAppGD.horizontalSpan = 2;
		genAppGD.heightHint = 30;
		btnGenerateThreadApp.setLayoutData(genAppGD);
		btnGenerateThreadApp.setText("generate application loop");
		btnGenerateThreadApp.setEnabled(false);
		btnGenerateThreadApp.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {/*unused here*/}
			public void widgetSelected(SelectionEvent e) {
				pinfo.setShouldGenerateApplicationLoop(((Button)e.widget).getSelection());
			}
		});
	}	
	
	
	/* HELPER METHODS */
	
	/**
	 * Clear stuff in pinfo and stuff in dependencyViewer
	 */
	private void clearSelections() {
		// clear pinfo's selections
		pinfo.getServicePropertyHelperMap().clear();
		pinfo.getServices().clear();
		dependencyViewer.setAllChecked(false);
	}
	
	/**
	 * Warn the user that current selections will be cleared
	 * 
	 * @param shell
	 * @return
	 */
	private boolean reloadListDialog(Shell shell) {
		// if we switch to another bug, our settings will be lost!
		if (pinfo.getServices() == null || pinfo.getServices().size() < 1)
			return true;
		
		return MessageDialog.openConfirm(
				shell, RELOAD_LIST_WARN_TITLE, RELOAD_LIST_WARN_MESSAGE);
	}	
	
	
	/**
	 * Opens the selector dialog that lets us choose service properties for filtering
	 * 
	 */
	private void openServicePropertySelectorDialog(
			Shell shell, String selectedService, boolean clearCheckedOnCancel) {
		
		// TODO - uncomment this out of for v 1.5 of the SDK which will include
		// support for service property filters.  For the current release,
		// this is turned off
		
		/*
		if (servicePropertyOptionsMap.containsKey(selectedService)
				&& servicePropertyOptionsMap.get(selectedService) != null
				&& servicePropertyOptionsMap.get(selectedService).size() > 0) {
			Dialog d = new ServicePropertySelectorDialog(shell, selectedService, 
					(List<ServicePropertyHelper>) servicePropertyOptionsMap.get(selectedService), pinfo);
			if (d.open() == Dialog.CANCEL && clearCheckedOnCancel) {
				dependencyViewer.setChecked(selectedService, false);
			}
		}
		*/
	}
	
	
	/**
	 * Sets up and kicks off a job to list services provided by connected BUG
	 */
	private void launchRefreshServicesJob(BugConnection connection) {
		IJobManager manager = Job.getJobManager();
		String jobFamily = "family-" + connection.getName(); //$NON-NLS-1$
		Job[] jobs = manager.find(jobFamily);
		
		// TODO servicePropertyOptionsMap.clear();
		
		// start a job only if a job within this family has not been started already.
		if(jobs.length == 0) {
			GetServicesJob job = 
				new GetServicesJob(
						"Getting services for " + connection.getName(),connection, jobFamily);
			job.schedule();
		}		
	}
	
	/**
	 * Sets the page message based on size
	 * 
	 * @param size
	 */
	private void setPageMessage(int size) {
		if (size == 0) setMessage(LAUNCH_VBUG_MESSAGE);
		else setMessage(SELECT_BUG_MESSAGE);
	}
	
	/**
	 * Get ServiceDetail List from bug (via WSHelper)
	 * and turn into a Map that makes things easier to display
	 *  
	 * @param bugConnection
	 * @param monitor
	 */
	private void populateServiceList(BugConnection bugConnection, IProgressMonitor monitor){
		try {
			List<ServiceDetail>  details = 
				BugWSHelper.getAllServiceDetails(bugConnection.getProgramURL());
			
			// Older BUGs don't have service details XML
			if (details == null || details.size() < 1) {
				List<String> services = 
					BugWSHelper.getAllServices(bugConnection.getProgramURL());
				details = new ArrayList<ServiceDetail>();
				for (String service : services) {
					details.add(
						new ServiceDetail(service, new ArrayList<ServiceProperty>()));
				}
			}
			
			// clear the map before adding stuff
			servicePropertyOptionsMap.clear();
			
			List<ServiceProperty> properties;
			for (ServiceDetail detail : details) {
				properties = detail.getServiceProperties();
				Collections.sort(properties);
				if (!servicePropertyOptionsMap.containsKey(detail.getServiceName()))
					servicePropertyOptionsMap.put(
							detail.getServiceName(), 
							ServicePropertyHelper.createHelperList(properties));
				else
					servicePropertyOptionsMap.get(detail.getServiceName()).addAll(
							ServicePropertyHelper.createHelperList(properties));
			}
			
			setInputForDepndencyViewer();
		} catch (Exception e1) {
			e1.printStackTrace();
			servicePropertyOptionsMap.clear();
			setInputForDepndencyViewer();
		}
	}

	/**
	 * Freshen the model based on the viewer.
	 */
	private void updateModel() {
		List checkedServices = Arrays.asList(dependencyViewer.getCheckedElements());
		pinfo.getServices().clear();
		pinfo.getServices().addAll(checkedServices);
		if(checkedServices.size() > 0){
			btnGenerateThreadApp.setEnabled(true);
		}
		else{
			btnGenerateThreadApp.setEnabled(false);
		}
	}
	
	/**
	 * Called to set the input for the dependencyViewer component
	 */
	private void setInputForDepndencyViewer() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){
			public void run() {
				dependencyViewer.setInput(servicePropertyOptionsMap);
			}
		});
	}

	/* INNER CLASSES */

	class ModulesContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object input) {
			if (input instanceof Map) {
				return ((Map) input).keySet().toArray();
			}
			return null;
		}
		public void inputChanged(Viewer viewer,	Object oldInput, Object newInput) {
			// don't need to hang onto input for this example, so do nothing
		}
		public void dispose() {
		}
	}

	class ModulesLabelProvider extends LabelProvider implements ITableLabelProvider {
		private final static String DELIM = "."; //$NON-NLS-1$
		public String getColumnText(Object element, int columnIndex) {
			String casted = (String)element;
			switch (columnIndex) {
			case 0 :
				return casted.substring(casted.lastIndexOf(DELIM)+1, casted.length());
			case 1 :
				return casted.substring(0, casted.lastIndexOf(DELIM));
			default :
				return "";
			}
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	/**
	 * Private inner class to handle clicking of "Lauch Virtual BUG" button
	 * 
	 * Broken out into private innner class (instead of anonymous class) to make
	 * the code easier to read.
	 * 
	 *
	 */
	private class StartVBUGSelectionListener implements SelectionListener {			
		/**
		 *  Empty
		 */
		public void widgetDefaultSelected(SelectionEvent e) {}

		/**
		 * Selected the Launch Virtual BUG button
		 */
		public synchronized void widgetSelected(SelectionEvent e) {
			btnStartVBUG.setEnabled(false);
			btnStartVBUG.getDisplay().syncExec( new Runnable(){
				public void run() {
					// Reset this global flag - fixes defect 322
					DragonflyActivator.getDefault().setVirtualBugRemovedByTerminate(false);

					VirtualBugLaunchShortCut virtualBugLaunchShortcut = new VirtualBugLaunchShortCut();
					IProcess[] processes = null;
					try {
						processes = virtualBugLaunchShortcut
							.launch(ILaunchManager.RUN_MODE).getProcesses();
					} catch (CoreException e) {
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, 
								DragonflyActivator.PLUGIN_ID, 
								IStatus.OK, "Failure launching Virtual BUG", e));
					}
					if (processes == null || processes.length < 1) return;
					
					processes[0].getStreamsProxy().
						getOutputStreamMonitor().addListener(new ProcessStreamListener());			
				}
			});

			btnStartVBUG.setEnabled(true);
		}
		
		/**
		 * internal helper class to keep things clean
		 *
		 */
		private class ProcessStreamListener implements IStreamListener {
			private int cnt = 0;
			
			public void streamAppended(String text, IStreamMonitor monitor) {
				if (text.indexOf(BUGLABS_EMULATOR_BUNDLE_NAME) == -1) 
						return;
				cnt++;
				if (cnt == 2) {
					PlatformUI.getWorkbench().getDisplay().asyncExec( new Runnable() {
						public void run() {
							btnStartVBUG.setEnabled(true);
						}
					});
				}
			}
		}
		
	}
	
	/**
	 * Job that retrieves services for a connection
	 * @author akravets
	 *
	 */
	private class GetServicesJob extends Job{

		private BugConnection connection;
		private String family;

		public GetServicesJob(String name, BugConnection connection, String family) {
			super(name);
			this.connection = connection;
			this.family = family;
		}

		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Trying to connect to BUG", IProgressMonitor.UNKNOWN);
			if(connection != null){
				monitor.beginTask("Retrieving services for " + connection.getName(), IProgressMonitor.UNKNOWN);
				populateServiceList(connection, monitor);
				monitor.done();
			}
			return Status.OK_STATUS;
		}	
		
		public boolean belongsTo(Object family) {
			return this.family.equals(family);
		}
	}
	
	
}
