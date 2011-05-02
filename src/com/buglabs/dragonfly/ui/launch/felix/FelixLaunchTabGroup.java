package com.buglabs.dragonfly.ui.launch.felix;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

import com.buglabs.dragonfly.ui.launch.SystemPropertiesTab;

public class FelixLaunchTabGroup extends AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] { new SystemPropertiesTab() };
		setTabs(tabs);
	}
}
