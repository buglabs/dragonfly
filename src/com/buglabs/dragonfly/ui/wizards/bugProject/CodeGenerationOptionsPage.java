/*******************************************************************************
 * Copyright (c) 2011 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.wizards.bugProject;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.buglabs.dragonfly.model.BugProjectInfo;
import com.buglabs.dragonfly.ui.Activator;

/**
 * Wizard page for code generation options.  These options determine how the resultant BUGapp project is structured.
 * @author kgilmer
 *
 */
public class CodeGenerationOptionsPage  extends WizardPage implements IDebugEventSetListener {

	private static final String PAGE_NAME = "CodeGenerationOptionsPage";
	private static final String PAGE_TITLE = "Code Generation Options";
	private final BugProjectInfo pinfo;
	
	private Button btnCodeInActivator;
	private Button btnSeparateApplicationClass;

	protected CodeGenerationOptionsPage(BugProjectInfo pinfo) {
		super(PAGE_NAME, PAGE_TITLE, Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_COLOR_DIALOG_PROJECT));
		this.pinfo = pinfo;
	}

	
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		mainComposite.setLayout(layout);
		
		createApplicationLoop(mainComposite);
		createOtherOptions(mainComposite);
		
		setControl(mainComposite);
	}
	
	/**
	 * Create misc code generation UI
	 * @param mc
	 */
	private void createOtherOptions(Composite mc) {
		Group g = new Group(mc, SWT.BORDER);
		g.setLayout(new GridLayout());
		g.setText("General Options");
		g.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final Button btnAddLogCode = new Button(g, SWT.CHECK);
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		btnAddLogCode.setLayoutData(gd);
		btnAddLogCode.setText("Generate static logging method in Activator.");
		btnAddLogCode.setSelection(true);
		btnAddLogCode.addSelectionListener(new SelectionListener() {
			
			
			public void widgetSelected(SelectionEvent arg0) {	
				pinfo.setGenerateLogMethod(btnAddLogCode.getSelection());
			}
			
			
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}
		});
		
		final Button btnAddDebugCode = new Button(g, SWT.CHECK);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		btnAddDebugCode.setLayoutData(gd);
		btnAddDebugCode.setText("Generate debug statements.");
		btnAddDebugCode.addSelectionListener(new SelectionListener() {
			
			
			public void widgetSelected(SelectionEvent arg0) {	
				pinfo.setGenerateDebugStatements(btnAddDebugCode.getSelection());
			}
			
			
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}
		});
	}

	
	public void setVisible(boolean visible) {
		if (visible) {
			boolean codeEnabled = pinfo.getOSGiServices().size() > 0 || pinfo.getModuleServices().size() > 0;
			btnCodeInActivator.setEnabled(codeEnabled);
			btnSeparateApplicationClass.setEnabled(codeEnabled);
			
			if (codeEnabled) {
				btnSeparateApplicationClass.setSelection(true);
				pinfo.setGenerateSeparateApplicationClass(true);
			} else {
				btnCodeInActivator.setSelection(false);
				btnSeparateApplicationClass.setSelection(false);
			}
		}
		super.setVisible(visible);
	}
	
	/**
	 * Creates application loop button
	 * 
	 * @param mainComposite
	 */
	private void createApplicationLoop(Composite mainComposite) {
		Group g = new Group(mainComposite, SWT.BORDER);
		g.setLayout(new GridLayout());
		g.setText("Application Structure");
		g.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btnSeparateApplicationClass = new Button(g, SWT.RADIO);
		GridData genAppGD = new GridData(GridData.FILL_HORIZONTAL);
		genAppGD.horizontalSpan = 2;
		genAppGD.heightHint = 30;
		btnSeparateApplicationClass.setLayoutData(genAppGD);
		btnSeparateApplicationClass.setText("Generate separate application class.");
		btnSeparateApplicationClass.setSelection(true);

		btnSeparateApplicationClass.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {/*unused here*/
			}

			public void widgetSelected(SelectionEvent e) {
				pinfo.setGenerateSeparateApplicationClass(true);
			}
		});
		
		btnCodeInActivator = new Button(g, SWT.RADIO);
		genAppGD = new GridData(GridData.FILL_HORIZONTAL);
		genAppGD.horizontalSpan = 2;
		genAppGD.heightHint = 30;
		btnCodeInActivator.setLayoutData(genAppGD);
		btnCodeInActivator.setText("Generate service binding code in Activator class.");

		btnCodeInActivator.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {/*unused here*/
			}

			public void widgetSelected(SelectionEvent e) {
				pinfo.setGenerateSeparateApplicationClass(false);
			}
		});
	}

	
	public void handleDebugEvents(DebugEvent[] arg0) {
		// TODO Auto-generated method stub
		
	}

}
