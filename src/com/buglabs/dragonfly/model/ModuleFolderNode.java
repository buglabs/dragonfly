/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;

import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * A container node for modules.
 * 
 * @author ken
 * 
 */
public class ModuleFolderNode extends FolderNode {

	private static final long serialVersionUID = -8709645597627912400L;

	private final Bug bug;

	private boolean childrenLoaded = false;

	public ModuleFolderNode(Bug bug) {
		super("Modules", bug);
		this.bug = bug;
	}

	public Collection getChildren() {
		if (!childrenLoaded && bug.isConnected()) {
			try {
				this.setChildren(BugWSHelper.getModuleList(this, bug.getModuleURL()));
			} catch (MalformedURLException e) {
				UIUtils.handleNonvisualError("Unable to load modules.", e);
			} catch (IOException e) {
				UIUtils.handleNonvisualError("Unable to load modules.", e);
			}
		}

		return super.getChildren();
	}

	public boolean hasChildren() {
		if (!childrenLoaded) {
			return true;
		}

		return super.hasChildren();
	}

}
