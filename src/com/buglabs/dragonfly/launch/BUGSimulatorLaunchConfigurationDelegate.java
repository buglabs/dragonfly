package com.buglabs.dragonfly.launch;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.felix.launch.FelixLaunchConfiguration;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.util.BugProjectUtil;

/**
 * A launch configuration for BUG Simulator. Relies on FelixLaunchConfiguration
 * to provide OSGi framework jars.
 * 
 * @author kgilmer
 * 
 */
public class BUGSimulatorLaunchConfigurationDelegate extends
		FelixLaunchConfiguration {
	public static final String ATTR_GPS_LOG = "GPS_LOG";
	public static final String ATTR_IMAGES = "IMAGES";
	public static final String ATTR_HTTP_PORT = "HTTP PORT";
	public static final String ATTR_LAUNCH_PROJECTS = "Bug Projects to Launch";
	public static final String SHELL_BUNDLE = "com.buglabs.osgi.shell";
	public static final String CG_SHELL_BUNDLE = "shell.jar";
	public static final String PROP_LAUNCH_ALL = "com.buglabs.dragonfly.launch.launchAllProjects";
	public static final String PROP_HTTP_PORT = "org.osgi.service.http.port";
	public static final String PROP_LOG_LEVEL = "felix.log.level";
	public static final String PROP_VBUG = "com.buglabs.virtual.bug";
	public static final String PROP_CAMERA_SNAPSHOTS = "com.buglabs.bug.emulator.module.camera.snapshots";
	public static final String PROP_GPS_LOG = "com.buglabs.bug.emulator.module.gps.log";
	public static final String PROP_ACC_LOG = "com.buglabs.bug.emulator.module.accelerometer.log";
	public static final String PROP_CM_STORAGE = "com.buglabs.osgi.cm.storage";
	public static final String PROP_VBUG_SCROLLSPEED = "com.buglabs.bug.emulator.scrollspeed";
	public static final String PROP_VBUG_SCROLLDELAY = "com.buglabs.bug.emulator.scrolldelay";
	public static final String ATTR_VBUG_SYSTEM_PROPERTIES = "ATTR_VBUG_SYSTEM_PROPERTIES";
	public static final String DEFAULT_START_LEVEL = "4";
	public static final String APP_DIR = "app.bundle.path";
	private ILaunchConfiguration configuration;

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		this.configuration = configuration;
		int port = getHttpPort(configuration);

		try {
			ServerSocket socket = new ServerSocket(port);
			socket.close();
			super.launch(configuration, mode, launch, monitor);
			new Timer().schedule(new TimerTask() {
				public void run() {
					BugConnectionManager.getInstance()
							.addNewVirtualBugConnection();
				}
			}, 3000);
		} catch (IOException e) {
			if (Activator.getDefault().getLaunchErrorVisible()) {

				Display.getCurrent().syncExec(new Runnable() {

					public void run() {
						MessageDialog
								.openInformation(new Shell(),
										"BUG Simulator Launch",
										"BUG Simulator is already running. Please close it and launch again.");
					}
				});
			}
		}
	}

	private static String getSystemProperty(ILaunchConfiguration configuration,
			String prop, String defaultValue) throws CoreException {

		Map properties = configuration
				.getAttribute(
						BUGSimulatorLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES,
						new HashMap());
		String val = (String) properties.get(prop);

		if (val != null)
			return val;
		return defaultValue;
	}

	private int getHttpPort(ILaunchConfiguration configuration)
			throws CoreException {
		String val = getSystemProperty(configuration, PROP_HTTP_PORT,
				DragonflyActivator.getDefault().getHttpPort());
		return Integer.parseInt((String) val);
	}

	@Override
	protected String getSourceDir() throws Exception {
		return com.buglabs.dragonfly.simulator.Activator.getDefault()
				.getBUGBundleLocation();
	}

	@Override
	protected Map<String, String> getLaunchProperties() {
		Map m = new Hashtable();

		m.put("bug.os.version", "2009.X-stable");
		m.put("org.osgi.service.http.port", "8082");
		m.put("org.osgi.framework.storage.clean", "onFirstInit");
		m.put("org.osgi.framework.os.name", "linux");
		m.put("org.osgi.framework.processor", "armv7l");
		m.put(PROP_VBUG, "true");
		
		//This method generates a path should not be valid for Windows File class but actually is.
		//Also the forward slash isn't magically stripped as the backslash is when the property is passed
		//to the felix runtime.
		String s = getLaunchDirectory().toPortableString();
		m.put(APP_DIR, s);

		return m;
	}

	@Override
	protected List<String> getWorkspaceBundles() throws CoreException {
		List selectedProjects = BugProjectUtil.getWSBugProjectNames();
		String launchAll = getSystemProperty(configuration, PROP_LAUNCH_ALL,
				"true");
		if (!launchAll.equals("true"))
			selectedProjects = configuration.getAttribute(ATTR_LAUNCH_PROJECTS,
					BugProjectUtil.getWSBugProjectNames());
		return selectedProjects;
	}
}
