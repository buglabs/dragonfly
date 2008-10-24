/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import com.buglabs.dragonfly.exception.NodeNotUniqueException;

/**
 * A model class for a "my library" folder node in the explorer view.
 * 
 * @author ken
 * 
 */
public class MyLibraryNode extends FolderNode {
	public MyLibraryNode(String name, ITreeNode parent) {
		super(name, parent);
	}

	private static final long serialVersionUID = 6493490362028196127L;

	public IModelNode addChild(IModelNode child) throws NodeNotUniqueException {
		super.addChild(child);

		return child;
	}
}
