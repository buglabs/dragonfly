package com.buglabs.dragonfly.launch.felix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.buglabs.dragonfly.felix.launch.FelixLaunchConfiguration;
import com.buglabs.dragonfly.felix.launch.ProjectUtils;
import com.buglabs.dragonfly.launch.BUGSimulatorLaunchConfigurationDelegate;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.launch.BugSimulatorMainTab;
import com.buglabs.dragonfly.ui.launch.SystemPropertiesTab;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * A launch configuration for the Apache Felix OSGi framework. Relies on
 * FelixLaunchConfiguration to provide OSGi framework jars.
 * 
 * @author kgilmer
 * 
 */
public class FelixLaunchConfigurationDelegate extends FelixLaunchConfiguration {
	public static final String PROP_LAUNCH_ALL = "com.buglabs.dragonfly.launch.launchAllProjects";
	public static final String PROP_LOG_LEVEL = "felix.log.level";
	public static final String PROP_CM_STORAGE = "felix.cm.dir";
	public static final String DEFAULT_START_LEVEL = "4";
	private static final String COMPILE_BUNDLE_TMP_DIR = "workspace_compile_dir";
	private static final String ATTR_FELIX_SYSTEM_PROPERTIES = "com.buglabs.dragonfly.felix.system.properties";
	private ILaunchConfiguration configuration;
	private boolean hasWorkspaceBundles = false;

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		this.configuration = configuration;

		try {
			buildPDEProjects();

			super.launch(configuration, mode, launch, monitor);

			deleteBUGProjects();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to launch Felix.", e));
		}
	}

	protected IPath getLaunchDir() {
		return Activator.getDefault().getStateLocation().append("felix");
	}

	/**
	 * Delete temp bundles generated on launch.
	 * 
	 * @throws IOException
	 */
	private void deleteBUGProjects() throws IOException {
		if (hasWorkspaceBundles) {
			File dir = Activator.getDefault().getStateLocation().append(COMPILE_BUNDLE_TMP_DIR).toFile();

			for (File cf : Arrays.asList(dir.listFiles())) {
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
	 * 
	 * @throws IOException
	 * @throws CoreException
	 */
	private void buildPDEProjects() throws IOException, CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		List<IProject> children = new ArrayList<IProject>();

		for (IProject project : Arrays.asList(root.getProjects()))
			if (project.isOpen())
				children.add(project);

		if (children.isEmpty()) {
			return;
		}

		File file = Activator.getDefault().getStateLocation().append(COMPILE_BUNDLE_TMP_DIR).toFile();

		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new IOException("Unable to create directory " + file.getAbsolutePath());
			}
		}

		for (IProject project : children) {
			try {
				ProjectUtils.exporToJar(file, project, true, true);
			} catch (CoreException e) {
				UIUtils.handleNonvisualError("Unable to compile project " + project, e);
			}
		}

		hasWorkspaceBundles = true;
	}

	@Override
	protected String getSourceDir() throws Exception {
		return Activator.getDefault().getStateLocation().append(COMPILE_BUNDLE_TMP_DIR).toFile().getAbsolutePath();
	}

	@Override
	protected Map<String, String> getLaunchProperties() {
		Map m = new Hashtable();

		try {
			Object x = configuration.getAttribute(FelixLaunchConfigurationDelegate.ATTR_FELIX_SYSTEM_PROPERTIES, new HashMap());
			if (x instanceof Map) {
				m.putAll(((Map) x));
			}

			x = configuration.getAttribute(SystemPropertiesTab.SYSTEM_PROPERTIES_KEY, new HashMap());
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
		String s = configuration.getAttribute(BUGSimulatorLaunchConfigurationDelegate.JVM_ARGS, new String());

		if (s == null || s.trim().length() == 0) {
			return new String[0];
		}
		return s.split(" ");
	}

	@Override
	protected List<File> getOtherLaunchBundles() throws Exception {
		String rawList = getSystemProperty(configuration, BUGSimulatorLaunchConfigurationDelegate.PROP_EXTERNAL_BUNDLE_LAUNCH_LIST, "");

		List<File> l = new ArrayList<File>();

		if (rawList != null && rawList.trim().length() > 0) {
			l = BugSimulatorMainTab.delimitedStringToList(rawList);
		}

		return l;
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
