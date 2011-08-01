package com.buglabs.dragonfly.simulator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activator for simulator.
 *
 */
public class Activator implements BundleActivator {

	/**
	 * Bundles that should be started when the simulator starts.
	 */
	private static final String[] STARTABLE_BUNDLES = {
				"com.buglabs.app.bugdash2.jar",
				"com.buglabs.bug.dragonfly.jar",
				"com.buglabs.bug.simulator.jar",
				"com.buglabs.bug.ws.jar",
				"com.buglabs.osgi.sewing.jar",
				"com.buglabs.util.shell.jar"};
	
	private static BundleContext context;
	private static Activator ref;

	public Activator() {
		ref = this;
	}

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		resetBundlePermissions(getBUGBundleLocation());
	}

	/**
	 * Set the exec permission on startable BUG bundles.  This would preferably be done in the build but the Eclipse PDE build system does not
	 * allow for setting permissions on files due to a limitation with Zip.
	 * 
	 * @param bundleDir
	 * @throws IOException
	 */
	private void resetBundlePermissions(String bundleDir) throws IOException {
		File dir = new File(bundleDir);
		
		if (!dir.exists() || dir.isFile())
			throw new IOException("Invalid bundle directory: " + bundleDir);
		
		List<String> execBundles = Arrays.asList(STARTABLE_BUNDLES);
		
		for (String name : execBundles) {
			File bf = new File(dir, name);
			
			if (!bf.exists() || !bf.isFile())
				throw new IOException("Unable to find " + name);
			
			bf.setExecutable(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	public static Activator getDefault() {
		return ref;
	}

	public String getBUGBundleLocation() throws IOException, URISyntaxException {
		return getFileSystemLocation(File.separator + "bundles");
	}

	/**
	 * Returns the absolute file system path pertaining to the relative bundle
	 * path.
	 * 
	 * @param path
	 *            a bundle relative path of the filesystem resource.
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private String getFileSystemLocation(String path) throws IOException, URISyntaxException {
		URL locURL = context.getBundle().getEntry(path);
		URL fileURL = FileLocator.toFileURL(locURL);
		File locDir = new File(fileURL.getPath());
		return locDir.getAbsolutePath();
	}

	public List<File> getBUGOSGiJars() throws IOException, URISyntaxException {
		List<File> jars = new ArrayList<File>();

		String loc = "";

		loc = getBUGBundleLocation();

		if (!loc.equals("")) {
			File bugKernelLoc = new File(loc);

			if (bugKernelLoc.exists()) {

				File[] libraries = bugKernelLoc.listFiles(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						if (name.endsWith(".jar") && !name.startsWith("org.eclipse.swt")) {
							return true;
						}
						return false;
					}
				});

				jars.addAll(Arrays.asList(libraries));
			}
		}

		return jars;
	}

}
