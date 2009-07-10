/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.util.List;
import java.util.Map;

public interface IPackage {

	public abstract List getEventURIs();

	public abstract void setEventURIs(List eventTopics);

	public abstract List getServiceDependendies();

	public abstract void setServiceDependendies(List serviceDependendies);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract String getAuthor();

	public abstract void setAuthor(String author);

	public abstract String getCreated();

	public abstract String getModified();

	public abstract int getPackageVersion();

	public abstract String getBundleType();

	public abstract void setBundleType(String type);

	public abstract boolean isActive();

	public abstract void setActive(boolean active);

	public abstract void updateModified();

	public abstract String getVersion();

	public abstract void setVersion(String version);

	public abstract String getProgramType();

	public abstract void setProgramType(String type);

	public abstract String getNotes();

	public abstract void setNotes(String notes);

	public abstract String getId();

	public abstract void setId(String id);
	
	public abstract ServiceDetail[] getServiceDetails();
	
	public abstract void setServiceDetails(ServiceDetail[] serviceDetails);

}