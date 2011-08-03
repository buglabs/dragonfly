/* Copyright (c) 2007 Bug Labs, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of Bug Labs nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.buglabs.dragonfly.ui.launch;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.buglabs.dragonfly.launch.BUGSimulatorLaunchConfigurationDelegate;
import com.buglabs.dragonfly.ui.Activator;

/**
 * 
 * @author Ken Gilmer - ken@buglabs.net
 * 
 */
public class SystemPropertiesTab extends AbstractLaunchConfigurationTab {

	public static final String SYSTEM_PROPERTIES_KEY = "SYSTEM_PROPERTIES";

	private Map systemProps;

	private Button silentLevel;
	
	private Button errorLevel;

	private Button warningLevel;

	private Button infoLevel;

	private Button debugLevel;

	private TableViewer propViewer;

	private Button removePropButton;

	private Button editPropButton;

	private Text jvmArgs;

	protected String jvmArgStr;

	public SystemPropertiesTab() {
	}

	// Workaround
	private void put(String key, String value) {
		Map tempMap = new Hashtable();
		tempMap.putAll(systemProps);
		tempMap.put(key, value);
		systemProps = tempMap;
		refreshDialog();
	}

	// Workaround
	private void remove(Object key) {
		Map tempMap = new Hashtable();
		tempMap.putAll(systemProps);
		tempMap.remove(key);
		systemProps = tempMap;
		refreshDialog();
	}

	protected void refreshDialog() {
		setDirty(true);
		updateLaunchConfigurationDialog();
	}

