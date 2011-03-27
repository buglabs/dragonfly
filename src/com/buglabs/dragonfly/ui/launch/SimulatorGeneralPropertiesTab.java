package com.buglabs.dragonfly.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.buglabs.dragonfly.util.UIUtils;

public class SimulatorGeneralPropertiesTab extends SystemPropertiesTab {
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		try {
			SimulatorLaunchConfigurationInitializer.initializeSystemProperties(configuration, false);
		} catch (CoreException e) {
			UIUtils.handleVisualError("Failed to initalize tab.", e);
		}
	}
}
