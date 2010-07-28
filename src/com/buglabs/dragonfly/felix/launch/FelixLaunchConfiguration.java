package com.buglabs.dragonfly.felix.launch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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

public abstract class FelixLaunchConfiguration extends LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			String felixPluginBase = Activator.getDefault().getBundle().getLocation().split(":")[2];
			IPath launchDir = Activator.getDefault().getStateLocation();
			deleteBundleCacheDir(launchDir.append("bundle"), monitor);
			String launchClass = "org.apache.felix.main.Main";
			String bootClasspath[] = loadBootClasspath(felixPluginBase);

			VMRunnerConfiguration vmconfig = new VMRunnerConfiguration(launchClass, bootClasspath);
		
			File confFile = createFelixConfFile(configuration, launchDir, felixPluginBase, getLaunchProperties());
			
			copyBundles(felixPluginBase + "bundle", launchDir, monitor);
			copyBundles(getSourceDir(), launchDir, monitor);
			exportProjectsAsjars(getWorkspaceBundles(), launchDir.append("bundle").toFile());
			
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

	private void copyBundles(String srcDir, IPath launchDir, IProgressMonitor monitor) throws CoreException, URISyntaxException {
		System.out.println("srcdir: " + srcDir);
		System.out.println("dstdir: " + launchDir.toOSString());
		
		IPath destDir = launchDir.append("bundle");
		
		IFileSystem fs = EFS.getLocalFileSystem();
		
		IFileStore srcStore = fs.getStore(new URI("file://" + srcDir));
		IFileStore destStore = fs.getStore(destDir);

		destStore = destStore.mkdir(EFS.NONE, monitor);
		
		srcStore.copy(destStore, EFS.OVERWRITE, monitor);
	}

	private String[] getVMArgs(File confFile, String felixPluginBase) {
		return new String[] { 
				"-Dfelix.config.properties=file://" + confFile.getAbsolutePath(),
				};
	}

	private File createFelixConfFile(ILaunchConfiguration configuration, IPath launchDir, String felixPluginBase, Map<String, String> props) throws IOException {
		
		
		String fileContents = "felix.auto.deploy.action=install,start\n";
		
		File configFile = new File(launchDir.toOSString(), "config.properties");
		FileWriter fw = new FileWriter(configFile);
		fw.write(fileContents);
		
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
		
		m.put("org.osgi.framework.os.name", "linux");
		m.put("org.osgi.framework.processor", "armv7l");
		m.put("felix.log.level", "4");
		m.put("obr.repository.url","http://felix.apache.org/obr/releases.xml");
		
		return m;
	}

	private String[] loadBootClasspath(String felixPluginBase) throws IOException {
		List cp = new ArrayList();
		
		//Add the Felix OSGi Framework
		cp.add(felixPluginBase + "framework/felix.jar");
	
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
	 * @return A directory where OSGi bundles can be found that should be added to the started bundles for Felix.
	 */
	protected abstract String getSourceDir() throws Exception;
	
	/**
	 * @return A map of name-value pairs of properties that should be added to the Felix launch configuration.
	 */
	protected abstract Map<String, String> getLaunchProperties();
	
	protected abstract List<String> getWorkspaceBundles() throws Exception;
}
