/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.beans.PropertyChangeEvent;

/**
 * A class for generating model change events.
 * 
 * @author Ken
 * 
 */
public class ModelNodeChangeEvent extends PropertyChangeEvent {
	private static final long serialVersionUID = 1L;

	private IModelNode changedNode;

	private boolean refreshSelection = false;

	/**
	 * @param source
	 *            source
	 * @param changedNode
	 *            oldValue
	 */
	public ModelNodeChangeEvent(Object source, IModelNode changedNode) {
		super(source, null, changedNode, null);
	}
	
	public ModelNodeChangeEvent(Object source, String propertyName, IModelNode changeNode){
		super(source,propertyName,changeNode,changeNode);
	}

	/**
	 * @return Returns the changedNode.
	 */
	public IModelNode getChangedNode() {
		return changedNode;
	}

	/**
	 * @param changedNode
	 *            The changedNode to set.
	 */
	public void setChangedNode(IModelNode changedNode) {
		this.changedNode = changedNode;
	}

	/**
	 * This is a hint to viewers that they should refresh thier selections so
	 * other dependent views will update themselves.
	 * 
	 * @return
	 */
	public boolean isRefreshSelection() {
		return refreshSelection;
	}

	/**
	 * Sets the hint for viewers to update the selection. Should only be used if
	 * necessary.
	 * 
	 * @param refreshSelection
	 */
	public void setRefreshSelection(boolean refreshSelection) {
		this.refreshSelection = refreshSelection;
	}
}
