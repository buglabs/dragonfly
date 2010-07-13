/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.buglabs.dragonfly.exception.NodeNotUniqueException;

/**
 * A general purpose tree model object.
 * 
 * @author ken
 * 
 */
public class BaseTreeNode extends BaseNode implements ITreeNode {
	private static final long serialVersionUID = 244185876040942855L;

	private List children;

	private ITreeNode parent;

	public BaseTreeNode(String name) {
		super(name);
	}

	public IModelNode addChild(IModelNode child) throws NodeNotUniqueException {
		if (children == null) {
			children = new ArrayList();
		}

		if (children.contains(child)) {
			throw new NodeNotUniqueException("A node with name " + child.getName() + " already exists at this level.");
		}

		children.add(child);

		return child;
	}

	public boolean childExists(IModelNode child) {
		if (children == null) {
			children = new ArrayList();
		}

		return children.contains(child);
	}

	public Collection getChildren() {
		if (children == null) {
			children = new ArrayList();
		}

		return children;
	}

	public ITreeNode getParent() {
		return parent;
	}

	public IModelNode removeChild(IModelNode child) {
		if (children == null) {
			children = new ArrayList();
		}

		if (childExists(child)) {
			children.remove(child);
			return child;
		}

		return null;
	}

	public ITreeNode setParent(ITreeNode parent) {
		this.parent = parent;
		return parent;
	}

	public boolean hasChildren() {
		if (children == null) {
			children = new ArrayList();
		}

		if (children.size() > 0) {
			return true;
		}
		return false;
	}

	public void setChildren(Collection c) {
		if (children == null) {
			children = new ArrayList();
		}

		children.clear();
		for (Iterator i = c.iterator(); i.hasNext();) {
			IModelNode node = (IModelNode) i.next();

			children.add(node);
		}

	}

	public ITreeNode getFirstParentOfType(String type) {
		ITreeNode node = this;

		while ((node = node.getParent()) != null) {
			if (node.getClass().getName().equals(type)) {
				return node;
			}
		}

		return null;
	}

	public void addChildren(Collection children) throws NodeNotUniqueException {
		for (Iterator i = children.iterator(); i.hasNext();) {
			addChild((IModelNode) i.next());
		}

	}

	public Collection getChildren(String name) {
		if (children == null) {
			children = new ArrayList();
		}

		ArrayList matchingChildren = new ArrayList();

		Iterator childIter = children.iterator();
		while (childIter.hasNext()) {
			IModelNode child = (IModelNode) childIter.next();
			if (child.getName().equals(name)) {
				matchingChildren.add(child);
			}
		}

		return matchingChildren;
	}
}
