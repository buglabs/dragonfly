package com.buglabs.dragonfly.model;

import java.util.Collection;

import com.buglabs.dragonfly.exception.NodeNotUniqueException;

/**
 * Adds tree-style containment functionality to base model interface.
 * 
 * @author ken
 * 
 */
public interface ITreeNode extends IModelNode {
	public ITreeNode getParent();

	public ITreeNode setParent(ITreeNode parent);

	public Collection getChildren();

	public Collection getChildren(String name);

	public boolean childExists(IModelNode child);

	/**
	 * Add a child node to current. Note this modifies the child by setting it's
	 * parent to current node.
	 * 
	 * @param child
	 * @return
	 * @throws NodeNotUniqueException
	 */
	public IModelNode addChild(IModelNode child) throws NodeNotUniqueException;

	public void addChildren(Collection children) throws NodeNotUniqueException;

	public IModelNode removeChild(IModelNode child);

	/**
	 * @return TRUE if number of children greater than 0.
	 */
	public boolean hasChildren();

	public void setChildren(Collection children);

	/**
	 * Iterate through ancestors until one of specified type is found. NULL if
	 * none found.
	 * 
	 * @param type
	 * @return
	 */
	public ITreeNode getFirstParentOfType(String type);
}
