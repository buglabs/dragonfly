/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.net.URL;

import org.eclipse.core.resources.IProject;

/**
 * Wrapper model class for Bug model class for project view.
 * 
 * @author ken
 * 
 */
public class BugProjectNode extends Bug {
	private final IProject project;

	private static final long serialVersionUID = 7272494174801806948L;

	public BugProjectNode(String name, URL url, IProject project) {
		super(name, url);
		this.project = project;
	}

	public IProject getProject() {
		return project;
	}
}
