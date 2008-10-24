package com.buglabs.dragonfly.ui.info;

import java.util.Vector;

import com.buglabs.osgi.concierge.ui.info.ProjectInfo;

public class BugProjectInfo extends ProjectInfo {
	boolean shouldGenerateApplicationLoop;

	Vector services;

	public BugProjectInfo() {
		shouldGenerateApplicationLoop = false;
		services = new Vector();
	}

	public Vector getServices() {
		return services;
	}

	public boolean isShouldGenerateApplicationLoop() {
		return shouldGenerateApplicationLoop;
	}

	public void setShouldGenerateApplicationLoop(boolean shouldGenerateApplicationLoop) {
		this.shouldGenerateApplicationLoop = shouldGenerateApplicationLoop;
	}
}
