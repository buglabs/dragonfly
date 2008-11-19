/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AuthenticationDialog extends Dialog {

	private String username;

	private String pwd;

	private boolean saveAuthData = false;

	private Button btnSaveAuth;

	public static final int ACCOUNT_CREATE_ID = Integer.MAX_VALUE - 1;

	public AuthenticationDialog(Shell topShell) {
		super(topShell);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("AuthenticationDialog.0")); //$NON-NLS-1$
	}

	protected Control createDialogArea(Composite parent) {

		Composite top = new Composite(parent, SWT.NONE);
		GridData gdSpanAll = GridDataFactory.fillDefaults().create();
		gdSpanAll.horizontalSpan = 2;
		GridData gdFillH = GridDataFactory.fillDefaults().create();

		gdFillH.widthHint = 150;
		GridLayout layout = new GridLayout(2, false);
		top.setLayout(layout);

		Label lbl = new Label(top, SWT.NONE);
		lbl.setText(Messages.getString("AuthenticationDialog.1")); //$NON-NLS-1$
		lbl.setLayoutData(gdSpanAll);
		Label lblUserName = new Label(top, SWT.NONE);
		lblUserName.setText(Messages.getString("AuthenticationDialog.2")); //$NON-NLS-1$
		Text txtUserName = new Text(top, SWT.BORDER);
		txtUserName.setLayoutData(gdFillH);
		txtUserName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				username = ((Text) e.widget).getText();
			}
		});
		Label lblPassword = new Label(top, SWT.None);
		lblPassword.setText(Messages.getString("AuthenticationDialog.3")); //$NON-NLS-1$
		Text txtPassword = new Text(top, SWT.BORDER | SWT.PASSWORD);
		txtPassword.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				pwd = ((Text) e.widget).getText();
			}

		});
		txtPassword.setLayoutData(gdFillH);

		createSpacer(top);

		btnSaveAuth = new Button(top, SWT.CHECK);
		btnSaveAuth.setText(Messages.getString("AuthenticationDialog.4")); //$NON-NLS-1$
		btnSaveAuth.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

			public void widgetSelected(SelectionEvent e) {
				saveAuthData = ((Button) e.widget).getSelection();
			}

		});

		createSpacer(top);

		Link createAccountLink = new Link(top, SWT.NONE);
		createAccountLink.setText("If you have not yet created an account on BUGnet, you can do it <a>here</a>");

		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;

		createAccountLink.setLayoutData(gridData);

		createAccountLink.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setReturnCode(ACCOUNT_CREATE_ID);
				close();
			}
		});

		return top;
	}

	private void createSpacer(Composite top) {
		Layout layout = top.getLayout();
		if (layout instanceof GridLayout) {
			int columns = ((GridLayout) layout).numColumns;
			Label spacer = new Label(top, SWT.NONE);
			GridData gd = new GridData();
			gd.heightHint = 5;
			gd.horizontalSpan = columns;
			spacer.setLayoutData(gd);
		}
	}

	public String getUsername() {
		if (username == null) {
			username = "";
		}
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean getSaveAuthentication() {
		return saveAuthData;
	}

	public String getPwd() {
		if (pwd == null) {
			pwd = "";
		}

		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
}
