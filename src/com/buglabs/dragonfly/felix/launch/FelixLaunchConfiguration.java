package com.buglabs.dragonfly.felix.launch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
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
	private static final String REL_APP_DIR = "apps";
	/**
	 * This value must be changed when the embedded Felix jar is changed.
	 */
	private static final String FELIX_VERSION = "3.2.2";
	private static final String FELIX_FRAMEWORK_REL_PATH = "framework" + File.separator + "org.apache.felix.main-" + FELIX_VERSION + ".jar";
	private IPath launchDir;
	private boolean debug = false;

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			launchDir = getLaunchDir();
			if (!launchDir.toFile().exists())
				if (!launchDir.toFile().mkdirs())
					throw new IOException("Cannot create directory: " + launchDir.toFile());
			
			URL relativeURL = Activator.getDefault().getBundle().getEntry(File.separator);
			URL bundleURL = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry(File.separator + REL_BUNDLE_DIR));
			URL localURL = FileLocator.toFileURL(relativeURL);
			String felixPluginBase = Path.fromPortableString(localURL.getPath()).toOSString();
			
			debugPrint("Felix plugin base: " + felixPluginBase);
			
			deleteBundleCacheDir(launchDir.append(REL_BUNDLE_DIR), monitor);
			String launchClass = FELIX_MAIN_CLASS;
			debugPrint("Felix launch class: " + launchClass);
			String bootClasspath[] = loadBootClasspath(felixPluginBase);
			debugPrint("Felix boot classpath: " + printStrArray(bootClasspath));

			VMRunnerConfiguration vmconfig = new VMRunnerConfiguration(launchClass, bootClasspath);
		
			File confFile = createFelixConfFile(configuration, launchDir, felixPluginBase, getLaunchProperties());
			debugPrint("Felix configuration: " + confFile.toString());
			
			copyBundles(Path.fromPortableString(bundleURL.getPath()), launchDir, REL_BUNDLE_DIR, monitor);
			
			if (getSourceDir() != null)
				copyBundles(Path.fromPortableString(getSourceDir()), launchDir, REL_BUNDLE_DIR, monitor);
			
			if (getCompiledWorkspaceBundleDir() != null) {
				copyBundles(Path.fromPortableString(getCompiledWorkspaceBundleDir()), launchDir, REL_APP_DIR ,monitor);
			}
			for (File extraBundle: getOtherLaunchBundles()) {
				copyBundle(Path.fromPortableString(extraBundle.getAbsolutePath()), launchDir, REL_BUNDLE_DIR, monitor);
			}
			
			String[] args = getVMArgs(confFile, felixPluginBase);
			debugPrint("Felix boot classpath: " + printStrArray(args));
			vmconfig.setVMArguments(args);
			
			debugPrint("Felix workingdir: " + launchDir.toOSString());
			vmconfig.setWorkingDirectory(launchDir.toOSString());
			
			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
			IVMRunner vmRunner = vmInstall.getVMRunner(mode);
			vmRunner.run(vmconfig, launch, monitor);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to launch Felix.", e));
		} 
	}

	protected IPath getLaunchDir() {
		return Activator.getDefault().getStateLocation().append("bugSimulator");
	}

	/**
	 * Concrete subclasses need to return location of compiled bundles from workspace, or null if none exist.
	 * @return
	 */
	public abstract String getCompiledWorkspaceBundleDir();

	/**
	 * Return array as a string.
	 * @param array
	 * @return
	 */
	private String printStrArray(String [] array) {
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < array.length; ++i) {
			sb.append(array[i]);
			sb.append('\n');
		}
		
		return sb.toString();
	}

	/**
	 * If debug enabled, print output to sysout.
	 * @param s
	 */
	private void debugPrint(String s) {
		if (debug) {
			System.out.println(s);
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

	/**
	 * Copy files from source to launchDir + childPath
	 * @param srcDir
	 * @param launchDir
	 * @param childPath
	 * @param monitor
	 * @throws CoreException
	 * @throws URISyntaxException
	 */
	private void copyBundles(IPath srcDir, IPath launchDir, String childPath, IProgressMonitor monitor) throws CoreException, URISyntaxException {
		if (srcDir == null || launchDir == null) {
			return;
		}
		
		IPath destDir = launchDir.append(childPath);
		
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
	
	/**
	 * Copy file from srcBundle to launchDir + childDir
	 * @param srcBundle
	 * @param launchDir
	 * @param childDir
	 * @param monitor
	 * @throws CoreException
	 * @throws URISyntaxException
	 */
	private void copyBundle(IPath srcBundle, IPath launchDir, String childDir, IProgressMonitor monitor) throws CoreException, URISyntaxException {
		IPath destDir = launchDir.append(childDir);
		
		IFileSystem fs = EFS.getLocalFileSystem();
		
		IFileStore srcStore = fs.getStore(srcBundle);
		IFileStore destStore = fs.getStore(destDir);
		destStore = destStore.mkdir(EFS.NONE, monitor);	

		srcStore.copy(destStore.getChild(srcStore.getName()), EFS.OVERWRITE, monitor);
	}

	/**
	 * Get the VM args as specified in the launch UI.
	 * @param confFile
	 * @param felixPluginBase
	 * @return
	 * @throws MalformedURLException
	 * @throws CoreException
	 */
	private String[] getVMArgs(File confFile, String felixPluginBase) throws MalformedURLException, CoreException {
		String [] cs = getVMArgs();
		
		List l = Arrays.asList(cs);
		List l2 = new ArrayList(l);
		String s = confFile.toURI().toURL().toString();
		
		l2.add("-Dfelix.config.properties=" + s);
		
		return (String[]) l2.toArray(new String[l.size()]);
	}
	
	/**
	 * @return
	 * @throws CoreException
	 */
	protected abstract String[] getVMArgs() throws CoreException;

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
	 * @return A list of other bundles that should be added to the Felix launch configuration.  Client should return empty list, not null if no bundles exist.
	 * @throws Exception
	 */
	protected abstract List<File> getOtherLaunchBundles() throws Exception;
}
