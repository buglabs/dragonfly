/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/

package com.buglabs.dragonfly.ui.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.ui.BugnetAuthenticationHelper;
import com.buglabs.dragonfly.ui.actions.RefreshBugNetViewAction;
import com.buglabs.dragonfly.ui.views.bugnet.BugnetView;

public class BugNetPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String PROTOCOL_SEPARATOR = "://";
	
	private String serverName;

	private String protocol;
	
	private boolean enabled;

	private String numofapps;

	private Text serverNameTextField;

	private Text numOfApplications;

	private Button enableChk;

	private boolean applyPerformed = false;

	private String originalServerName;

	public BugNetPreferencePage() {
	}

	public BugNetPreferencePage(String title) {
		super(title);
	}

	public BugNetPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	protected Control createContents(Composite parent) {

		initPreferences();

		Composite top = new Composite(parent, SWT.NONE);

		GridLayout gl = new GridLayout();
		top.setLayout(gl);

		GridData grabHGD = GridDataFactory.fillDefaults().create();
		grabHGD.grabExcessHorizontalSpace = true;

		enableChk = new Button(top, SWT.CHECK);
		enableChk.setText(Messages.getString("BugNetPreferencePage.0")); //$NON-NLS-1$
		enableChk.setSelection(enabled);
		enableChk.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				enabled = enableChk.getSelection();
				enabledFields();
				resetChangePerform();
			}

		});
		Composite urlComp = new Composite(top, SWT.NONE);
		urlComp.setLayout(new GridLayout(2, false));
		urlComp.setLayoutData(grabHGD);
		Label urlLbl = new Label(urlComp, SWT.NONE);
		urlLbl.setText(Messages.getString("BugNetPreferencePage.1")); //$NON-NLS-1$
		serverNameTextField = new Text(urlComp, SWT.BORDER);
		serverNameTextField.setText(protocol + serverName);
		serverNameTextField.setLayoutData(grabHGD);

		serverNameTextField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				setServerAndProtocol();
				resetChangePerform();
			}
		});

		// spacer label
		new Label(top, SWT.NONE);

		Composite appsComp = new Composite(top, SWT.NONE);
		appsComp.setLayoutData(grabHGD);
		appsComp.setLayout(new GridLayout(2, false));

		Label numOfAppsLbl = new Label(appsComp, SWT.NONE);
		numOfAppsLbl.setText(Messages.getString("BugNetPreferencePage.2")); //$NON-NLS-1$

		numOfApplications = new Text(appsComp, SWT.BORDER);
		numOfApplications.setText(numofapps);
		numOfApplications.setToolTipText(Messages.getString("BugNetPreferencePage.3")); //$NON-NLS-1$

		GridData gData = new GridData();
		gData.widthHint = 20;
		numOfApplications.setLayoutData(gData);

		// restric number of applications to dispay to 100
		numOfApplications.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				try {
					numofapps = numOfApplications.getText();
				} catch (NumberFormatException nfe) {
					numofapps = "0";
				}
				if (!Character.isDigit(e.character)) {
					if (e.character == SWT.BS) {
						e.doit = true;
						return;
					}
					e.doit = false;
					return;
				} else {
					if (String.valueOf(numofapps).length() == 3) {
						e.doit = false;
						return;
					}
					e.doit = true;
					return;
				}
			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

		});

		numOfApplications.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				numofapps = numOfApplications.getText();
				
				if(!numofapps.equals("")){
					if (Integer.parseInt(numofapps) > 100) {
						setErrorMessage("Maximum number of applications is 100");
						setValid(false);
					} else if (Integer.parseInt(numofapps) < 1) {
						setErrorMessage("Minimum number of applications is 1");
						setValid(false);
					} else {
						setErrorMessage(null);
						setValid(true);
					}
					resetChangePerform();
				}
			}

		});

		Button clearAuthDataButton = new Button(top, SWT.NONE);
		clearAuthDataButton.setText("Clear Authentication Data");
		clearAuthDataButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				BugnetAuthenticationHelper.getInstance().logout();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		enabledFields();

		return top;
	}

	private void enabledFields() {
		serverNameTextField.setEnabled(enabled);
		numOfApplications.setEnabled(enabled);
	}

	protected void performApply() {
		setData();
		refreshBUGnetView();
		applyPerformed  = true;
	}

	public boolean performOk() {
		// perform Ok only if apply wasn't performed
		if(!applyPerformed){
			setData();
			refreshBUGnetView();
		}
		applyPerformed = false;
		return true;
	}

	private void refreshBUGnetView() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			public void run() {
				BugnetView bugNetView = (BugnetView) DragonflyActivator.getDefault().getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().findView(BugnetView.VIEW_ID);
				if (bugNetView != null) {
					new RefreshBugNetViewAction(bugNetView).run();
				}
			}

		});
	}

	private void setData() {
		DragonflyActivator.getDefault().getPluginPreferences().setValue(DragonflyActivator.PREF_PROTOCOL, protocol);
		DragonflyActivator.getDefault().getPluginPreferences().setValue(DragonflyActivator.PREF_SERVER_NAME, serverName);
		DragonflyActivator.getDefault().getPluginPreferences().setValue(DragonflyActivator.PREF_BUGNET_ENABLED, enabled);
		DragonflyActivator.getDefault().getPluginPreferences().setValue(DragonflyActivator.PREF_BUGNET_NUM_OF_APPS, numofapps);

		// if serverName has changed clear authentication data
		if(!originalServerName.equals(serverName)){
			originalServerName = serverName;
			BugnetAuthenticationHelper.getInstance().logout();
		}
	}

	/*  NEVER CALLED AS FAR AS I CAN TELL SO COMMENTING OUT FOR NOW
	protected void performDefaults() {
		super.performDefaults();

		DragonflyActivator.getDefault().internalInitializeDefaultPluginPreferences();
		setDefaults();

		BugnetAuthenticationHelper.clearAuthData();
		originalServerName = serverName;
		
		serverNameTextField.setText(protocol + serverName);
		numOfApplications.setText(numofapps);
		enableChk.setSelection(enabled);
		enabledFields();
	}
	*/

	public void init(IWorkbench workbench) {
		initPreferences();
	}

	private void initPreferences() {
		protocol = DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_PROTOCOL);
		serverName = DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_SERVER_NAME);
		originalServerName = serverName;
		enabled = DragonflyActivator.getDefault().getPluginPreferences().getBoolean(DragonflyActivator.PREF_BUGNET_ENABLED);
		numofapps = DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_BUGNET_NUM_OF_APPS);
	}

	private void setDefaults() {
		protocol = DragonflyActivator.getDefault().getPluginPreferences().getDefaultString(DragonflyActivator.PREF_PROTOCOL);
		serverName = DragonflyActivator.getDefault().getPluginPreferences().getDefaultString(DragonflyActivator.PREF_SERVER_NAME);
		enabled = DragonflyActivator.getDefault().getPluginPreferences().getDefaultBoolean(DragonflyActivator.PREF_BUGNET_ENABLED);
		numofapps = DragonflyActivator.getDefault().getPluginPreferences().getDefaultString(DragonflyActivator.PREF_BUGNET_NUM_OF_APPS);
	}

	private void setServerAndProtocol() {
		// check for protocol
		String serverText = serverNameTextField.getText();
		String[] serverSplit = serverText.split(PROTOCOL_SEPARATOR);
		if (serverSplit.length > 1) {
			protocol = serverSplit[0] + PROTOCOL_SEPARATOR;
			serverText = serverSplit[1];
		} else {
			protocol = "https://";
		}
		serverName = serverText;		
	}
	
	private void resetChangePerform() {
		applyPerformed = false;
	}
}
