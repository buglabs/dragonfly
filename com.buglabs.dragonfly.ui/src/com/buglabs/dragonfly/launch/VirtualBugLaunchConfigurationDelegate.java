package com.buglabs.dragonfly.launch;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.util.JarUtils;
import com.buglabs.util.BugBundleConstants;

public class VirtualBugLaunchConfigurationDelegate extends ConciergeLaunchConfiguration {

	public static final String ID = "com.buglabs.dragonfly.launch.virtualBug";
	public static final String ATTR_GPS_LOG = "GPS_LOG";
	public static final String ATTR_IMAGES = "IMAGES";
	public static final String ATTR_HTTP_PORT = "HTTP PORT";
	public static final String SHELL_BUNDLE = "com.buglabs.osgi.shell";
	public static final String CG_SHELL_BUNDLE = "shell.jar";
	public static final String PROP_HTTP_PORT = "org.osgi.service.http.port";
	public static final String PROP_VBUG = "com.buglabs.virtual.bug";
	public static final String PROP_CAMERA_SNAPSHOTS = "com.buglabs.bug.emulator.module.camera.snapshots";
	public static final String PROP_GPS_LOG = "com.buglabs.bug.emulator.module.gps.log";
	public static final String PROP_ACC_LOG = "com.buglabs.bug.emulator.module.accelerometer.log";
	public static final String PROP_CM_STORAGE = "felix.cm.dir";
	public static final String PROP_VBUG_SCROLLSPEED = "com.buglabs.bug.emulator.scrollspeed";
	public static final String PROP_VBUG_SCROLLDELAY = "com.buglabs.bug.emulator.scrolldelay";
	public static final String ATTR_VBUG_SYSTEM_PROPERTIES = "ATTR_VBUG_SYSTEM_PROPERTIES";
	public static final String DEFAULT_START_LEVEL = "4";
	public static final String APP_DIR = "app.bundle.path";
	public static final String JVM_ARGS = "com.buglabs.osgi.concierge.ui.launch.jvmArgs";
	public static final String FELIX_LOG_LEVEL = "felix.log.level";
	public static final String PROP_EXTERNAL_BUNDLE_LAUNCH_LIST = "com.buglabs.bug.emulator.external.bundle.launch.path";
	public static final String PROP_DISCOVERY_MODE = "com.buglabs.bug.discoveryMode";
	public static final String FELIX_CLEAN_STORAGE = "org.osgi.framework.storage.clean";

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		int port = getHttpPort(configuration);
		ILaunchConfigurationType type = configuration.getType();

		try {
			ServerSocket socket = new ServerSocket(port);
			socket.close();
			super.launch(configuration, mode, launch, monitor);
		} catch (IOException e) {
			if (Activator.getDefault().getLaunchErrorVisible())
				MessageDialog.openInformation(new Shell(), "Virtual BUG Launch", "A Virtual BUG is already running. Please close it and launch again.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.buglabs.osgi.concierge.launch.ConciergeLaunchConfiguration#getSystemPropertiesContents(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	protected StringBuffer getSystemPropertiesContents(ILaunchConfiguration configuration) throws CoreException {
		StringBuffer sb = super.getSystemPropertiesContents(configuration);

		Map props = configuration.getAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, new Hashtable());
		sb.append(generateSystemPropertiesContents(props));
	
		return sb;
	}

	/*
	 * (non-Javadoc)
	 * @see com.buglabs.osgi.concierge.launch.ConciergeLaunchConfiguration#getVMArguments()
	 */
	@Override
	protected List getVMArguments() {
		ArrayList vmArgs = new ArrayList();
		vmArgs.addAll(super.getVMArguments());
		return vmArgs;
	}

	/*
	 * (non-Javadoc)
	 * @see com.buglabs.osgi.concierge.launch.ConciergeLaunchConfiguration#getClassPathEntries()
	 */
	@Override
	protected IClasspathEntry[] getClassPathEntries() {
		Vector cpes = new Vector();
		cpes.addAll(Arrays.asList(super.getClassPathEntries()));

		return (IClasspathEntry[]) cpes.toArray(new IClasspathEntry[cpes.size()]);
	}

