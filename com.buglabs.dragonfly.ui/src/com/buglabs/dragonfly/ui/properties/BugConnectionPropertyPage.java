package com.buglabs.dragonfly.ui.properties;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.ui.actions.RefreshBugAction;
import com.buglabs.dragonfly.ui.wizards.bug.Messages;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Property page for BUG Connection to change connection url.
 * 
 * @author akravets
 * 
 */
public class BugConnectionPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	private Text txtBugAddress;

	private boolean isEnabled = true;

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		GridData gdRight = new GridData(GridData.FILL_HORIZONTAL);

		Label lblBugAddress = new Label(composite, SWT.NONE);
		lblBugAddress.setText(Messages.getString("ConnectBug_MainPage.BUG_ADDRESS_LABEL")); //$NON-NLS-1$
		lblBugAddress.setToolTipText(Messages.getString("ConnectBug_MainPage.BUG_ADDRESS_TOOLTIP")); //$NON-NLS-1$

		txtBugAddress = new Text(composite, SWT.BORDER);
		txtBugAddress.setToolTipText(Messages.getString("ConnectBug_MainPage.BUG_ADDRESS_TOOLTIP")); //$NON-NLS-1$
		txtBugAddress.setLayoutData(gdRight);

		String url = getBugURL();

		txtBugAddress.setEditable(isEnabled);

		if (url == null) {
			setErrorMessage("Error retrieving url address");
		} else {
			txtBugAddress.setText(url);
		}

		txtBugAddress.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					new URL(txtBugAddress.getText());
					setErrorMessage(null);
					setValid(true);
				} catch (MalformedURLException murle) {
					setErrorMessage("Please enter a valid address.");
					setValid(false);
				}
			}

		});

		return composite;
	}

	/**
	 * @return Returns URL for this project
	 */
	private String getBugURL() {
		try {
			BugConnection bugConnection = (BugConnection) getElement().getAdapter(BugConnection.class);

			// only static bugs can have editable properties
			if (!(bugConnection instanceof StaticBugConnection)) {
				isEnabled = false;
			}

			return bugConnection.getUrl().toExternalForm();
		} catch (Exception e) {
			UIUtils.handleNonvisualError("Error getting BUG url", e);
		}
		return null;
	}

	public boolean performOk() {
		IAdaptable adap = getElement();
		try {
			BugConnection node = (BugConnection) adap.getAdapter(BugConnection.class);
			node.setConnected(false);
			node.getChildren().clear();
			node.setUrl(new URL(txtBugAddress.getText()));
			new RefreshBugAction(node).run();
		} catch (Exception e) {
			setErrorMessage("Unable to apply changes");
			UIUtils.handleNonvisualError("Unable to apply changes", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	protected void performDefaults() {
		IAdaptable adap = getElement();
		StaticBugConnection node = (StaticBugConnection) adap.getAdapter(BugConnection.class);
		String defaultUrl = node.getDefaultUrl();
		txtBugAddress.setText(defaultUrl);
	}
}
