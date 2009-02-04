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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.swt.SWTHelper;
import com.buglabs.dragonfly.ui.util.BugProjectUtil;
import com.buglabs.dragonfly.util.JarUtils;
import com.buglabs.osgi.concierge.launch.ConciergeLaunchConfiguration;
import com.buglabs.osgi.concierge.runtime.ConciergeRuntime;
import com.buglabs.util.BugBundleConstants;

public class VirtualBugLaunchConfigurationDelegate extends ConciergeLaunchConfiguration {

	public static final String ID = "com.buglabs.dragonfly.launch.virtualBug";

	public static final String ATTR_GPS_LOG = "GPS_LOG";

	public static final String ATTR_IMAGES = "IMAGES";

	public static final String ATTR_HTTP_PORT = "HTTP PORT";

	public static final String ATTR_PROJECTS = "Bug Projects";

	public static final String SHELL_BUNDLE = "com.buglabs.osgi.shell";

	public static final String CG_SHELL_BUNDLE = "shell.jar";

	public static final String PROP_HTTP_PORT = "org.osgi.service.http.port";
	public static final String PROP_SERVICE_LISTENER_CHECK = "ch.ethz.iks.concierge.deepServiceListenerCheck";
	public static final String PROP_LOG_ENABLED = "ch.ethz.iks.concierge.log.enabled";
	public static final String PROP_LOG_LEVEL = "ch.ethz.iks.concierge.log.level";
	public static final String PROP_VBUG = "com.buglabs.virtual.bug";
	public static final String PROP_CAMERA_SNAPSHOTS = "com.buglabs.bug.emulator.module.camera.snapshots";
	public static final String PROP_GPS_LOG = "com.buglabs.bug.emulator.module.gps.log";
	public static final String PROP_ACC_LOG = "com.buglabs.bug.emulator.module.accelerometer.log";
	public static final String PROP_SLP_MULTICAST_JOIN = "net.slp.multicastJoin";
	public static final String PROP_CM_STORAGE = "com.buglabs.osgi.cm.storage";
	public static final String PROP_VBUG_SCROLLSPEED = "com.buglabs.bug.emulator.scrollspeed";
	public static final String PROP_VBUG_SCROLLDELAY = "com.buglabs.bug.emulator.scrolldelay";

	public static final String ATTR_VBUG_SYSTEM_PROPERTIES = "ATTR_VBUG_SYSTEM_PROPERTIES";

	public void launch(ILaunchConfiguration configuration, String mode, 
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		int port = getHttpPort(configuration);
		ILaunchConfigurationType type = configuration.getType();

		try {
			ServerSocket socket = new ServerSocket(port);
			socket.close();
			super.launch(configuration, mode, launch, monitor);
		} catch (IOException e) {
			if (Activator.getDefault().getLaunchErrorVisible())
				MessageDialog.openInformation(new Shell(), "Virtual BUG Launch",
				"A Virtual BUG is already running. Please close it and launch again.");
		}
	}

	public static String getSystemProperty(
			ILaunchConfiguration configuration, 
			String prop, String defaultValue) throws CoreException {
		
		Map properties = configuration.getAttribute(
				VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, new HashMap());
		String val = (String) properties.get(prop);

		if(val != null) {
			return val;
		}

		return defaultValue;
	}

	private int getHttpPort(ILaunchConfiguration configuration) throws CoreException {

		String val = getSystemProperty(configuration, PROP_HTTP_PORT, DragonflyActivator.getDefault().getHttpPort());

		return Integer.parseInt((String) val);
	}

	protected StringBuffer getSystemPropertiesContents(ILaunchConfiguration configuration) throws CoreException {
		
		StringBuffer sb = super.getSystemPropertiesContents(configuration);
		
		Map props = configuration.getAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, new Hashtable());
		sb.append(generateSystemPropertiesContents(props));

