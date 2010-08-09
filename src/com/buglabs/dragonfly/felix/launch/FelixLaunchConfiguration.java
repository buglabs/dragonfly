package com.buglabs.dragonfly.felix.launch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.osgi.framework.BundleReference;

import com.buglabs.dragonfly.felix.Activator;

/**
 * An abstract LaunchConfiguration for Apache Felix.  Subclasses specify additional jars and startup properties for launch.  
 * This launch configuration bundles the Felix framework jar and a few other necessary elements to have a vanilla, non interactive
 * OSGi environment.
 * 
 * @author kgilmer
 *
 */
public abstract class FelixLaunchConfiguration extends LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	private static final String FELIX_MAIN_CLASS = "org.apache.felix.main.Main";
	private static final String REL_BUNDLE_DIR = "bundle";
	private static final String FELIX_FRAMEWORK_REL_PATH = "framework" + File.separator + "org.apache.felix.main-3.0.1.jar";
	private IPath launchDir;
	
	public FelixLaunchConfiguration() {
		launchDir = Activator.getDefault().getStateLocation();
	}

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			URL relativeURL = Activator.getDefault().getBundle().getEntry(File.separator);
			URL bundleURL = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry(File.separator + REL_BUNDLE_DIR));
			URL localURL = FileLocator.toFileURL(relativeURL);
			String felixPluginBase = localURL.getPath();
			
			deleteBundleCacheDir(launchDir.append(REL_BUNDLE_DIR), monitor);
			String launchClass = FELIX_MAIN_CLASS;
			String bootClasspath[] = loadBootClasspath(felixPluginBase);

			VMRunnerConfiguration vmconfig = new VMRunnerConfiguration(launchClass, bootClasspath);
		
			File confFile = createFelixConfFile(configuration, launchDir, felixPluginBase, getLaunchProperties());
			
			copyBundles(Path.fromPortableString(bundleURL.getPath()), launchDir, monitor);
			copyBundles(Path.fromPortableString(getSourceDir()), launchDir, monitor);
			exportProjectsAsjars(getWorkspaceBundles(), launchDir.append(REL_BUNDLE_DIR).toFile());
			
			vmconfig.setVMArguments(getVMArgs(confFile, felixPluginBase));
			
			vmconfig.setWorkingDirectory(launchDir.toOSString());
			
			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
			IVMRunner vmRunner = vmInstall.getVMRunner(mode);
			vmRunner.run(vmconfig, launch, monitor);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to launch BUG Simulator", e));
		} 
	}

	/**
	 * Delete the bundle cache directory so that bundles from previous launches do not effect startup.
	 * @param file
	 * @throws IOException 
	 * @throws CoreException 
	 */
	private void deleteBundleCacheDir(IPath dir2, IProgressMonitor monitor) throws IOException, CoreException {		
		IFileSystem fs = EFS.getLocalFileSystem();
		IFileStore target = fs.getStore(dir2);
		target.delete(EFS.NONE, monitor);
	}

	private void copyBundles(IPath srcDir, IPath launchDir, IProgressMonitor monitor) throws CoreException, URISyntaxException {
		IPath destDir = launchDir.append(REL_BUNDLE_DIR);
		
		IFileSystem fs = EFS.getLocalFileSystem();
		
		IFileStore srcStore = fs.getStore(srcDir);
		IFileStore destStore = fs.getStore(destDir);
		destStore = destStore.mkdir(EFS.NONE, monitor);
		IFileStore [] srcStores = srcStore.childStores(EFS.NONE, monitor);

		for (int i = 0; i < srcStores.length; ++i) {
			//For runtime workbench, don't copy svn metadata contained in workspace.
			if (!srcStores[i].getName().endsWith(".svn")) {
				srcStores[i].copy(destStore.getChild(srcStores[i].getName()), EFS.OVERWRITE, monitor);
			}
		}
	}

	private String[] getVMArgs(File confFile, String felixPluginBase) throws MalformedURLException {
		return new String[] { 
				"-Dfelix.config.properties=" + confFile.toURI().toURL().toString(),
				};
	}

	private File createFelixConfFile(ILaunchConfiguration configuration, IPath launchDir, String felixPluginBase, Map<String, String> props) throws IOException {		
		File configFile = new File(launchDir.toOSString(), "config.properties");
		FileWriter fw = new FileWriter(configFile);
		
		props.putAll(getFelixLaunchProperties());
		
		for (Iterator i = props.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String val = props.get(key);
			
			fw.write(key);
			fw.write('=');
			fw.write(val);
			fw.write('\n');
		}
		
		fw.close();
		
		return configFile;
	}

	private Map<String, String> getFelixLaunchProperties() {
		
		Map<String, String> m = new Hashtable();
		
		m.put("felix.auto.deploy.action", "install,start");
		m.put("felix.log.level", "4");
		
		return m;
	}

	private String[] loadBootClasspath(String felixPluginBase) throws IOException {
		List cp = new ArrayList();
		
		//Add the Felix OSGi Framework
		cp.add(felixPluginBase + FELIX_FRAMEWORK_REL_PATH);
	
		return (String[]) cp.toArray(new String[cp.size()]);
	}
	
	private void exportProjectsAsjars(List workspaceBundles, File destinationDirectory) throws CoreException, IOException {
		List cgProjects = ProjectUtils.getWSCGProjects();
		Iterator projIter = cgProjects.iterator();

		while (projIter.hasNext()) {
			IProject proj = (IProject) projIter.next();

			if (proj.isOpen() && workspaceBundles.contains(proj.getName())) {
				ProjectUtils.exporToJar(destinationDirectory, proj, true);
			}
		}
	}
	
	/**
	 * @return The local root directory where the launch data will be stored.  Is writable by clients but can be disposed of across launches.
	 */
	protected IPath getLaunchDirectory() {
		return launchDir;
	}
	
	/**
	 * @return A directory where OSGi bundles can be found that should be added to the started bundles for Felix.
	 */
	protected abstract String getSourceDir() throws Exception;
	
	/**
	 * @return A map of name-value pairs of properties that should be added to the Felix launch configuration.
	 */
	protected abstract Map<String, String> getLaunchProperties();
	
	/**
	 * @return A list of workspace bundles that should be compiled and added to the Felix launch configuration.
	 * @throws Exception
	 */
	protected abstract List<String> getWorkspaceBundles() throws Exception;
}
