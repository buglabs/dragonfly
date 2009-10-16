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
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.AuthenticationData;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.info.BugProjectInfo;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;

public class NewProjectMainPage extends WizardPage {

	public static final String PAGE_NAME 				= "NewProjectMainPage"; //$NON-NLS-1$
	public static final String PAGE_TITLE 				= Messages.getString("NewProjectMainPage.1"); //$NON-NLS-1$
	private static final String JRE_GROUP_TITLE 		= Messages.getString("NewProjectMainPage.JRE_GROUP_TITLE"); //$NON-NLS-1$
	private static final String PHONEME_RADIO_TEXT 		= Messages.getString("NewProjectMainPage.PHONEME_RADIO_TEXT"); //$NON-NLS-1$
	private static final String JRE_RADIO_TEXT 			= String.format(
															Messages.getString("NewProjectMainPage.JRE_RADIO_TEXT"), 
															JavaCore.VERSION_1_6);	//$NON-NLS-1$											
	private static final String JAVA_1_6_EXECUTION_ENV	= "JavaSE-1.6";	//$NON-NLS-1$
	
	private Text txtName;
	private Text txtAuthorName;
	private BugProjectInfo pinfo;
	private int projectNameSize;

	public NewProjectMainPage(BugProjectInfo pinfo) {
		super(PAGE_NAME, PAGE_TITLE, Activator.getDefault().getImageRegistry()
				.getDescriptor(Activator.IMAGE_COLOR_DIALOG_PROJECT));
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
				if (!Character.isLetterOrDigit(cs[i])) {
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
		
		if(authData != null) {
			String username = authData.getUsername();
			if(username != null) {
			txtAuthorName.setText(username);
			}
		}

		txtName.setFocus();
		
		// Draw the JRE group if we have Java 1.6
		if (hasJava_1_6()) {
			Control jreControl = (new JREGroup(pinfo)).createControl(top);
			jreControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		setControl(top);
	}

	/**
	 * Does this system have a java 1.6 execution environment?
	 *
	 * this method of getting the EE is used by eclipse,
	 * see: org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne
	 * 		
	 * @return
	 */
	private boolean hasJava_1_6() {
		IExecutionEnvironment[] executionEnvs = 
			JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments();

		for (int i= 0; i < executionEnvs.length; i++) {
			// This is how eclipse does it, by matching EE's Id string to find compliance
			if (executionEnvs[i].getId().indexOf(JavaCore.VERSION_1_6) != -1)
				return true;
		}
		return false;
	}

	/**
	 * This helper class is for creating the JRE Group which allows the user
	 * to select the execution environment for their BUG App
	 * 
	 * @author bbballantine
	 *
	 */
	private final class JREGroup {
	
		private Group group;
		private Button phoneme_radio;
		private Button jre_radio;
		private BugProjectInfo pinfo;

		public JREGroup(BugProjectInfo pinfo) {
			this.pinfo = pinfo;
		}
		
		public Control createControl(Composite composite) {
			group = new Group(composite, SWT.NONE);
			group.setLayout(new GridLayout(2, false));
			group.setFont(composite.getFont());
			group.setText(JRE_GROUP_TITLE);
			
			phoneme_radio = new Button(group, SWT.RADIO);
			phoneme_radio.setText(PHONEME_RADIO_TEXT);
			phoneme_radio.setSelection(true);
			GridData gdSpan = new GridData(SWT.LEFT);
			gdSpan.horizontalSpan = 2;
			phoneme_radio.setLayoutData(gdSpan);
			phoneme_radio.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					jre_radio.setSelection(false);
					phoneme_radio.setSelection(true);
					pinfo.setExecutionEnvironment("");
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {/* not used */}
			});
			
			jre_radio = new Button(group, SWT.RADIO);
			jre_radio.setText(JRE_RADIO_TEXT);
			jre_radio.setLayoutData(new GridData(SWT.LEFT));
			jre_radio.addSelectionListener(new SelectionListener() {
				
				public void widgetSelected(SelectionEvent e) {
					phoneme_radio.setSelection(false);
					jre_radio.setSelection(true);
					pinfo.setExecutionEnvironment(JAVA_1_6_EXECUTION_ENV);
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {/* not used */}
			});
						
			return group;
		}
	}
	
}
