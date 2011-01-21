package com.buglabs.dragonfly.model;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class BugProjectInfo {
	private String projectName;
	private String version;
	private String activator;
	private String vendor;
	private String symbolicName;
	private String executionEnvironment;
	private boolean generateActivator;
	boolean generateSeparateAppClass;

	private Vector services;
	private Hashtable<String, List<ServicePropertyHelper>> service_property_helpers;
	private String bundleDescription;
	private boolean generateLogMethod;
	private boolean generateDebugStatements;

	public BugProjectInfo() {
		version = "1.0.0";
		vendor = "";
		symbolicName = "";
		activator = "";
		projectName = "";
		generateActivator = true;
		executionEnvironment = "";
		
		generateSeparateAppClass = false;
		services = new Vector();
		service_property_helpers = new Hashtable<String, List<ServicePropertyHelper>>();
	}

	public Vector getServices() {
		return services;
	}

	public Map<String, List<ServicePropertyHelper>> getServicePropertyHelperMap() {
		return service_property_helpers;
	}

	public boolean isGenerateSeparateApplicationClass() {
		return generateSeparateAppClass;
	}

	public void setGenerateSeparateApplicationClass(boolean shouldGenerateApplicationLoop) {
		this.generateSeparateAppClass = shouldGenerateApplicationLoop;
	}
	

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getActivator() {
		return activator;
	}

	public void setActivator(String activator) {
		this.activator = activator;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isGenerateActivator() {
		return generateActivator;
	}

	public void setGenerateActivator(boolean generateActivator) {
		this.generateActivator = generateActivator;
	}

	public String getActivatorName() {
		if (getActivator().equals("")) {
			return "";
		}

		String name = getActivator();
		int index = getActivator().lastIndexOf(".");

		if (index > 0 && index < name.length()) {
			name = name.substring(index + 1, name.length());
		}

		return name;
	}

	public String getActivatorPackage() {
		if (getActivator().equals("")) {
			return "";
		}

		String name = getActivator();

		int index = name.lastIndexOf(".");
		if (index > 0) {
			name = name.substring(0, index);
		}

		return name.toLowerCase();
	}

	public String getExecutionEnvironment() {
		return executionEnvironment;
	}

	public void setExecutionEnvironment(String executionEnvironment) {
		this.executionEnvironment = executionEnvironment;
	}

	public void setDescription(String text) {
		this.bundleDescription = text;
	}
	
	public String getDescription() {
		return bundleDescription;
	}

	public void setGenerateLogMethod(boolean selection) {
		this.generateLogMethod = selection;
	}
	
	public boolean getGenerateLogMethod() {
		return generateLogMethod;
	}

	public void setGenerateDebugStatements(boolean selection) {
		this.generateDebugStatements = selection;	
	}
	
	public boolean getGenerateDebugStatements() {
		return generateDebugStatements;
	}
}
