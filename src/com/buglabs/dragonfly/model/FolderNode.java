/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import com.buglabs.dragonfly.exception.NodeNotUniqueException;

public class FolderNode extends BaseTreeNode {
	private static final long serialVersionUID = -8755794801199514915L;

	public FolderNode(String name, ITreeNode parent) {
		super(name);
		this.setParent(parent);

		if (parent != null) {
			try {
				parent.addChild(this);
			} catch (NodeNotUniqueException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
