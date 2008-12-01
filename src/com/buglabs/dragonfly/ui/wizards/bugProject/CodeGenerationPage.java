/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.wizards.bugProject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BaseTreeNode;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.filters.ServiceFilter;
import com.buglabs.dragonfly.ui.info.BugProjectInfo;
import com.buglabs.dragonfly.ui.launch.VirtualBugLaunchShortCut;
import com.buglabs.dragonfly.ui.views.mybugs.MyBugsViewContentProvider;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;
/**
 * Wizard page that displays currently running BUGs and allows user to include services from a particular
 * BUG in the project
 * 
 * @author akravets
 *
 */
public class CodeGenerationPage extends WizardPage {

	private static final String BUGLABS_EMULATOR_BUNDLE_NAME = "com.buglabs.bug.emulator";
	
	private GridData gridData;
	private Display display = PlatformUI.getWorkbench().getDisplay();
	
	TableViewer bugsViewer;
	private CheckboxTableViewer dependencyViewer;
	private Text serviceDescriptionArea;
	private Button btnStartVBUG;
	private Button btnGenerateThreadApp;
	private Button refreshServiceDefintions;
	
	protected BugConnection selectedBugConnection;
	private List services = new ArrayList();
	private ServiceFilter serviceFilter = new ServiceFilter();
	private BugProjectInfo pinfo;
	private String pageMessage = "";
	
	protected CodeGenerationPage(BugProjectInfo pinfo) {
		super("CodeGenerationPage", "Service definition", 
				Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_COLOR_DIALOG_PROJECT));
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

		Group targetGroup = new Group(mainComposite, SWT.NONE);
		targetGroup.setText("Target BUG");
		targetGroup.setLayout(new GridLayout(2, false));

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;

		targetGroup.setLayoutData(gridData);

