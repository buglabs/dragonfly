package com.buglabs.dragonfly.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.buglabs.dragonfly.DragonflyActivator;

/**
 * Preference page that defines http port that BUG Simulator listens to
 * 
 * @author akravets
 * 
 */
public class VirtualBugPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text httpPort;

	protected String httpPortValue;

	public VirtualBugPreferencePage() {
		super("BUG Simulator");
	}

	/*
	 * public VirtualBugPreferencePage(String title) { super(title); // TODO
	 * Auto-generated constructor stub }
	 * 
	 * public VirtualBugPreferencePage(String title, ImageDescriptor image) {
	 * super(title, image); // TODO Auto-generated constructor stub }
	 */

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label lblHttpPort = new Label(composite, SWT.NONE);
		lblHttpPort.setText("HTTP Port:");

		httpPort = new Text(composite, SWT.BORDER);
		GridData gData = new GridData();
		gData.widthHint = 50;
		httpPort.setLayoutData(gData);

		httpPort.setText(httpPortValue);

		httpPort.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				httpPortValue = httpPort.getText();
				long min_port = Long.parseLong(DragonflyActivator.getDefault().getString("HTTP_PORT_MIN"));
				long max_port = Long.parseLong(DragonflyActivator.getDefault().getString("HTTP_PORT_MAX"));
				if (Long.parseLong(httpPortValue) < min_port || Long.parseLong(httpPortValue) > max_port) {
					setErrorMessage("Port number must be between " + String.valueOf(min_port) + " and " + String.valueOf(max_port));
					setValid(false);
				} else {
					setErrorMessage(null);
					setValid(true);
				}
			}
		});

		return composite;
	}

	public boolean performOk() {
		DragonflyActivator.getDefault().getPluginPreferences().setValue(DragonflyActivator.PREF_DEFAULT_BUGPORT, httpPortValue);
		return true;
	}

	protected void performDefaults() {
		super.performDefaults();

		DragonflyActivator.getDefault().internalInitializeDefaultPluginPreferences();
		httpPortValue = DragonflyActivator.getDefault().getPluginPreferences().getDefaultString(DragonflyActivator.PREF_DEFAULT_BUGPORT);
		httpPort.setText(httpPortValue);
	}

	public void init(IWorkbench workbench) {
		httpPortValue = DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_DEFAULT_BUGPORT);
	}
}
