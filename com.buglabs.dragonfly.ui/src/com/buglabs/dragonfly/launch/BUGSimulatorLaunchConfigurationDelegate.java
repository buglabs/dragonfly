package com.buglabs.dragonfly.launch;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.buglabs.dragonfly.BugApplicationNature;
import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.felix.launch.FelixLaunchConfiguration;
import com.buglabs.dragonfly.felix.launch.ProjectUtils;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.launch.BugSimulatorMainTab;
import com.buglabs.dragonfly.ui.launch.SystemPropertiesTab;
import com.buglabs.dragonfly.ui.properties.BUGAppPropertyPage;
import com.buglabs.dragonfly.util.UIUtils;

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
	public static final String PROP_LAUNCH_ALL = "com.buglabs.dragonfly.launch.launchAllProjects";
	public static final String PROP_HTTP_PORT = "org.osgi.service.http.port";
	public static final String PROP_HTTP_JETTY_ENABLED = "org.apache.felix.http.jettyEnabled";
	public static final String PROP_LOG_LEVEL = "felix.log.level";
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
	private static final String COMPILE_BUNDLE_TMP_DIR = "workspace_compile_dir";
	private ILaunchConfiguration configuration;
	private boolean hasWorkspaceBundles = false;

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		this.configuration = configuration;
		int port = getHttpPort(configuration);

		try {
			ServerSocket socket = new ServerSocket(port);
			socket.close();
			
			compileBUGProjects();
			
			super.launch(configuration, mode, launch, monitor);
			
			deleteBUGProjects();
			
			new Timer().schedule(new TimerTask() {
				public void run() {
					BugConnectionManager.getInstance()
							.addNewVirtualBugConnection();
				}
			}, 3000);
		} catch (IOException e) {
			if (Activator.getDefault().getLaunchErrorVisible()) {

				Display.getDefault().syncExec(new Runnable() {

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

	/**
	 * Delete temp bundles generated on launch.
	 * @throws IOException
	 */
	private void deleteBUGProjects() throws IOException {
		if (hasWorkspaceBundles) {
			File dir = Activator.getDefault().getStateLocation().append(COMPILE_BUNDLE_TMP_DIR).toFile();
			
			for (File cf: Arrays.asList(dir.listFiles())) {
				if (cf.isFile()) {
					if (!cf.delete()) {
						throw new IOException("Unable to delete " + cf);
					}
				}
			}
		}
	}

	/**
	 * Compile bug projects that will be installed in BS on launch.
	 * @throws IOException
	 * @throws CoreException
	 */
	private void compileBUGProjects() throws IOException, CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		List<IProject> children = new ArrayList<IProject>();
		
		for (IProject project: Arrays.asList(root.getProjects())) {
			try {
				// TODO: Find the nature ID static String
				if (project.isOpen() && project.hasNature(BugApplicationNature.ID)) {
					String prop = project.getPersistentProperty(new QualifiedName("", BUGAppPropertyPage.AUTO_INSTALL_BUGAPP_PROPERTY));
					
					if (prop != null && Boolean.parseBoolean(prop)) {
						children.add(project);
					}
				}
			} catch (CoreException e) {
				// Purposely do nothing
			}
		}
		
		if (children.isEmpty()) {
			return;
		}
		
		File file = Activator.getDefault().getStateLocation().append(COMPILE_BUNDLE_TMP_DIR).toFile();
		
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new IOException("Unable to create directory " + file.getAbsolutePath());
			}
		}
		
		for (IProject project: children) {
			try {
				ProjectUtils.exporToJar(file, project, true, false);
			} catch (CoreException e) {
				UIUtils.handleNonvisualError("Unable to compile project " + project, e);
			}
		}
		
		hasWorkspaceBundles  = true;
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

		//TODO confirm that these properties are what is on BUG20 rootfs.
		m.put("bug.os.version", "2009.X-stable");
		m.put(PROP_HTTP_PORT, "8082");
		m.put(PROP_HTTP_JETTY_ENABLED, "true");
		m.put("org.osgi.framework.storage.clean", "onFirstInit");
		m.put("org.osgi.framework.os.name", "linux");
		m.put("org.osgi.framework.processor", "armv7l");
		m.put(PROP_VBUG, "true");
		
		//This method generates a path should not be valid for Windows File class but actually is.
		//Also the forward slash isn't magically stripped as the backslash is when the property is passed
		//to the felix runtime.
		String s = getLaunchDirectory().toPortableString();
		m.put(APP_DIR, s);
		
		try {
			Object x = configuration.getAttribute(
					BUGSimulatorLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES,
					new HashMap());
			if (x instanceof Map) {
				m.putAll(((Map) x));
			}
			
			x = configuration.getAttribute(
					SystemPropertiesTab.SYSTEM_PROPERTIES_KEY,
					new HashMap());
			if (x instanceof Map) {
				m.putAll(((Map) x));
			}
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		return m;
	}

	@Override
	protected String[] getVMArgs() throws CoreException {
		String s = configuration.getAttribute(VirtualBugLaunchConfigurationDelegate.JVM_ARGS, new String());
		
		if (s == null || s.trim().length() == 0) {
			return new String[0];
		}
		return s.split(" ");
	}

	@Override
	protected List<File> getOtherLaunchBundles() throws Exception {
		String rawList = getSystemProperty(configuration, VirtualBugLaunchConfigurationDelegate.PROP_EXTERNAL_BUNDLE_LAUNCH_LIST,
				"");
		
		List<File> l = new ArrayList<File>();
		
		if (rawList != null && rawList.trim().length() > 0) {
			l = BugSimulatorMainTab.delimitedStringToList(rawList);
		}
		
		return l;
	}

	@Override
	public String getCompiledWorkspaceBundleDir() {
		if (!hasWorkspaceBundles) {
			return null;
		}
		
		File f = Activator.getDefault().getStateLocation().append(COMPILE_BUNDLE_TMP_DIR).toFile();
		
		if (f.exists()) {
			return Activator.getDefault().getStateLocation().append(COMPILE_BUNDLE_TMP_DIR).toFile().toString();
		}
		
		return null;
	}
}
