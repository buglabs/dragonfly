/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm.model;

import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * This models the time of day on a remote system. It might as well model any
 * other remote resource.
 */
public class BUGResource extends AbstractResource {

	private String fDaytime;

	/** Default constructor */
	public BUGResource() {
		super();
	}

	/**
	 * Constructor when parent subsystem is given
	 * 
	 * @param subsystem
	 *            the parent subsystem
	 */
	public BUGResource(ISubSystem subsystem) {
		super(subsystem);
	}

	public String getDaytime() {
		return fDaytime;
	}

	public void setDaytime(String daytime) {
		fDaytime = daytime;
	}

}
