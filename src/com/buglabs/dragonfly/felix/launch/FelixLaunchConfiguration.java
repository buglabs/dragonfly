package com.buglabs.dragonfly.felix.launch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
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

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			int a = 1;
			String felixPluginBase = Activator.getDefault().getBundle().getLocation().split(":")[2];
			IPath launchDir = Activator.getDefault().getStateLocation();
			String launchClass = "org.apache.felix.main.Main";
			String bootClasspath[] = loadBootClasspath(felixPluginBase);

			VMRunnerConfiguration vmconfig = new VMRunnerConfiguration(launchClass, bootClasspath);
		
			File confFile = createFelixConfFile(configuration, launchDir, felixPluginBase);
			copyBundles(felixPluginBase + "bundle", launchDir, monitor);
			copyBundles(getSourceDir(), launchDir, monitor);
			
			vmconfig.setVMArguments(getVMArgs(confFile, felixPluginBase));
			vmconfig.setWorkingDirectory(launchDir.toOSString());
			
			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
			IVMRunner vmRunner = vmInstall.getVMRunner(mode);
			vmRunner.run(vmconfig, launch, monitor);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to launch BUG Simulator", e));
		} 
	}

	private void copyBundles(String srcDir, IPath launchDir, IProgressMonitor monitor) throws CoreException, URISyntaxException {
		IPath destDir = launchDir.append("bundle");
		
		IFileSystem fs = EFS.getLocalFileSystem();
		
		IFileStore srcStore = fs.getStore(new URI("file://" + srcDir));
		IFileStore destStore = fs.getStore(destDir);

		destStore = destStore.mkdir(EFS.NONE, monitor);
		
		srcStore.copy(destStore, EFS.OVERWRITE | EFS.SHALLOW, monitor);
	}

	private String[] getVMArgs(File confFile, String felixPluginBase) {
		return new String[] { 
				"-Dfelix.config.properties=file://" + confFile.getAbsolutePath(),
				};
	}

	private File createFelixConfFile(ILaunchConfiguration configuration, IPath launchDir, String felixPluginBase) throws IOException {
		String fileContents = 
			"org.osgi.framework.os.name=linux\n" + 
			"org.osgi.framework.processor=armv7l\n" +
			"java.library.path=/usr/lib/jni\n" +
			"felix.auto.deploy.action=install,start\n" +
			"felix.log.level=4\n" +
			"org.osgi.framework.startlevel.beginning=1\n" +
			"org.osgi.console.port=8090\n" +
			"freetype.font=/usr/share/fonts/ttf/LiberationSans-Regular.ttf\n" +
			"app.bundle.path=/usr/share/java/apps\n" +
			"org.osgi.service.http.port=80\n" +
			"obr.repository.url=http://felix.apache.org/obr/releases.xml\n" +
			"bug.os.version=2009.X-stable\n";
		
		fileContents = "felix.auto.deploy.action=install,start\n";
		
		File configFile = new File(launchDir.toOSString(), "config.properties");
		FileWriter fw = new FileWriter(configFile);
		fw.write(fileContents);
		fw.close();
		
		return configFile;
	}

	private String[] loadBootClasspath(String felixPluginBase) throws IOException {
		List cp = new ArrayList();
		
		//Add the Felix OSGi Framework
		cp.add(felixPluginBase + "framework/felix.jar");
	
		return (String[]) cp.toArray(new String[cp.size()]);
	}
	
	/**
	 * @return A directory where OSGi bundles can be found that should be added to the started bundles for Felix.
	 */
	protected abstract String getSourceDir() throws Exception;
}
