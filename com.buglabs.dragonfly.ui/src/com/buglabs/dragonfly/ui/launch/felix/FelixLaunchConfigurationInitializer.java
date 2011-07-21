package com.buglabs.dragonfly.ui.launch.felix;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.launch.ConciergeLaunchConfiguration;
import com.buglabs.dragonfly.launch.VirtualBugLaunchConfigurationDelegate;

/**
 * This class defines the default launch configuration of the BUG Simulator.
 * 
 * @author aroman
 * 
 */
public class FelixLaunchConfigurationInitializer {
	public static void initializeSystemProperties(ILaunchConfigurationWorkingCopy wc, boolean discoveryMode) throws CoreException {
		Map properties = wc.getAttribute(ConciergeLaunchConfiguration.SYSTEM_PROPERTIES, new HashMap());
		properties.put(VirtualBugLaunchConfigurationDelegate.FELIX_LOG_LEVEL, "3"); //$NON-NLS-1$
		
		wc.setAttribute(ConciergeLaunchConfiguration.SYSTEM_PROPERTIES, properties);

		Map bugprops = wc.getAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, new HashMap());

		bugprops.put(VirtualBugLaunchConfigurationDelegate.PROP_HTTP_PORT, DragonflyActivator.getDefault().getHttpPort());
		bugprops.put(VirtualBugLaunchConfigurationDelegate.PROP_EXTERNAL_BUNDLE_LAUNCH_LIST, "");

		wc.setAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, bugprops);
	}
}
