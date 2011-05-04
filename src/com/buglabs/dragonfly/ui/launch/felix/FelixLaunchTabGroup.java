package com.buglabs.dragonfly.ui.launch.felix;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Determines the tabs that are present on the Felix launch configuration
 * 
 * @author kgilmer
 *
 */
public class FelixLaunchTabGroup extends AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] { new FelixLaunchBundleTab(), new FelixLaunchPropertiesTab() };
		setTabs(tabs);
	}
}
