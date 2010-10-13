package com.buglabs.dragonfly.ui.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.launch.VirtualBugLaunchConfigurationDelegate;
import com.buglabs.dragonfly.ui.launch.VirtualBugLaunchProjectsSectionDrawer.ILaunchProjectSelectionListener;
import com.buglabs.dragonfly.ui.util.BugProjectUtil;
import com.buglabs.dragonfly.util.UIUtils;

public class VirtualBugTab extends AbstractLaunchConfigurationTab {

	private final static String GPS_LOG_LABEL = "GPS Log: ";
	private final static String ACC_LOG_LABEL = "Accelerometer Log: ";
	private final static String HTTP_PORT_LABEL = "HTTP Port:";
	private final static String TAB_NAME = "BUG Simulator";

	private Text httpPort;
	private String httpPortValue;
	private Text txtGpsLog;
	private Text txtAccelerometerLog;
	private Text txtImages;

	public VirtualBugTab() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return TAB_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		Label lblHttpPort = new Label(composite, SWT.NONE);
		lblHttpPort.setText(HTTP_PORT_LABEL);

		httpPort = new Text(composite, SWT.BORDER);
		GridData gData = new GridData();
		gData.widthHint = 50;
		gData.horizontalSpan = 2;
		httpPort.setLayoutData(gData);
		httpPort.setText(String.valueOf(httpPortValue));
		httpPort.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				e.doit = true;
				if (!Character.isDigit(e.character)) {
					if (e.character != SWT.BS)
						e.doit = false;
				}
			}
		});

		httpPort.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				httpPortValue = httpPort.getText();
				if (!httpPortValue.equals("")) {
					long min_port = Long.parseLong(DragonflyActivator.getDefault().getString("HTTP_PORT_MIN"));
					long max_port = Long.parseLong(DragonflyActivator.getDefault().getString("HTTP_PORT_MAX"));
					if (Long.parseLong(httpPortValue) < min_port || Long.parseLong(httpPortValue) > max_port) {
						setErrorMessage("Port number must be between " + String.valueOf(min_port) + " and " + String.valueOf(max_port));
					} else {
						setErrorMessage(null);
					}
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		});

		GridData gdText = new GridData(GridData.FILL_HORIZONTAL);
		gdText.grabExcessHorizontalSpace = true;

		GridData gdButton = new GridData();
		gdButton.horizontalAlignment = SWT.BEGINNING;

		// create GPS field
		Label gpsLabel = new Label(composite, SWT.NONE);
		gpsLabel.setText(GPS_LOG_LABEL);
		txtGpsLog = new Text(composite, SWT.BORDER);
		createLog(composite, gdText, txtGpsLog, gdButton);

		// create ACCELEROMETER field
		Label accLabel = new Label(composite, SWT.NONE);
		accLabel.setText(ACC_LOG_LABEL);
		txtAccelerometerLog = new Text(composite, SWT.BORDER);
		createLog(composite, gdText, txtAccelerometerLog, gdButton);

		// create IMAGE field
		Label lblImages = new Label(composite, SWT.NONE);
		lblImages.setText("Images: ");
		txtImages = new Text(composite, SWT.BORDER);
		createLog(composite, gdText, txtImages, gdButton);

		setControl(composite);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		httpPortValue = httpPort.getText();
		if (!httpPortValue.equals("")) {
			long min_port = Long.parseLong(DragonflyActivator.getDefault().getString("HTTP_PORT_MIN"));
			long max_port = Long.parseLong(DragonflyActivator.getDefault().getString("HTTP_PORT_MAX"));
			if (Long.parseLong(httpPortValue) < min_port || Long.parseLong(httpPortValue) > max_port) {
				return false;
			}
		}

		String gpsLogFile = txtGpsLog.getText();
		if (gpsLogFile.length() > 0) {
			File gpsLog = new File(gpsLogFile);
			if (!gpsLog.exists()) {
				return false;
			}
		}

		String imagesFile = txtImages.getText();
		if (imagesFile.length() > 0) {
			File images = new File(imagesFile);
			if (!images.exists()) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {

		// listen to the port as defined in SDK preferences
		httpPortValue = DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_DEFAULT_BUGPORT);
		httpPort.setText(String.valueOf(httpPortValue));

		try {
			String systemProperty = getSystemProperty(configuration, VirtualBugLaunchConfigurationDelegate.PROP_CAMERA_SNAPSHOTS, ""); //$NON-NLS-1$
			txtImages.setText(getSystemProperty(configuration, VirtualBugLaunchConfigurationDelegate.PROP_CAMERA_SNAPSHOTS, ""));
		} catch (CoreException e) {
			UIUtils.handleVisualError("Unable to initialize images.", e);
		}

		try {
			String systemProperty = getSystemProperty(configuration, VirtualBugLaunchConfigurationDelegate.PROP_GPS_LOG, ""); //$NON-NLS-1$
			txtGpsLog.setText(getSystemProperty(configuration, VirtualBugLaunchConfigurationDelegate.PROP_GPS_LOG, ""));
		} catch (CoreException e) {
			UIUtils.handleVisualError("Unable to initialize GPS log.", e);
		}

		try {
			txtAccelerometerLog.setText(getSystemProperty(configuration, VirtualBugLaunchConfigurationDelegate.PROP_ACC_LOG, ""));
		} catch (CoreException e) {
			UIUtils.handleVisualError("Unable to initialize Accelerometer log.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.
	 * debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		try {
			VirtualBugLaunchConfigurationInitializer.initializeSystemProperties(configuration);
		} catch (CoreException e1) {
			e1.printStackTrace();
			UIUtils.handleNonvisualError(e1.getMessage(), e1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse
	 * .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		try {
			// here make a new copy of the config hash and save the copy so
			// revert button works
			Map properties = configuration.getAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, new HashMap());
			Map<String, String> propertiesCopy = new HashMap<String, String>(properties);
			propertiesCopy.put(VirtualBugLaunchConfigurationDelegate.PROP_HTTP_PORT, httpPortValue);
			propertiesCopy.put(VirtualBugLaunchConfigurationDelegate.PROP_CAMERA_SNAPSHOTS, txtImages.getText());
			propertiesCopy.put(VirtualBugLaunchConfigurationDelegate.PROP_GPS_LOG, txtGpsLog.getText());
			propertiesCopy.put(VirtualBugLaunchConfigurationDelegate.PROP_ACC_LOG, txtAccelerometerLog.getText());
			configuration.setAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, propertiesCopy);
		} catch (CoreException e) {
			e.printStackTrace();
			UIUtils.handleNonvisualError(e.getMessage(), e);
		}

		DragonflyActivator.getDefault().getPluginPreferences().setValue(DragonflyActivator.PREF_DEFAULT_BUGPORT, httpPortValue);

	}

	private String getSystemProperty(ILaunchConfiguration configuration, String prop, String defaultValue) throws CoreException {
		Map properties = configuration.getAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, new HashMap());
		String val = (String) properties.get(prop);

		if (val != null) {
			return val;
		}

		return defaultValue;
	}

	private void createLog(Composite composite, GridData gdText, Text textField, GridData gdButton) {
		textField.setLayoutData(gdText);
		textField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});
		Button btnBrowse = new Button(composite, SWT.PUSH);
		btnBrowse.setText("Browse...");
		btnBrowse.setLayoutData(gdButton);
		btnBrowse.addMouseListener(new BrowseMouseListener(textField));
	}

	private List getBugProjectNames() throws CoreException {
		ArrayList names = new ArrayList();
		Iterator iter;
		iter = BugProjectUtil.getWSBugProjects().iterator();
		while (iter.hasNext()) {
			IProject proj = (IProject) iter.next();
			names.add(proj.getName());
		}
		return names;
	}

}