		// this is the list of all the bugs you can see
		createTargetArea(targetGroup);
		// where you choose the services
		createServicesSection(mainComposite);
		createApplicationLoop(mainComposite);
		setControl(mainComposite);
	}

	/**
	 * Creates TableViewer that has all BUGs currently available in MyBUGs view
	 * 
	 * @param composite
	 */
	private void createTargetArea(Composite composite) {
		GridData gdLabel = new GridData(GridData.FILL_HORIZONTAL);
		gdLabel.horizontalSpan = 2;

		Label label = new Label(composite, SWT.NONE);
		label.setText("Select a BUG from the list below to be used as a target");
		label.setLayoutData(gdLabel);

		GridData fillHorizontal = new GridData(GridData.FILL_HORIZONTAL);
		GridData gdViewer = GridDataFactory.createFrom(fillHorizontal).create();
		gdViewer.heightHint = 100;
		bugsViewer = new TableViewer(composite, SWT.BORDER | SWT.V_SCROLL);

		bugsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				if(((BaseTreeNode)bugsViewer.getInput()).getChildren().size() != 0){
					final BugConnection connection = 
						(BugConnection) ((StructuredSelection) event.getSelection()).getFirstElement();
					if(connection != null){
						refreshServiceDefintions.setEnabled(true);
						IJobManager manager = Job.getJobManager();
						String jobFamily = "family-" + connection.getName(); //$NON-NLS-1$
						Job[] jobs = manager.find(jobFamily);
						
						services.clear();
						setInputForDepndencyViewer();
						
						// start a job only if a job within this family has not been started already.
						if(jobs.length == 0){
							GetServicesJob job = new GetServicesJob("Getting services for " + connection.getName(),connection, jobFamily);
							job.schedule();
						}
					}
				}
			}
		});

		bugsViewer.getTable().setLayoutData(gdViewer);

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
		btnStartVBUG.setText("Start &Virtual BUG");
		btnStartVBUG.setToolTipText("Start Virtual BUG and start consuming its services");
		GridData gdButton = new GridData();
		gdButton.verticalAlignment = SWT.TOP;
		btnStartVBUG.setLayoutData(gdButton);
		btnStartVBUG.addSelectionListener(((SelectionListener) new StartVBUGSelectionListener()));

		setPageMessage(root.getChildren().size());
	}

	private void setPageMessage(int size) {
		if(size == 0){
			setMessage("Launch Virtual BUG to select services that this project will consume.");
		}
		else{
			setMessage("Select a BUG from Target BUG List to choose services that this project will consume.");
		}
	}

	private void populateServiceList(BugConnection bugConnection, IProgressMonitor monitor){
		try {
			services = BugWSHelper.getAllServices(bugConnection.getProgramURL());
			setInputForDepndencyViewer();
		} catch (Exception e1) {
			services.clear();
			setInputForDepndencyViewer();
		}
	}

	private void setInputForDepndencyViewer() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){

			public void run() {
				dependencyViewer.setInput(services);
			}

		});
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

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

			public void widgetSelected(SelectionEvent e) {
				pinfo.setShouldGenerateApplicationLoop(((Button)e.widget).getSelection());
			}
		});
	}

	/**
	 * Draws the section where you can filter services
	 * or select a service based on your selection above
	 * 
	 * @param mainComposite
	 */
	private void createServicesSection(Composite mainComposite) {
		// set up
		Composite servicesComposite = new Composite(mainComposite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		servicesComposite.setLayout(layout);

		// box that surrounds this section
		Group compServices = new Group(servicesComposite, SWT.NONE);
		compServices.setText("Required Services");
		GridData gdServices = new GridData(GridData.FILL_BOTH);
		gdServices.horizontalSpan = 2;
		gdServices.heightHint = 300;
		gdServices.widthHint = 550;
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
				serviceFilter.setPattern(".*" + filter + ".*", 
						Arrays.asList(dependencyViewer.getCheckedElements()));
				dependencyViewer.refresh();
			}
		});

		// table with list of services to choose from
		Table modulesTable = new Table(compServices,SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		modulesTable.setHeaderVisible(true);
		modulesTable.setLinesVisible(true);

		TableColumn col1 = new TableColumn(modulesTable, SWT.NONE);
		col1.setText("Name");
		TableColumn col2 = new TableColumn(modulesTable, SWT.NONE);
		col2.setText("Package");

		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(90));
		tableLayout.addColumnData(new ColumnWeightData(120));
		modulesTable.setLayout(tableLayout);

		GridData viewerData = new GridData(GridData.FILL_BOTH);
		viewerData.horizontalSpan = layout.numColumns;
		viewerData.heightHint = 200;

		dependencyViewer = new CheckboxTableViewer(modulesTable);
		dependencyViewer.getControl().setLayoutData(viewerData);
		dependencyViewer.setContentProvider(new ModulesContentProvider());
		dependencyViewer.setLabelProvider(new ModulesLabelProvider());


		setUpViewerListener();

		dependencyViewer.setSorter(new ViewerSorter());
		dependencyViewer.addFilter(serviceFilter);
		dependencyViewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				updateModel();
			}
		});

		Composite buttonComposite = new Composite(compServices, SWT.NONE);
		GridData buttonData = new GridData(GridData.FILL_VERTICAL);
		buttonComposite.setLayoutData(buttonData);
		RowLayout buttonLayout = new RowLayout(2);
		buttonLayout.pack = false;
		buttonComposite.setLayout(buttonLayout);

		Button btnSelectAll = new Button(buttonComposite, SWT.PUSH);
		btnSelectAll.setText("&Select All");
		btnSelectAll.setToolTipText("Select all services");
		btnSelectAll.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

			public void widgetSelected(SelectionEvent e) {
				dependencyViewer.setAllChecked(true);
				updateModel();
			}
		});
		
		Button btnDeselectAll = new Button(buttonComposite, SWT.PUSH);
		btnDeselectAll.setText("&Deselect All");
		btnDeselectAll.setToolTipText("Deselect all services");
		btnDeselectAll.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

			public void widgetSelected(SelectionEvent e) {
				dependencyViewer.setAllChecked(false);
				updateModel();
			}
		});

		refreshServiceDefintions = new Button(buttonComposite, SWT.PUSH);
		refreshServiceDefintions.setText("&Refresh Services");
		refreshServiceDefintions.setToolTipText("Refreshes services definitions provided by the target BUG");
		refreshServiceDefintions.setEnabled(false);
		refreshServiceDefintions.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

			public void widgetSelected(SelectionEvent e) {
				final BugConnection connection = (BugConnection) ((StructuredSelection) bugsViewer.getSelection()).getFirstElement();
				if(connection != null){
					IJobManager manager = Job.getJobManager();
					String jobFamily = "family-" + connection.getName(); //$NON-NLS-1$
					Job[] jobs = manager.find(jobFamily);
					
					services.clear();
					setInputForDepndencyViewer();
					
					// start a job only if a job within this family has not been started already.
					if(jobs.length == 0){
						GetServicesJob job = new GetServicesJob("Getting services for " + connection.getName(),connection, jobFamily);
						job.schedule();
					}
				}
				else{
					UIUtils.giveVisualInformation("Please select target BUG to refresh");
				}
			}
		});
		
		serviceDescriptionArea = new Text(compServices, SWT.BORDER | SWT.MULTI | SWT.WRAP|SWT.V_SCROLL);
		
		GridData spanAllFillBoth = new GridData(GridData.FILL_BOTH);
		spanAllFillBoth.horizontalSpan = layout.numColumns;

		spanAllFillBoth.minimumHeight = 50;
		serviceDescriptionArea.setLayoutData(spanAllFillBoth);
		serviceDescriptionArea.setEditable(false);
	}

	class ModulesContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object input) {
			if (input instanceof List) {
				return ((List) input).toArray();
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


	private void setUpViewerListener() {
		dependencyViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (event != null) {
					ISelection selection = event.getSelection();
					if (selection instanceof IStructuredSelection) {
						String selectedService = (String) ((IStructuredSelection) selection).getFirstElement();
						if (selectedService != null) {
							String descr = Activator.getServiceDescription(selectedService.trim());
							serviceDescriptionArea.setText(descr);
						}
					}
				}
			}

		});
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
	 * Private inner class to handle clicking of "Lauch Virtual BUG" button
	 * 
	 * Broken out into private innner class (instead of anonymous class) to make
	 * the code easier to read.
	 * 
	 * @author brian
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
					try {
						// Reset this global flag - fixes defect 322
						DragonflyActivator.getDefault().setVirtualBugRemovedByTerminate(false);

						VirtualBugLaunchShortCut virtualBugLaunchShortcut = new VirtualBugLaunchShortCut();
						IProcess[] processes = virtualBugLaunchShortcut
							.launch(ILaunchManager.RUN_MODE).getProcesses();
						if(processes != null && processes.length > 0) {
							processes[0].getStreamsProxy()
									.getOutputStreamMonitor().addListener( new IStreamListener() {
								int cnt = 0;
								public void streamAppended(String text, IStreamMonitor monitor) {
									if (text.indexOf(BUGLABS_EMULATOR_BUNDLE_NAME) != -1) {
										cnt++;
										if (cnt == 2) {
											display.asyncExec( new Runnable() {
												public void run() {
													btnStartVBUG.setEnabled(true);
												}
											});
										}
									}
								}
							});
						}
					} catch (CoreException e) {
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, 
								DragonflyActivator.PLUGIN_ID, 
								IStatus.OK, 
								"Failure launching Virtual BUG", 
								e));
					}
				};
			});

			btnStartVBUG.setEnabled(true);
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
