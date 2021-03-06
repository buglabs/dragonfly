/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/

package com.buglabs.dragonfly.ui.wizards.bugProject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.felix.launch.ProjectUtils;
import com.buglabs.dragonfly.model.AuthenticationData;
import com.buglabs.dragonfly.model.BugProjectInfo;
import com.buglabs.dragonfly.ui.Activator;

public class NewProjectMainPage extends WizardPage {

	public static final String PAGE_NAME = "NewProjectMainPage"; //$NON-NLS-1$
	public static final String PAGE_TITLE = Messages.getString("NewProjectMainPage.1"); //$NON-NLS-1$

	private Text txtName;
	private Text txtAuthorName;
	private BugProjectInfo pinfo;
	private int projectNameSize;
	private Text txtDescription;

	public NewProjectMainPage(BugProjectInfo pinfo) {
		super(PAGE_NAME, PAGE_TITLE, Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_COLOR_DIALOG_PROJECT));
		setMessage(Messages.getString("NewProjectMainPage.5")); //$NON-NLS-1$
		this.pinfo = pinfo;

		try {
			projectNameSize = Integer.parseInt(Activator.getString("PROJECT_NAME_SIZE"));
		} catch (Exception e) {
			projectNameSize = 150;
		}
	}

	public boolean isPageComplete() {

		setErrorMessage(null);

		String projName = pinfo.getProjectName();

		char[] cs = projName.toCharArray();
		if (cs.length != 0) {
			if (cs.length > projectNameSize) {
				setErrorMessage("Project name limit reached!");
				return false;
			}
			for (int i = 0; i < cs.length; i++) {
				if (cs[i] == ' ' || cs[i] == '_') {
					continue;
				}
				if (!Character.isLetterOrDigit(cs[i]) && cs[i] != '.') {
					setErrorMessage("Invalid project name.");
					return false;
				}
			}

			if (ResourcesPlugin.getWorkspace().validateName(projName, IFile.PROJECT).getSeverity() != IStatus.OK
					|| !JavaConventions.validatePackageName(ProjectUtils.formatNameToPackage(projName)).isOK()) {
				setErrorMessage("Invalid project name.");
				return false;
			}

			IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(pinfo.getProjectName());
			if (proj.exists()) {
				setErrorMessage("Project with name " + pinfo.getProjectName() + " already exists.");
				return false;
			}
			return true;
		}
		return false;
	}

	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		top.setLayout(layout);

		GridData gdFillH = new GridData(GridData.FILL_HORIZONTAL);
		GridData spanAll = new GridData(GridData.FILL_HORIZONTAL);
		spanAll.horizontalSpan = layout.numColumns;

		GridData spanAllFillBoth = new GridData(GridData.FILL_BOTH);
		spanAllFillBoth.horizontalSpan = layout.numColumns;

		GridData gdFillBoth = GridDataFactory.createFrom(gdFillH).create();

		Composite comp = new Composite(top, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		GridData compData = new GridData(GridData.FILL_HORIZONTAL);
		compData.horizontalSpan = layout.numColumns;
		compData.minimumHeight = 100;
		comp.setLayoutData(compData);
		Label lblName = new Label(comp, SWT.NONE);
		lblName.setText(Messages.getString("NewProjectMainPage.8")); //$NON-NLS-1$

		txtName = new Text(comp, SWT.BORDER);
		txtName.setLayoutData(gdFillH);
		txtName.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				pinfo.setProjectName(((Text) e.widget).getText());

				String activator = ProjectUtils.formatNameToPackage(pinfo.getProjectName());

				/*char[] charArray = activator.toCharArray();
				charArray[0] = Character.toUpperCase(charArray[0]);
				activator = new String(charArray);*/

				pinfo.setActivator(activator + ".Activator");
				pinfo.setSymbolicName(ProjectUtils.formatName(pinfo.getProjectName()));
				setPageComplete(true);
			}
		});

		Label lblAuthorName = new Label(comp, SWT.NONE);
		lblAuthorName.setText("Author:");

		txtAuthorName = new Text(comp, SWT.BORDER);
		txtAuthorName.setLayoutData(gdFillH);
		AuthenticationData authData = DragonflyActivator.getDefault().getAuthenticationData();
		txtAuthorName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				pinfo.setVendor(((Text) e.widget).getText());
				setPageComplete(true);
			}
		});

		if (authData != null) {
			String username = authData.getUsername();
			if (username != null) {
				txtAuthorName.setText(username);
			}
		}
		
		Label lblDescription = new Label(comp, SWT.NONE);
		lblDescription.setText("Description:");
		
		txtDescription = new Text(comp, SWT.BORDER);
		txtDescription.setLayoutData(gdFillH);
		txtDescription.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				pinfo.setDescription(((Text) e.widget).getText());
				setPageComplete(true);
			}
		});

		txtName.setFocus();

		setControl(top);
	}
}
