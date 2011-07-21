package com.buglabs.dragonfly.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * The main page of the Bug Bundle Manifest Editor.
 * 
 * @author Angel Roman
 * 
 */
public class BugBundleMainPage extends FormPage {
	public final static String PAGE_TITLE = Messages.getString("BugBundleMainPage.0"); //$NON-NLS-1$

	public final static String PAGE_ID = "BugBundle_Main_Page"; //$NON-NLS-1$

	public BugBundleMainPage(FormEditor editor) {
		super(editor, PAGE_ID, PAGE_TITLE);
	}

	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit tk = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();

		form.setText(PAGE_TITLE);
		Composite top = form.getBody();
		top.setLayout(new ColumnLayout());

		Section mainSection = createSection(Messages.getString("BugBundleMainPage.28"), Messages.getString("BugBundleMainPage.3")); //$NON-NLS-1$ //$NON-NLS-2$
		Composite mainComposite = (Composite) mainSection.getClient();
		GridData gdRight = new GridData(GridData.FILL_HORIZONTAL);
		Label lblName = tk.createLabel(mainComposite, Messages.getString("BugBundleMainPage.4")); //$NON-NLS-1$
		Text txtName = tk.createText(mainComposite, ""); //$NON-NLS-1$
		txtName.setLayoutData(gdRight);
		Label lblVersion = tk.createLabel(mainComposite, Messages.getString("BugBundleMainPage.6")); //$NON-NLS-1$
		Text txtVersion = tk.createText(mainComposite, ""); //$NON-NLS-1$
		txtVersion.setLayoutData(gdRight);

		Label lblID = tk.createLabel(mainComposite, Messages.getString("BugBundleMainPage.8")); //$NON-NLS-1$
		Text txtID = tk.createText(mainComposite, ""); //$NON-NLS-1$
		txtID.setLayoutData(gdRight);

		Label lblActivator = tk.createLabel(mainComposite, Messages.getString("BugBundleMainPage.10")); //$NON-NLS-1$
		lblActivator.setToolTipText(Messages.getString("BugBundleMainPage.11")); //$NON-NLS-1$
		Text txtActivator = tk.createText(mainComposite, ""); //$NON-NLS-1$
		txtActivator.setLayoutData(gdRight);

		Label lblProvider = tk.createLabel(mainComposite, Messages.getString("BugBundleMainPage.13")); //$NON-NLS-1$
		Text txtProvider = tk.createText(mainComposite, ""); //$NON-NLS-1$
		txtProvider.setLayoutData(gdRight);

		Label lblBugBundleType = tk.createLabel(mainComposite, Messages.getString("BugBundleMainPage.15")); //$NON-NLS-1$
		lblBugBundleType.setText(Messages.getString("BugBundleMainPage.16")); //$NON-NLS-1$

		Combo cmbBugBundleType = new Combo(mainComposite, SWT.READ_ONLY);
		cmbBugBundleType
				.setItems(new String[] { Messages.getString("BugBundleMainPage.17"), Messages.getString("BugBundleMainPage.18"), Messages.getString("BugBundleMainPage.19") }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cmbBugBundleType.select(0);
		cmbBugBundleType.setLayoutData(gdRight);

		Section modulesSection = createSection(Messages.getString("BugBundleMainPage.20"), Messages.getString("BugBundleMainPage.21")); //$NON-NLS-1$ //$NON-NLS-2$
		Composite modulesComposite = (Composite) modulesSection.getClient();
		modulesComposite.setLayout(new GridLayout(2, false));
		modulesSection.setClient(modulesComposite);
		addModules(modulesComposite);

		Section requiredBundlesSection = createSection(Messages.getString("BugBundleMainPage.22"), Messages.getString("BugBundleMainPage.23")); //$NON-NLS-1$ //$NON-NLS-2$
		Composite requiredBundlesComposite = (Composite) requiredBundlesSection.getClient();
	}

	private void addModules(Composite modulesComposite) {
		Button btnCamera = new Button(modulesComposite, SWT.CHECK);
		btnCamera.setText(Messages.getString("BugBundleMainPage.24")); //$NON-NLS-1$
		Button btnGPS = new Button(modulesComposite, SWT.CHECK);
		btnGPS.setText(Messages.getString("BugBundleMainPage.25")); //$NON-NLS-1$
		Button btnLCD = new Button(modulesComposite, SWT.CHECK);
		btnLCD.setText(Messages.getString("BugBundleMainPage.26")); //$NON-NLS-1$
		Button btnMotionSensor = new Button(modulesComposite, SWT.CHECK);
		btnMotionSensor.setText(Messages.getString("BugBundleMainPage.27")); //$NON-NLS-1$
	}

	/**
	 * Creates a section given the title and description.
	 * 
	 * @param title
	 * @param description
	 * @return
	 */
	private Section createSection(String title, String description) {
		ScrolledForm form = getManagedForm().getForm();
		FormToolkit tk = getManagedForm().getToolkit();
		Section sect = tk.createSection(form.getBody(), Section.TITLE_BAR | Section.DESCRIPTION);
		sect.setText(title);
		sect.setDescription(description);
		Composite comp = tk.createComposite(sect, SWT.BORDER);
		comp.setLayout(new GridLayout(2, false));
		sect.setClient(comp);
		return sect;
	}
}
