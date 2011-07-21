package com.buglabs.dragonfly;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * Initializes preferences for the SDK. Clients should not call this class, it's
 * being called by the framework
 * 
 * @author akravets
 * 
 */
public class DragonflyPreferenceInitializer extends AbstractPreferenceInitializer {

	public DragonflyPreferenceInitializer() {
		super();
	}

	public void initializeDefaultPreferences() {
		Preferences preferences = DragonflyActivator.getDefault().getPluginPreferences();
		preferences.setDefault(DragonflyActivator.PREF_PROTOCOL, DragonflyActivator.getString("DEFAULT_PROTOCOL"));
		preferences.setDefault(DragonflyActivator.PREF_BUGNET_ENABLED, "true");
		preferences.setDefault(DragonflyActivator.PREF_BUGNET_NUM_OF_APPS, DragonflyActivator.getString("NUM_OF_APPS"));
		preferences.setDefault(DragonflyActivator.PREF_SERVER_NAME, DragonflyActivator.getString("DEFAULT_SERVER_NAME"));
		preferences.setDefault(DragonflyActivator.PREF_BUGNET_USER, "");
		preferences.setDefault(DragonflyActivator.PREF_BUGNET_PWD, "");
		preferences.setDefault(DragonflyActivator.PREF_DEFAULT_BUGPORT, DragonflyActivator.getString("DEFAULT_PORT"));
	}

}
