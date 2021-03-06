package com.buglabs.dragonfly.ui.launch;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.launch.BUGSimulatorLaunchConfigurationDelegate;
import com.buglabs.dragonfly.launch.ConciergeLaunchConfiguration;
import com.buglabs.dragonfly.ui.Activator;

/**
 * This class defines the default launch configuration of the BUG Simulator.
 * 
 * @author aroman
 * 
 */
public class SimulatorLaunchConfigurationInitializer {
	public static void initializeSystemProperties(ILaunchConfigurationWorkingCopy wc, boolean discoveryMode) throws CoreException {
		Map properties = wc.getAttribute(ConciergeLaunchConfiguration.SYSTEM_PROPERTIES, new HashMap());
		properties.put(BUGSimulatorLaunchConfigurationDelegate.FELIX_LOG_LEVEL, "3"); //$NON-NLS-1$
		properties.put(BUGSimulatorLaunchConfigurationDelegate.PROP_VBUG, "true"); //$NON-NLS-1$
		
		if (discoveryMode)
			properties.put(BUGSimulatorLaunchConfigurationDelegate.PROP_DISCOVERY_MODE, "true"); //$NON-NLS-1$

		String bundleVersion = (String) Activator.getDefault().getContext().getBundle().getHeaders().get("Bundle-Version"); //$NON-NLS-1$
		String storageDirectory = "bug-configuration"; //$NON-NLS-1$
		if (bundleVersion.indexOf("qualifier") == -1) { //$NON-NLS-1$
			storageDirectory = bundleVersion + File.separator + "bug-configuration"; //$NON-NLS-1$
		}

		String appDirectory = com.buglabs.dragonfly.felix.Activator.getDefault().getStateLocation() + File.separator + "bugSimulator" + File.separator + "apps";

		File f = new File(appDirectory);
		if (!f.exists()) {
			f.mkdirs();
		}

		properties.put(BUGSimulatorLaunchConfigurationDelegate.APP_DIR, appDirectory); //$NON-NLS-1$
		properties.put(BUGSimulatorLaunchConfigurationDelegate.PROP_CM_STORAGE, storageDirectory);
		properties.put(BUGSimulatorLaunchConfigurationDelegate.PROP_VBUG_SCROLLDELAY, "1000"); //$NON-NLS-1$
		properties.put(BUGSimulatorLaunchConfigurationDelegate.PROP_VBUG_SCROLLSPEED, "15"); //$NON-NLS-1$

		wc.setAttribute(ConciergeLaunchConfiguration.SYSTEM_PROPERTIES, properties);

		Map bugprops = wc.getAttribute(BUGSimulatorLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, new HashMap());

		bugprops.put(BUGSimulatorLaunchConfigurationDelegate.PROP_HTTP_PORT, DragonflyActivator.getDefault().getHttpPort());
		bugprops.put(BUGSimulatorLaunchConfigurationDelegate.PROP_CAMERA_SNAPSHOTS, "");
		bugprops.put(BUGSimulatorLaunchConfigurationDelegate.PROP_GPS_LOG, "");
		bugprops.put(BUGSimulatorLaunchConfigurationDelegate.PROP_EXTERNAL_BUNDLE_LAUNCH_LIST, "");

		wc.setAttribute(BUGSimulatorLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, bugprops);
	}
}
