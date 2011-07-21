/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.io.Serializable;

import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Base interface for nodes contained in IModelContainers.
 * 
 * @author Ken
 */
public interface IModelNode extends IPropertySource, Serializable {

	/**
	 * Name property used in the UI.
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Sets the name property used in the UI. Not unique.
	 * 
	 * @param name
	 */
	public abstract void setName(String name);
}