package com.buglabs.dragonfly.ui.wizards.bug;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jdt.core.JavaConventions;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.felix.launch.ProjectUtils;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.jobs.TestConnectionJob;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Main page for the Bug Connection Project.
 * 
 * @author angel
 * 
 */
public class ConnectBug_MainPage extends WizardPage {

	public static final String PAGE_TITLE = Messages.getString("ConnectBug_MainPage.PAGE_TITLE"); //$NON-NLS-1$

	public static final String PAGE_NAME = Messages.getString("ConnectBug_MainPage.PAGE_NAME"); //$NON-NLS-1$

	private Text txtProjectName;

	private Text txtBugAddress;

	private ITreeNode root;

	private String bugAddress;

	protected boolean isProjectNameError = false;

	private Button testConnectionButton;

	public ConnectBug_MainPage() {
		super(PAGE_NAME, PAGE_TITLE, Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_COLOR_DIALOG_CONNECTION));
		setMessage(Messages.getString("GeneralPage.4")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		root = BugConnectionManager.getInstance().getBugConnectionsRoot();
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		top.setLayout(layout);

		GridData gdRight = new GridData(GridData.FILL_HORIZONTAL);
		Label lblProjectName = new Label(top, SWT.NONE);
		lblProjectName.setText(Messages.getString("ConnectBug_MainPage.NAME")); //$NON-NLS-1$
		lblProjectName.setToolTipText(Messages.getString("ConnectBug_MainPage.PROJECT_NAME_TOOLTIP")); //$NON-NLS-1$

		txtProjectName = new Text(top, SWT.BORDER);
		txtProjectName.setLayoutData(gdRight);
		txtProjectName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(true);
			}
		});

		txtProjectName.setFocus();

		Label blank = new Label(top, SWT.NONE);
		blank.setText("");

		Label lblBugAddress = new Label(top, SWT.NONE);
		lblBugAddress.setText(Messages.getString("ConnectBug_MainPage.BUG_ADDRESS_LABEL")); //$NON-NLS-1$
		lblBugAddress.setToolTipText(Messages.getString("ConnectBug_MainPage.BUG_ADDRESS_TOOLTIP")); //$NON-NLS-1$

		txtBugAddress = new Text(top, SWT.BORDER);
		txtBugAddress.setToolTipText(Messages.getString("ConnectBug_MainPage.BUG_ADDRESS_TOOLTIP")); //$NON-NLS-1$
		txtBugAddress.setLayoutData(gdRight);
		txtBugAddress.setText("http://");

		// place cursor at the end of the text
		txtBugAddress.setSelection(txtBugAddress.getText().length());

		txtBugAddress.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				setPageComplete(true);
				bugAddress = txtBugAddress.getText();
			}

		});

		testConnectionButton = new Button(top, SWT.NONE);
		testConnectionButton.setText("Test Connection");
		testConnectionButton.setEnabled(false);
		testConnectionButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

			public void widgetSelected(SelectionEvent e) {
				TestConnectionJob con = new TestConnectionJob(bugAddress);
				con.schedule();
				con.addJobChangeListener(new IJobChangeListener() {

					public void aboutToRun(IJobChangeEvent event) {
						// TODO Auto-generated method stub

					}

					public void awake(IJobChangeEvent event) {
						// TODO Auto-generated method stub

					}

					public void done(IJobChangeEvent event) {
						if (event.getResult().isOK())
							UIUtils.giveVisualInformation("Successfully connected to " + bugAddress);
					}

					public void running(IJobChangeEvent event) {
						// TODO Auto-generated method stub

					}

					public void scheduled(IJobChangeEvent event) {
						// TODO Auto-generated method stub

					}

					public void sleeping(IJobChangeEvent event) {
						// TODO Auto-generated method stub

					}

				});
			}
		});
		setControl(top);
	}

	public boolean isPageComplete() {

		setErrorMessage(null);

		if (UIUtils.stringEmpty(getBugAddress())) {
			setErrorMessage(Messages.getString("Address must not be empty."));
			disable();
			return false;
		}

		try {
			URL bugUrl = new URL(getBugAddress());
		} catch (Exception e) {
			setErrorMessage(Messages.getString("Please enter a valid address."));
			disable();
			return false;
		}

		String connectionName = txtProjectName.getText();
		char[] cs = connectionName.toCharArray();

		if (cs.length != 0) {
			if (root.getChildren(connectionName).size() != 0) {
				setErrorMessage("BUG Connection with this name already exists.");
				disable();
				return false;
			}

			for (int i = 0; i < cs.length; i++) {
				if (cs[i] == ' ' || cs[i] == '_') {
					continue;
				}
				if (!Character.isLetterOrDigit(cs[i])) {
					setErrorMessage("Invalid connection name.");
					disable();
					return false;
				}
			}

			Collection children = root.getChildren();
			Iterator iterator = children.iterator();
			while (iterator.hasNext()) {
				BugConnection bugConnection = (BugConnection) iterator.next();

				String bugAddress = getBugAddress();
				String bcAddress = (bugAddress.endsWith("//") || bugAddress.endsWith("/")) ? bugAddress.substring(0, bugAddress.indexOf("/")) : bugAddress;
				if (bugConnection.getUrl().toString().equals(bcAddress)) {
					setErrorMessage("BUG Connection with this url already exists.");
					disable();
					return false;
				}
			}

			if (ResourcesPlugin.getWorkspace().validateName(connectionName, IFile.PROJECT).getSeverity() != IStatus.OK
					|| !JavaConventions.validatePackageName(ProjectUtils.formatNameToPackage(connectionName)).isOK()) {
				setErrorMessage("Invalid connection name.");
				disable();
				return false;
			}
			enable();
			return true;
		}
		disable();
		return false;
	}

	public String getProjectName() {
		return txtProjectName.getText();
	}

	private void disable() {
		testConnectionButton.setEnabled(false);
	}

	private void enable() {
		testConnectionButton.setEnabled(true);
	}

	public String getBugAddress() {
		return txtBugAddress.getText();
	}

	public URL getBugURL() throws MalformedURLException {
		String bugAddress2 = getBugAddress();
		return new URL(getBugAddress());
	}

}
