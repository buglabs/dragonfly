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

public class Activator implements BundleActivator {

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

	public List getBUGOSGiJars() throws IOException, URISyntaxException {
		List jars = new ArrayList();

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
