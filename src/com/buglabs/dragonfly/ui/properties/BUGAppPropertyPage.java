package com.buglabs.dragonfly.ui.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * A property page for Dragonfly specific settings for BUGapp projects.
 * @author kgilmer
 *
 */
public class BUGAppPropertyPage extends PropertyPage {

	public static final String AUTO_INSTALL_BUGAPP_PROPERTY = "AUTO_INSTALL_BUGAPP_PROPERTY";
	private Button autoInstallBtn;

	private void addFirstSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		autoInstallBtn = new Button(composite, SWT.CHECK);		
		autoInstallBtn.setText("Auto install in BUG Simulator upon start.");
			
		//pathValueText.setText(((IResource) getElement()).getFullPath().toString());
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addFirstSection(composite);
	
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {
		super.performDefaults();		
		autoInstallBtn.setSelection(false);
	}
	
	public boolean performOk() {
		// store the value in the owner text field
		try {
			((IResource) getElement()).setPersistentProperty(
				new QualifiedName("", AUTO_INSTALL_BUGAPP_PROPERTY),
				Boolean.toString(autoInstallBtn.getSelection()));
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

}