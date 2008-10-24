package com.buglabs.dragonfly.ui.swt;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.pde.internal.ui.PDEPlugin;

/**
 * This class determine the location of SWT Native Libraries in the eclipse
 * installation. Some of this code is based on SWTLaunchConfiguration from
 * org.eclipse.pde.ui plug-in.
 * 
 * @author Angel Roman
 * 
 */
public class SWTHelper {

	public static List getSWTJars() {
		Location loc = Platform.getInstallLocation();
		String locValue = loc.getURL().getFile();

		File eclipseDir = new File(locValue);
		File pluginsDir = new File(eclipseDir, "plugins");
		File[] swtJars = pluginsDir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if (name.endsWith(".jar") && name.startsWith("org.eclipse.swt")) {
					return true;
				}
				return false;
			}
		});

		if (swtJars != null) {
			return Arrays.asList(swtJars);
		}

		return new ArrayList(0);
	}

	public static String getSWTLocation() {
		StringBuffer locations = new StringBuffer();

		BundleDescription[] fragments = getSWTFragments();

		if (fragments != null) {
			for (int i = 0; i < fragments.length; ++i) {
				String location = getNativeLibraryLocation(fragments[i]);
				if (location != null) {
					locations.append(location);
				}
			}
		}
		return locations.toString();
	}

	public static BundleDescription[] getSWTFragments() {
		IPluginModelBase model = PDECore.getDefault().getModelManager().findModel("org.eclipse.swt");

		if (model != null && model.isEnabled()) {
			BundleDescription desc = model.getBundleDescription();

			if (desc.getContainingState() != null) {
				BundleDescription[] fragments = desc.getFragments();
				if (fragments != null) {
					return fragments;
				}
			}
		}
		return new BundleDescription[0];
	}

	private static String getNativeLibraryLocation(BundleDescription description) {

		File file = new File(description.getLocation());

		if (file.isDirectory()) {
			return description.getLocation();
		} else {
			return getExtractionLocation(file);
		}
	}

	private static String getExtractionLocation(File file) {
		long timestamp = file.lastModified() ^ file.getAbsolutePath().hashCode();
		File metadata = PDEPlugin.getDefault().getStateLocation().toFile();
		File cache = new File(metadata, Long.toString(timestamp) + ".swt"); //$NON-NLS-1$
		if (!cache.exists()) {
			cache.mkdirs();
			extractZipFile(file, cache);
		}
		return cache.getAbsolutePath();
	}

	private static void extractZipFile(File fragment, File destination) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(fragment);
			for (Enumeration zipEntries = zipFile.entries(); zipEntries.hasMoreElements();) {
				ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
				if (zipEntry.isDirectory())
					continue;
				if (isInterestingFile(zipEntry.getName())) {
					InputStream in = null;
					try {
						in = zipFile.getInputStream(zipEntry);
						if (in != null) {
							File file = new File(destination, zipEntry.getName());
							CoreUtility.readFile(in, file);
							if (!Platform.getOS().equals(Constants.OS_WIN32))
								Runtime.getRuntime().exec(new String[] { "chmod", "755", file.getAbsolutePath() }).waitFor(); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} catch (IOException e) {
					} catch (InterruptedException e) {
					} finally {
						try {
							if (in != null)
								in.close();
						} catch (IOException e1) {
						}
					}
				}
			}
		} catch (ZipException e) {
		} catch (IOException e) {
		} finally {
			try {
				if (zipFile != null)
					zipFile.close();
			} catch (IOException e) {
			}
		}
	}

	private static boolean isInterestingFile(String name) {
		Path path = new Path(name);
		if (path.segmentCount() > 1)
			return false;
		return name.endsWith(".dll") //$NON-NLS-1$
				|| name.endsWith(".jnilib") //$NON-NLS-1$
				|| name.endsWith(".sl") //$NON-NLS-1$
				|| name.endsWith(".a") //$NON-NLS-1$
				|| name.indexOf(".so") != -1; //$NON-NLS-1$
	}
}
