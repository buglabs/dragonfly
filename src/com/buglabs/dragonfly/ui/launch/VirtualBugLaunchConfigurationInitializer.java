package com.buglabs.dragonfly.ui.launch;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.launch.VirtualBugLaunchConfigurationDelegate;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.osgi.concierge.launch.ConciergeLaunchConfiguration;
import com.buglabs.osgi.concierge.runtime.ConciergeRuntime;

/**
 * This class defines the default launch configuration of the BUG Simulator.
 * 
 * @author aroman
 * 
 */
public class VirtualBugLaunchConfigurationInitializer {
	public static void initializeSystemProperties(ILaunchConfigurationWorkingCopy wc) throws CoreException {
		Map properties = wc.getAttribute(ConciergeLaunchConfiguration.SYSTEM_PROPERTIES, new HashMap());
		properties.put(VirtualBugLaunchConfigurationDelegate.PROP_SERVICE_LISTENER_CHECK, "true"); //$NON-NLS-1$
		properties.put(VirtualBugLaunchConfigurationDelegate.PROP_LOG_ENABLED, "true"); //$NON-NLS-1$
		properties.put(VirtualBugLaunchConfigurationDelegate.PROP_LOG_LEVEL, "4"); //$NON-NLS-1$
		properties.put(VirtualBugLaunchConfigurationDelegate.PROP_VBUG, "true"); //$NON-NLS-1$

		String bundleVersion = (String) Activator.getDefault().getContext().getBundle().getHeaders().get("Bundle-Version"); //$NON-NLS-1$
		String storageDirectory = "bug-configuration"; //$NON-NLS-1$
		if (bundleVersion.indexOf("qualifier") == -1) { //$NON-NLS-1$
			storageDirectory = bundleVersion + File.separator + "bug-configuration"; //$NON-NLS-1$
		}

		String appDirectory = ConciergeRuntime.getDefault().getStateLocation() + File.separator + "apps";
		File f = new File(appDirectory);
		if (!f.exists()) {
			f.mkdirs();
		}

		properties.put(VirtualBugLaunchConfigurationDelegate.APP_DIR, appDirectory); //$NON-NLS-1$
		properties.put(VirtualBugLaunchConfigurationDelegate.PROP_CM_STORAGE, storageDirectory);
		properties.put(VirtualBugLaunchConfigurationDelegate.PROP_VBUG_SCROLLDELAY, "1000"); //$NON-NLS-1$
		properties.put(VirtualBugLaunchConfigurationDelegate.PROP_VBUG_SCROLLSPEED, "15"); //$NON-NLS-1$

		wc.setAttribute(ConciergeLaunchConfiguration.SYSTEM_PROPERTIES, properties);

		Map bugprops = wc.getAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, new HashMap());

		bugprops.put(VirtualBugLaunchConfigurationDelegate.PROP_HTTP_PORT, DragonflyActivator.getDefault().getHttpPort());
		bugprops.put(VirtualBugLaunchConfigurationDelegate.PROP_CAMERA_SNAPSHOTS, "");
		bugprops.put(VirtualBugLaunchConfigurationDelegate.PROP_GPS_LOG, "");
		bugprops.put(VirtualBugLaunchConfigurationDelegate.PROP_LAUNCH_ALL, "true");

		wc.setAttribute(VirtualBugLaunchConfigurationDelegate.ATTR_VBUG_SYSTEM_PROPERTIES, bugprops);
	}
}
