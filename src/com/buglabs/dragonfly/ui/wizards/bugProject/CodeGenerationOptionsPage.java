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

public class CodeGenerationOptionsPage  extends WizardPage implements IDebugEventSetListener {

	private static final String PAGE_NAME = "CodeGenerationOptionsPage";
	private static final String PAGE_TITLE = "Code Generation Options";
	private final BugProjectInfo pinfo;
	
	private Button btnCodeInActivator;
	private Button btnGenerateThreadApp;

	protected CodeGenerationOptionsPage(BugProjectInfo pinfo) {
		super(PAGE_NAME, PAGE_TITLE, Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_COLOR_DIALOG_PROJECT));
		this.pinfo = pinfo;
	}

	
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		mainComposite.setLayout(layout);
		
		// checkbox for creating application loop
		createApplicationLoop(mainComposite);
		
		createOtherOptions(mainComposite);
		
		setControl(mainComposite);
	}
	
	private void createOtherOptions(Composite mc) {
		Group g = new Group(mc, SWT.BORDER);
		g.setLayout(new GridLayout());
		g.setText("General Options");
		g.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final Button btnAddLogCode = new Button(g, SWT.CHECK);
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		btnAddLogCode.setLayoutData(gd);
		btnAddLogCode.setText("Generate static logging method in Activator.");
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
			boolean codeEnabled = pinfo.getServices() != null && pinfo.getServices().size() > 0;
			btnCodeInActivator.setEnabled(codeEnabled);
			btnGenerateThreadApp.setEnabled(codeEnabled);
			
			if (codeEnabled) {
				btnGenerateThreadApp.setSelection(true);
			} else {
				btnCodeInActivator.setSelection(false);
				btnGenerateThreadApp.setSelection(false);
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
		
		btnGenerateThreadApp = new Button(g, SWT.RADIO);
		GridData genAppGD = new GridData(GridData.FILL_HORIZONTAL);
		genAppGD.horizontalSpan = 2;
		genAppGD.heightHint = 30;
		btnGenerateThreadApp.setLayoutData(genAppGD);
		btnGenerateThreadApp.setText("Generate seperate application class.");

		btnGenerateThreadApp.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {/*unused here*/
			}

			public void widgetSelected(SelectionEvent e) {
				pinfo.setShouldGenerateApplicationLoop(true);
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
				pinfo.setShouldGenerateApplicationLoop(false);
			}
		});
	}

	
	public void handleDebugEvents(DebugEvent[] arg0) {
		// TODO Auto-generated method stub
		
	}

}
