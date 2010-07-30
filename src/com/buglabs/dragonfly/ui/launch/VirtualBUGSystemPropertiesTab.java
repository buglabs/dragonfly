package com.buglabs.dragonfly.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public class VirtualBUGSystemPropertiesTab extends SystemPropertiesTab {
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		try {
			VirtualBugLaunchConfigurationInitializer.initializeSystemProperties(configuration);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