	public void createControl(final Composite parent) {
		Composite main = new Composite(parent, SWT.None);
		main.setLayout(new GridLayout(2, false));
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group levelGroup = new Group(main, SWT.None);
		levelGroup.setText("Log Level");
		levelGroup.setLayout(new GridLayout());
		GridData gdata = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		levelGroup.setLayoutData(gdata);
		
		silentLevel = new Button(levelGroup, SWT.RADIO);
		silentLevel.setText("Silent");
		silentLevel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				put(BUGSimulatorLaunchConfigurationDelegate.FELIX_LOG_LEVEL, "0");
			}
		});

		errorLevel = new Button(levelGroup, SWT.RADIO);
		errorLevel.setText("Error");
		errorLevel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				put(BUGSimulatorLaunchConfigurationDelegate.FELIX_LOG_LEVEL, "1");
			}
		});
		warningLevel = new Button(levelGroup, SWT.RADIO);
		warningLevel.setText("Warning");
		warningLevel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				put(BUGSimulatorLaunchConfigurationDelegate.FELIX_LOG_LEVEL, "2");
			}
		});

		infoLevel = new Button(levelGroup, SWT.RADIO);
		infoLevel.setText("Information");
		infoLevel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				put(BUGSimulatorLaunchConfigurationDelegate.FELIX_LOG_LEVEL, "3");
			}
		});

		debugLevel = new Button(levelGroup, SWT.RADIO);
		debugLevel.setText("Debug");
		debugLevel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				put(BUGSimulatorLaunchConfigurationDelegate.FELIX_LOG_LEVEL, "4");
			}
		});

		Composite propComp = new Composite(main, SWT.None);
		GridLayout layout = new GridLayout(2, false);
		propComp.setLayout(layout);
		gdata = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		propComp.setLayoutData(gdata);

		propViewer = new TableViewer(propComp, SWT.None | SWT.FULL_SELECTION | SWT.BORDER);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.heightHint = 140;
		propViewer.getTable().setLayoutData(gdata);
		propViewer.setContentProvider(new PropertyContentProvider());
		propViewer.setLabelProvider(new PropertyLabelProvider());
		propViewer.getTable().setLinesVisible(true);
		propViewer.setSorter(new ViewerSorter());
		propViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				removePropButton.setEnabled(propViewer.getSelection() != null);
				editPropButton.setEnabled(propViewer.getSelection() != null);
			}

		});

		propViewer.getTable().setHeaderVisible(true);
		TableColumn tc = new TableColumn(propViewer.getTable(), SWT.None);
		tc.setText("Property Name");
		tc.setWidth(200);
		tc = new TableColumn(propViewer.getTable(), SWT.None);
		tc.setText("Value");
		tc.setWidth(200);

		Composite propButtonComp = new Composite(propComp, SWT.None);
		propButtonComp.setLayout(StripGridLayoutMargins(new GridLayout()));
		propButtonComp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		Button newPropButton = new Button(propButtonComp, SWT.NONE);
		newPropButton.setText("New...");
		newPropButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		newPropButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				NewPropertyDialog dialog = new NewPropertyDialog(parent.getShell());

				if (dialog.open() == Dialog.OK) {
					put(dialog.getName(), dialog.getValue());
				}
			}

		});

		editPropButton = new Button(propButtonComp, SWT.NONE);
		editPropButton.setText("Edit...");
		editPropButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		editPropButton.setEnabled(false);
		editPropButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				NewPropertyDialog dialog = new NewPropertyDialog(parent.getShell());

				String name = (String) ((IStructuredSelection) propViewer.getSelection()).getFirstElement();
				String value = (String) systemProps.get(name);

				dialog.setName(name);
				dialog.setValue(value);

				if (dialog.open() == Dialog.OK) {
					remove(name);
					put(dialog.getName(), dialog.getValue());
				}
			}

		});

		removePropButton = new Button(propButtonComp, SWT.NONE);
		removePropButton.setText("Remove");
		removePropButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		removePropButton.setEnabled(false);
		removePropButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent event) {
				remove(((IStructuredSelection) propViewer.getSelection()).getFirstElement());
			}

		});

		Group jvmArgGroup = new Group(main, SWT.NONE);
		jvmArgGroup.setText("VM Arguments");
		jvmArgGroup.setLayout(new GridLayout());
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 2;
		jvmArgGroup.setLayoutData(gdata);

		jvmArgs = new Text(jvmArgGroup, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		gdata = new GridData(GridData.FILL_BOTH);
		gdata.heightHint = 60;
		jvmArgs.setLayoutData(gdata);
		jvmArgs.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				jvmArgStr = jvmArgs.getText();
				refreshDialog();
			}

		});

		setControl(main);
	}

	public String getName() {
		return "Properties";
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			systemProps = configuration.getAttribute(SYSTEM_PROPERTIES_KEY, new Hashtable());
			propViewer.setInput(systemProps);

			String ll = (String) systemProps.get(BUGSimulatorLaunchConfigurationDelegate.FELIX_LOG_LEVEL);

			jvmArgStr = configuration.getAttribute(BUGSimulatorLaunchConfigurationDelegate.JVM_ARGS, "");

			if (ll != null) {
				switch (Integer.parseInt(ll)) {
				case 0:
					silentLevel.setSelection(true);
				case 1:
					errorLevel.setSelection(true);
					break;
				case 2:
					warningLevel.setSelection(true);
					break;
				case 3:
					infoLevel.setSelection(true);
					break;
				case 4:
					debugLevel.setSelection(true);
					break;
				}
			}

			String args = configuration.getAttribute(BUGSimulatorLaunchConfigurationDelegate.JVM_ARGS, "");
			if (args != null) {
				jvmArgs.setText(args);
			}
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, e.getMessage(), null));
		}

	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(SYSTEM_PROPERTIES_KEY, systemProps);
		configuration.setAttribute(BUGSimulatorLaunchConfigurationDelegate.JVM_ARGS, jvmArgStr);
		propViewer.setInput(systemProps);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * Removes margins from a GridLayout
	 * 
	 * @param layout
	 * @return
	 */
	private GridLayout StripGridLayoutMargins(GridLayout layout) {
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		return layout;
	}


	private class PropertyContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			Map props = (Map) inputElement;
			return props.keySet().toArray(new String[props.size()]);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class PropertyLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String key = (String) element;

			String val = (String) SystemPropertiesTab.this.systemProps.get(key);

			switch (columnIndex) {
			case 0:
				return key;
			case 1:
				return val;
			}

			return null;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}
}
