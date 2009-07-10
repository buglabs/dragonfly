/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.print.DocFlavor.SERVICE_FORMATTED;

public class PackageImpl implements IPackage {

	private List serviceDependencies;

	private List eventTopics;

	private String name;

	private String author;

	private String created;

	private String modified;

	private String version = "1";

	private String programType;

	private boolean active;

	private String notes;

	private String id;

	private String bundleType = "";

	private ServiceDetail[] serviceDetails;
	
	public PackageImpl() {
		author = new String();
		created = Calendar.getInstance().getTime().toString();
		modified = new String(created);
		programType = "sal";

		this.name = "unspecified";
		notes = new String();
	}

	public PackageImpl(String name) {
		this();
		this.name = name;
	}

	public PackageImpl(String name, List programInstructions) {
		this();

		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.buglabs.script.runtime.impl.IPackage#getEventTopics()
	 */
	public List getEventURIs() {
		if (eventTopics == null) {
			eventTopics = new ArrayList();
		}

		return eventTopics;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.buglabs.script.runtime.impl.IPackage#setEventTopics(java.util.List)
	 */
	public void setEventURIs(List eventTopics) {
		this.eventTopics = eventTopics;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.buglabs.script.runtime.impl.IPackage#getServiceDependendies()
	 */
	public List getServiceDependendies() {
		if (serviceDependencies == null) {
			serviceDependencies = new ArrayList();
		}

		return serviceDependencies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.buglabs.script.runtime.impl.IPackage#setServiceDependendies(java.util.List)
	 */
	public void setServiceDependendies(List serviceDependendies) {
		this.serviceDependencies = serviceDependendies;
	}

	public String getAuthor() {
		return author;
	}

	public String getCreated() {

		return created;
	}

	public String getModified() {

		return modified;
	}

	public String getName() {

		return name;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPackageVersion() {
		return 1;
	}

	public String getProgramType() {
		return programType;
	}

	public void setProgramType(String type) {
		programType = type;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void updateModified() {
		modified = Calendar.getInstance().getTime().toString();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getId() {
		// Lazy initialization.
		if (id == null) {
			VMID vid = new VMID();
			id = vid.toString();
		}

		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBundleType() {
		return bundleType;
	}

	public void setBundleType(String type) {
		if (type == null) {
			bundleType = "";
		} else {
			bundleType = type;
		}
	}

	public ServiceDetail[] getServiceDetails() {
		return serviceDetails;
	}

	public void setServiceDetails(
			ServiceDetail[] serviceDetails) {
		this.serviceDetails = serviceDetails;
	}
}