	@Override
	protected List getBundleJars(ILaunchConfiguration configuration) throws CoreException {
		Vector jars = new Vector();
		Vector cgFilteredJars = new Vector();

		List bugJars = DragonflyActivator.getDefault().getBUGOSGiJars();
		jars.addAll(bugJars);
		return jars;
	}

	/*
	 * (non-Javadoc)
	 * @see com.buglabs.osgi.concierge.launch.ConciergeLaunchConfiguration#getInstallBundles(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	protected List getInstallBundles(ILaunchConfiguration configuration) throws CoreException, IOException, URISyntaxException {
		Vector installBundles = new Vector();

		installBundles.addAll(super.getInstallBundles(configuration));

		List jars = getBundleJars(configuration);
		Iterator iter = jars.iterator();
		while (iter.hasNext()) {
			File jar = (File) iter.next();
			JarFile jarFile = new JarFile(jar);
			String bundleType = JarUtils.getBugBundleType(jarFile);
			if ((bundleType != null && bundleType.equals(BugBundleConstants.BUG_BUNDLE_MODULE))) {
				// installBundles.add(jar.toURL());
			}
		}
		return installBundles;
	}

	/*
	 * (non-Javadoc)
	 * @see com.buglabs.osgi.concierge.launch.ConciergeLaunchConfiguration#getStartLevelMap(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	protected Map getStartLevelMap(ILaunchConfiguration configuration) throws CoreException {
		Map startlevelmap = super.getStartLevelMap(configuration);

		List jars = new ArrayList();
		jars.addAll(DragonflyActivator.getDefault().getBUGOSGiJars());
		Iterator jarIter = jars.iterator();
		while (jarIter.hasNext()) {
			File jar = (File) jarIter.next();

			// TODO: Replace this with regex to look for the shell bundle
			if (jar.getName().startsWith(SHELL_BUNDLE)) {
				startlevelmap.put(jar.getAbsolutePath(), "4");
			} else if (jar.getName().startsWith("com.buglabs.bug.emulator")) {
				startlevelmap.put(jar.getAbsolutePath(), "2");
			} else if (jar.getName().startsWith("com.buglabs.bug.bmi")) {
				startlevelmap.put(jar.getAbsolutePath(), "2");
			} else if (jar.getName().startsWith("com.buglabs.bug.slp")) {
				startlevelmap.put(jar.getAbsolutePath(), "3");
			}
		}

		return startlevelmap;
	}

	/*
	 * (non-Javadoc)
	 * @see com.buglabs.osgi.concierge.launch.ConciergeLaunchConfiguration#getFrameworkStartLevel(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	protected String getFrameworkStartLevel(ILaunchConfiguration configuration) throws CoreException {
		return DEFAULT_START_LEVEL;
	}

	/**
	 * This one called from the parent on launching of virtual bugs says which
	 * projects from the workspace to build. It returns a list of strings of
	 * Project names.
	 * 
	 */
	/*
	
	@Override
	protected List<String> getWorkspaceBundles(ILaunchConfiguration configuration) throws CoreException {
		List selectedProjects = BugProjectUtil.getWSBugProjectNames();
		String launchAll = getSystemProperty(configuration, PROP_LAUNCH_ALL, "true");
		if (!launchAll.equals("true"))
			selectedProjects = configuration.getAttribute(ATTR_LAUNCH_PROJECTS, BugProjectUtil.getWSBugProjectNames());
		return selectedProjects;
	}
	*/

	private static String getSystemProperty(ILaunchConfiguration configuration, String prop, String defaultValue) throws CoreException {

		Map properties = configuration.getAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, new HashMap());
		String val = (String) properties.get(prop);

		if (val != null)
			return val;
		return defaultValue;
	}

	private int getHttpPort(ILaunchConfiguration configuration) throws CoreException {
		String val = getSystemProperty(configuration, PROP_HTTP_PORT, DragonflyActivator.getDefault().getHttpPort());
		return Integer.parseInt((String) val);
	}
}