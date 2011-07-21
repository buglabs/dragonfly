package com.buglabs.dragonfly.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class BUGSDKPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public BUGSDKPreferencePage() {
		// TODO Auto-generated constructor stub
	}

	public BUGSDKPreferencePage(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public BUGSDKPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		Label description = new Label(composite, SWT.NONE);
		description.setText("Set BUG specific properties here");

		return composite;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}
}
