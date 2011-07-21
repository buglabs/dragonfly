package com.buglabs.dragonfly.ui.launch.felix;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.launch.VirtualBugLaunchConfigurationDelegate;
import com.buglabs.dragonfly.util.UIUtils;

public class FelixLaunchBundleTab extends AbstractLaunchConfigurationTab {

	
	private final static String TAB_NAME = "Bundles";

	private List<File> externalBundleList;
	private TableViewer externalBundleViewer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return TAB_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		GridData gdText = new GridData(GridData.FILL_HORIZONTAL);
		gdText.grabExcessHorizontalSpace = true;

		GridData gdButton = new GridData();
		gdButton.horizontalAlignment = SWT.BEGINNING;

		Group externalBundleComposite = new Group(composite, SWT.BORDER);
		GridLayout gd = new GridLayout(2, false);
		externalBundleComposite.setLayout(gd);
		GridData gData = new GridData(GridData.FILL_HORIZONTAL);
		gData.horizontalSpan = 3;
		externalBundleComposite.setLayoutData(gData);
		externalBundleComposite.setText("External Binary Bundles");
		
		externalBundleViewer = new TableViewer(externalBundleComposite, SWT.FULL_SELECTION | SWT.BORDER);
		gData = new GridData(GridData.FILL_BOTH);
		gData.heightHint = 120;
		TableColumn tc = new TableColumn(externalBundleViewer.getTable(), SWT.None);
		tc.setText("Bundle Path");
		tc.setWidth(200);
		externalBundleViewer.getTable().setHeaderVisible(false);
		externalBundleViewer.getTable().setLinesVisible(true);
		externalBundleViewer.getTable().setLayoutData(gData);
		externalBundleViewer.setContentProvider(new IStructuredContentProvider() {

			
			public void dispose() {				
			}

			
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {				
			}

			
			public Object[] getElements(Object arg0) {
				
				return ((List) arg0).toArray();
			}
			
		});
		externalBundleViewer.setLabelProvider(new ITableLabelProvider() {
			
			private Image fileImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);

			
			public void removeListener(ILabelProviderListener arg0) {
				
			}
			
			
			public boolean isLabelProperty(Object arg0, String arg1) {				
				return false;
			}
			
			
			public void dispose() {				
			}
			
			
			public void addListener(ILabelProviderListener arg0) {				
			}
			
			
			public String getColumnText(Object arg0, int arg1) {
				return ((File) arg0).getAbsolutePath();
			}
			
			
			public Image getColumnImage(Object arg0, int arg1) {			
				return fileImage ;
			}
		});
		
		
		final Composite buttonComp = new Composite(externalBundleComposite, SWT.NONE);
		buttonComp.setLayout(UIUtils.StripGridLayoutMargins(new GridLayout()));
		buttonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		Button addExternalBundleButton = new Button(buttonComp, SWT.None);
		addExternalBundleButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		addExternalBundleButton.setText("Add Bundle...");
		addExternalBundleButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(buttonComp.getShell());

				String path = dialog.open();

				if (!UIUtils.stringEmpty(path)) {
					File f = new File(path);
					
					if (f.isFile() && f.exists() && f.getName().toUpperCase().endsWith(".JAR")) {
						if (!externalBundleList.contains(f)) {
							externalBundleList.add(f);
							
							setDirty(true);
							updateLaunchConfigurationDialog();
							externalBundleViewer.refresh();
						} else {
							UIUtils.handleVisualError(f.getName() + " is already in the list.", null);	
						}
					} else {
						UIUtils.handleVisualError(f.getName() + " is not a valid OSGi bundle.", null);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});
		
		final Button removeExternalBundleButton = new Button(buttonComp, SWT.None);
		removeExternalBundleButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
		removeExternalBundleButton.setText("Remove");
		removeExternalBundleButton.setEnabled(false);
		removeExternalBundleButton.addSelectionListener(new SelectionListener() {
			
			
			public void widgetSelected(SelectionEvent arg0) {
				externalBundleList.remove(((IStructuredSelection) externalBundleViewer.getSelection()).getFirstElement());
				externalBundleViewer.refresh();
				updateLaunchConfigurationDialog();
				setDirty(true);
			}
			
			
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		externalBundleViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			
			public void selectionChanged(SelectionChangedEvent arg0) {
				if (externalBundleViewer.getSelection().isEmpty()) {
					removeExternalBundleButton.setEnabled(false);
				} else {
					removeExternalBundleButton.setEnabled(true);
				}
			}
			
		});
		
		setControl(composite);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		
		try {
			String rawList = getSystemProperty(configuration, VirtualBugLaunchConfigurationDelegate.PROP_EXTERNAL_BUNDLE_LAUNCH_LIST, "");
			externalBundleList = delimitedStringToList(rawList);
			externalBundleViewer.setInput(externalBundleList);
		} catch (CoreException e) {
			UIUtils.handleVisualError("Unable to initialize external bundle list.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.
	 * debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		try {
			FelixLaunchConfigurationInitializer.initializeSystemProperties(configuration, false);
		} catch (CoreException e1) {
			e1.printStackTrace();
			UIUtils.handleNonvisualError(e1.getMessage(), e1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse
	 * .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		try {
			// here make a new copy of the config hash and save the copy so
			// revert button works
			Map properties = configuration.getAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, new HashMap());
			Map<String, String> propertiesCopy = new HashMap<String, String>(properties);
			
			propertiesCopy.put(VirtualBugLaunchConfigurationDelegate.PROP_EXTERNAL_BUNDLE_LAUNCH_LIST, listToDelimitedString(externalBundleList));
			configuration.setAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, propertiesCopy);
		} catch (CoreException e) {
			e.printStackTrace();
			UIUtils.handleNonvisualError(e.getMessage(), e);
		}
	}

	public static String listToDelimitedString(List<File> list) {
		StringBuffer sb = new StringBuffer();
		
		Iterator<File> i = list.iterator();
		
		while (i.hasNext()) {
			sb.append(i.next().getAbsolutePath());
			if (i.hasNext()) {
				sb.append(File.pathSeparator);
			}
		}
		
		return sb.toString();
	}
	
	public static List<File> delimitedStringToList(String list) {
		List<String> slist = Arrays.asList(list.split(File.pathSeparator));
		
		List<File> flist = new ArrayList<File>();
		
		for (String s: slist) {
			if (s.trim().length() > 0) {
				flist.add(new File(s));
			}
		}
		
		return flist;
	}

	private String getSystemProperty(ILaunchConfiguration configuration, String prop, String defaultValue) throws CoreException {
		Map properties = configuration.getAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, new HashMap());
		String val = (String) properties.get(prop);

		if (val != null) {
			return val;
		}

		return defaultValue;
	}



}
