package com.buglabs.dragonfly.ui.views.bugnet;

import java.util.ArrayList;
import java.util.List;

import com.buglabs.dragonfly.model.BUGNetProgramReferenceNode;

public class BugnetApplicationList {
	private List<BUGNetProgramReferenceNode> applications;

	private static final String NO_APPS_DEFAULT_MESSAGE = "No applications found";
	private String noAppsMsg = NO_APPS_DEFAULT_MESSAGE;

	public BugnetApplicationList() {
		initApplicationList();
	}

	public void setNoAppsMessage(String message) {
		noAppsMsg = message;
	}

	public String getNoAppsMessage() {
		return noAppsMsg;
	}

	public void initApplicationList() {
		applications = new ArrayList<BUGNetProgramReferenceNode>();
		noAppsMsg = NO_APPS_DEFAULT_MESSAGE;
	}

	public void setApplications(List<BUGNetProgramReferenceNode> applications) {
		this.applications = applications;
	}

	public void addApplications(List<BUGNetProgramReferenceNode> applications) {
		this.applications.addAll(applications);
	}

	public List<BUGNetProgramReferenceNode> getApplications() {
		return applications;
	}

	public int size() {
		if (applications == null)
			return 0;
		return applications.size();
	}

}
