package com.buglabs.dragonfly.ui.info;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.buglabs.osgi.concierge.ui.info.ProjectInfo;

public class BugProjectInfo extends ProjectInfo {
	boolean shouldGenerateApplicationLoop;

	private Vector services;
	private Hashtable<String, List<ServicePropertyHelper>> service_property_helpers;

	public BugProjectInfo() {
		shouldGenerateApplicationLoop = false;
		services = new Vector();
		service_property_helpers = new Hashtable<String, List<ServicePropertyHelper>>();
	}

	public Vector getServices() {
		return services;
	}

	public Map<String, List<ServicePropertyHelper>> getServicePropertyHelperMap() {
		return service_property_helpers;
	}
	
	public boolean isShouldGenerateApplicationLoop() {
		return shouldGenerateApplicationLoop;
	}

	public void setShouldGenerateApplicationLoop(boolean shouldGenerateApplicationLoop) {
		this.shouldGenerateApplicationLoop = shouldGenerateApplicationLoop;
	}
}
