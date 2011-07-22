package com.buglabs.bug.simulator.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.buglabs.util.osgi.BUGBundleConstants;

/**
 * Utility class for BUG Simulator.
 * 
 * @author akravets
 * 
 */
public class BUGModuleHelper {
	public static String BUG_MODULE = "Module";

	public static String BUNDLE_NAME = "Bundle-Name";

	/**
	 * 
	 * @param context
	 * @return a list of strings containing the ID of each module
	 */
	public static List getModuleNames(BundleContext context) {

		List bundles = getModuleBundles(context);
		Iterator bundlesIter = bundles.iterator();
		List moduleNames = new ArrayList();

		while (bundlesIter.hasNext()) {
			Bundle b = (Bundle) bundlesIter.next();
			moduleNames.add(b.getHeaders().get(BUGBundleConstants.BUG_BUNDLE_MODULE_ID));
		}

		return moduleNames;
	}
	

	/**
	 * 
	 * @param context
	 * @return A list of module bundles
	 */
	private static List getModuleBundles(BundleContext context) {
		Bundle[] bundles = context.getBundles();
		ArrayList modules = new ArrayList();
		for (int i = 0; i < bundles.length; ++i) {
			Bundle b = bundles[i];
			String type = (String) b.getHeaders().get(BUGBundleConstants.BUG_BUNDLE_TYPE_HEADER);

			// TODO determine if it's a list, if so handle appropriately.

			if (type != null) {
				if (type.equals(BUG_MODULE) && !modules.contains(b.getHeaders().get(BUGBundleConstants.BUG_BUNDLE_MODULE_ID))) {
					modules.add(b);
				}
			}
		}

		return modules;
	}

	private static String getBundleName(Bundle b) {
		return (String) b.getHeaders().get(BUNDLE_NAME);
	}

}
