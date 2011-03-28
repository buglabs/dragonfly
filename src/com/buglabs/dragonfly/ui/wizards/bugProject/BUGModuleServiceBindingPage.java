/*******************************************************************************
 * Copyright (c) 2011 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.wizards.bugProject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.model.BugProjectInfo;
import com.buglabs.dragonfly.model.project.wizard.BUGModule;
import com.buglabs.dragonfly.model.project.wizard.BUGModuleService;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.util.UIUtils;


/**
 * A page that shows BUG modules and the corresponding services the modules provide.
 * @author kgilmer
 *
 */
public class BUGModuleServiceBindingPage  extends WizardPage implements IDebugEventSetListener {

	private static final String DEFAULT_BUG_MODULE_SERVICES_XML_FILENAME = "/default_services.xml";
	private static final String PAGE_NAME = "BUGModuleServiceBindingPage";
	private static final String PAGE_TITLE = "BUG Module Services";
	private final BugProjectInfo pinfo;
	private CheckboxTableViewer serviceViewer;
	private List<String> pinfoSvcs;
	private List<BUGModule> moduleModel;
	
	protected BUGModuleServiceBindingPage(BugProjectInfo pinfo) {
		super(PAGE_NAME, PAGE_TITLE, Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_COLOR_DIALOG_PROJECT));
		setMessage("Select BUG module services that will be used in the application.");
		this.pinfo = pinfo;
	}

	
	public void createControl(Composite parent) {
		try {
			Composite mainComposite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout(1, false);
			mainComposite.setLayout(layout);
			mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Composite msComp = new Composite(mainComposite, SWT.None);
			msComp.setLayout(new GridLayout(2, false));
			msComp.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			final TableViewer moduleViewer = new TableViewer(msComp, SWT.BORDER);
			moduleViewer.getTable().setLayoutData(getModuleViewerLayoutData());
			moduleViewer.setContentProvider(new BUGStaticServiceContentProvider());
			moduleViewer.setLabelProvider(new BUGStaticServiceLabelProvider());
			//Load the bug module model from static data provided in the plugin.
			moduleModel = BUGModule.load(getModuleModel());
			moduleViewer.setInput(moduleModel);
			
			Composite rComp = new Composite(msComp, SWT.None);
			rComp.setLayout(UIUtils.StripGridLayoutMargins(new GridLayout()));
			rComp.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			serviceViewer = new CheckboxTableViewer(createServiceTable(rComp));
			serviceViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
			serviceViewer.setContentProvider(new BUGServiceContentProvider());
			serviceViewer.setLabelProvider(new BUGServiceLabelProvider());
			serviceViewer.setInput(null);
			serviceViewer.addCheckStateListener(new ICheckStateListener() {
				
				@Override
				public void checkStateChanged(CheckStateChangedEvent event) {
					BUGModuleService bms = (BUGModuleService) ((IStructuredSelection) serviceViewer.getSelection()).getFirstElement();
					if (bms != null) {
						bms.setSelected(event.getChecked());
					}
					updateModel();
				}
			});
			
			final Text serviceDescText = new Text(rComp, SWT.MULTI | SWT.WRAP);
			serviceDescText.setLayoutData(getDescriptionLabelLayoutData());
			serviceDescText.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
			
			moduleViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {					
					BUGModule bm = (BUGModule) ((IStructuredSelection) moduleViewer.getSelection()).getFirstElement();
					serviceViewer.setInput(bm);
					serviceDescText.setText(bm.getDescription());
					for (BUGModuleService bms: bm.getServices()) {
						if (bms.isSelected())
							serviceViewer.setChecked(bms, true);
					}
				}
			});
			
			serviceViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					BUGModuleService bms = (BUGModuleService) ((IStructuredSelection) serviceViewer.getSelection()).getFirstElement();
					if (bms != null) {
						if (bms.getDescription() == null) {
							serviceDescText.setText("");
						} else {
							serviceDescText.setText(bms.getDescription());
						}
					} else {
						serviceDescText.setText("");
					}
				}
			});
			
			setControl(mainComposite);
		} catch (IOException e) {
			UIUtils.handleVisualError("Failed to load module data.", e);
		}
	}
	
	private Table createServiceTable(Composite parent) {
		Table t = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		
		TableColumn tc = new TableColumn(t, SWT.NONE);
		tc.setText("Name");
		tc.setWidth(200);
		
		tc = new TableColumn(t, SWT.NONE);
		tc.setText("OSGi Service");
		tc.setWidth(120);
		
		t.pack();
		
		return t;
	}


	private Object getDescriptionLabelLayoutData() {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		
		gd.heightHint = 35;
		
		return gd;
	}


	private Object getModuleViewerLayoutData() {
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 180;
		
		return gd;
	}
	
	/**
	 * Freshen the model based on the viewer.
	 */
	private void updateModel() {
		pinfoSvcs = pinfo.getModuleServices();
		pinfoSvcs.clear();
		
		for (BUGModule bm: moduleModel) {
			for (BUGModuleService bms: bm.getServices()) {
				if (bms.isSelected()) {
					pinfoSvcs.add(bms.getName());
				}
			}
		}
	}


	/**
	 * @return
	 * @throws IOException
	 */
	private Reader getModuleModel() throws IOException {
		return new InputStreamReader(Activator.getDefault().getBundle().getEntry(DEFAULT_BUG_MODULE_SERVICES_XML_FILENAME).openStream());				
	}

	public void setVisible(boolean visible) {
	
		super.setVisible(visible);
	}

	
	public void handleDebugEvents(DebugEvent[] arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private class BUGStaticServiceContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {			
		}

		@Override
		public Object[] getElements(Object inputElement) {		
			List<BUGModule> bml = (List<BUGModule>) inputElement;
			
			return bml.toArray();
		}
		
	}
	
	private class BUGStaticServiceLabelProvider implements ITableLabelProvider {

		private ImageRegistry ir =  Activator.getDefault().getImageRegistry();
		private Display display = PlatformUI.getWorkbench().getDisplay();
		
		@Override
		public void addListener(ILabelProviderListener listener) {			
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {				
			BUGModule bm = (BUGModule) element;
			
			Image modImage = ir.get(bm.getImageFilename());
			

			if (modImage == null) {
				try {
					InputStream is = Activator.getDefault().getBundle().getEntry(bm.getImageFilename()).openStream();
					modImage = new Image(display, is);
					final int width = modImage.getBounds().width;
				    final int height = modImage.getBounds().height;
				    
					Image scaledImage = new Image(display,
							modImage.getImageData().scaledTo((int)(width*0.38),(int)(height*0.38)));
					ir.put(bm.getImageFilename(), scaledImage);
					modImage.dispose();
					modImage = scaledImage;
				} catch (IOException e) {
					UIUtils.handleNonvisualError("Unable to load image for module.", e);					
				}
			}
			
			return modImage;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			BUGModule bm = (BUGModule) element;
			
			return bm.getName();
		}
		
	}
	
	private class BUGServiceContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {			
		}

		@Override
		public Object[] getElements(Object inputElement) {		
			BUGModule bm = (BUGModule) inputElement;
			
			return bm.getServices().toArray();
		}
		
	}
	
	private class BUGServiceLabelProvider implements ITableLabelProvider {
		
		private ImageRegistry ir = Activator.getDefault().getImageRegistry();
		private Display display = PlatformUI.getWorkbench().getDisplay();
		
		@Override
		public void addListener(ILabelProviderListener listener) {			
		}

		@Override
		public void dispose() {			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {		
			return null;
			/*BUGModuleService bms = (BUGModuleService) element;
			
			Image modImage = ir.get("/icons/m_generic.gif");
			
			if (modImage == null) {
				try {
					InputStream is = Activator.getDefault().getBundle().getEntry("/icons/m_generic.gif").openStream();
					modImage = new Image(display, is);
					
					ir.put("/icons/m_generic.gif", modImage);
				} catch (IOException e) {
					UIUtils.handleNonvisualError("Unable to load image for module.", e);					
				}
			}
			
			return modImage;*/
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			BUGModuleService bm = (BUGModuleService) element;
			
			if (columnIndex == 0)
				return bm.getShortName();
			
			return bm.getName();
		}
		
	}

}