		return sb;
	}

	protected List getVMArguments() {
		ArrayList vmArgs = new ArrayList();
		vmArgs.addAll(super.getVMArguments());
		return vmArgs;
	}

	protected IClasspathEntry[] getClassPathEntries() {
		Vector cpes = new Vector();
		cpes.addAll(Arrays.asList(super.getClassPathEntries()));

		return (IClasspathEntry[]) cpes.toArray(new IClasspathEntry[cpes.size()]);
	}

	private List getSWTClasspathEntries() {
		List swtJars = SWTHelper.getSWTJars();
		List cpes = new ArrayList();
		Iterator jarsIter = swtJars.iterator();
		while (jarsIter.hasNext()) {
			File jar = (File) jarsIter.next();
			cpes.add(JavaCore.newLibraryEntry(new Path(jar.getAbsolutePath()), new Path(jar.getAbsolutePath()), null));
		}
		return cpes;
	}

	protected List getBundleJars(ILaunchConfiguration configuration) throws CoreException {
		Vector jars = new Vector();
		Vector cgFilteredJars = new Vector();

		List cgJars = ConciergeRuntime.getDefault().getConciergeJars();
		Iterator cgIter = cgJars.iterator();

		while (cgIter.hasNext()) {
			File cgjar = (File) cgIter.next();
			if (!cgjar.getName().endsWith(CG_SHELL_BUNDLE)) {
				// Remove cg shell
				cgFilteredJars.add(cgjar);
			}
		}

		jars.addAll(cgFilteredJars);

		List bugJars = DragonflyActivator.getDefault().getBugKernelJars();
		jars.addAll(bugJars);
		return jars;
	}

	/**
	 * This method removes bundles that should not be started in the Virtual Bug
	 * Service Discovery mode.
	 * 
	 * @param bugJars
	 */
	private void removeNonServiceDiscoveryBundles(List bugJars) {
		List itemsToRemove = new ArrayList();

		for (Iterator i = bugJars.iterator(); i.hasNext();) {
			File bundle = (File) i.next();

			if (bundle.getName().indexOf("com.buglabs.bug.slp") > -1) {
				itemsToRemove.add(bundle);
			} else if (bundle.getName().indexOf("ch.ethz.iks.slp") > -1) {
				itemsToRemove.add(bundle);
			} else if (bundle.getName().indexOf("com.buglabs.bug.event") > -1) {
				itemsToRemove.add(bundle);
			} else if (bundle.getName().indexOf("com.buglabs.osgi.shell") > -1) {
				itemsToRemove.add(bundle);
			} else if (bundle.getName().indexOf("com.buglabs.bug.emulator.jar") > -1) {
				itemsToRemove.add(bundle);
			}
		}

		for (Iterator i = itemsToRemove.iterator(); i.hasNext();) {
			bugJars.remove(i.next());
		}

	}

	protected List getInstallBundles(ILaunchConfiguration configuration) throws CoreException, IOException, URISyntaxException {
		Vector installBundles = new Vector();

		installBundles.addAll(super.getInstallBundles(configuration));

		List jars = getBundleJars(configuration);
		Iterator iter = jars.iterator();
		while (iter.hasNext()) {
			File jar = (File) iter.next();
			JarFile jarFile = new JarFile(jar);
			String bundleType = JarUtils.getBugBundleType(jarFile);
			if ((bundleType != null && bundleType
					.equals(BugBundleConstants.BUG_BUNDLE_MODULE))) {
				// installBundles.add(jar.toURL());
			}
		}
		return installBundles;
	}

	protected Map getStartLevelMap(ILaunchConfiguration configuration) throws CoreException {
		Map startlevelmap = super.getStartLevelMap(configuration);

		List jars = ConciergeRuntime.getDefault().getConciergeJars();
		jars.addAll(DragonflyActivator.getDefault().getBugKernelJars());
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

	protected String getFrameworkStartLevel(ILaunchConfiguration configuration) throws CoreException {
		return "4";
	}

	/**
	 * Returns the a list of names of BugProjects
	 */
	protected List getWorkspaceBundles(ILaunchConfiguration configuration) throws CoreException {
		List wsBundles = getProjectNames(configuration);
		return wsBundles;
	}

	private List getProjects(ILaunchConfiguration configuration) {
		ArrayList projs = new ArrayList();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		Iterator iter = getProjectNames(configuration).iterator();
		while (iter.hasNext()) {
			String projName = (String) iter.next();
			IProject proj = root.getProject(projName);
			if (proj != null && proj.isOpen()) {
				projs.add(proj);
			}
		}

		return projs;
	}

	private List getJarNames(List jars) {
		Vector names = new Vector();
		Iterator jarsIter = jars.iterator();
		while (jarsIter.hasNext()) {
			File jarFile = (File) jarsIter.next();
			String jarName = jarFile.getName().substring(0, jarFile.getName().length() - 4);
			names.add(jarName);
		}
		return names;
	}

	private List getProjectNames(ILaunchConfiguration configuration) {
		return BugProjectUtil.getProjectNames(BugProjectUtil.getWSBugProjects());
	}
}
