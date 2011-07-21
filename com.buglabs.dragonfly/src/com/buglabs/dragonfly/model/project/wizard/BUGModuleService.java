/*******************************************************************************
 * Copyright (c) 2011 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model.project.wizard;

/**
 * An OSGi service provided by a BUG module.
 * @author kgilmer
 *
 */
public class BUGModuleService {

	private final String javaName;
	private final String description;
	private final String shortName;
	private boolean selected;

	/**
	 * @param javaName Full Java name of service.  Ex com.buglabs.service.KitchenSink.
	 */
	public BUGModuleService(String shortName, String javaName, String description) {
		this.shortName = shortName;
		this.javaName = javaName;
		this.description = description;
		this.selected = false;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * @return human readable name for service
	 */
	public String getShortName() {
		return shortName;
	}
	
	public String getDescription() {
		return description;
	}
	
	/**
	 * @return Get the full package name of the service.
	 */
	public String getName() {
		return javaName;
	}
}
