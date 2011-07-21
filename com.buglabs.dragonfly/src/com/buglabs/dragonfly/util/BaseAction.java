/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.buglabs.dragonfly.model.IModelNode;

/**
 * This abstract action handles updating the action with the model object
 * currently being selected by the user. This allows the action to adjust itself
 * based on the selection.
 * 
 * Actions that are to be shared among plugins need to have this class as the
 * parent.
 * 
 * Any objects assocated with this action must implement IModelNode.
 * 
 * @author kgilmer
 */
public abstract class BaseAction extends Action implements ISelectionChangedListener {
	/**
	 * This variable holds either a valid model object currently being selected
	 * in the view or null.
	 */
	protected IModelNode node = null;

	public BaseAction() {
		setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection s = (IStructuredSelection) event.getSelection();

		IModelNode selectedNode = (IModelNode) s.getFirstElement();

		changeState(selectedNode);
	}

	/**
	 * Override this method to determine if the selected node is legal for this
	 * action or not.
	 * 
	 * @param node
	 * @return false if the action should be disabled.
	 */
	public abstract boolean isValidNode(Object selectedNode);

	private void changeState(IModelNode selectedNode) {
		if (isValidNode(selectedNode)) {
			node = selectedNode;
			setEnabled(true);
			// System.out.println("Enabled: " + selectedNode.getName() + " " +
			// this.getClass().toString());
		} else {
			node = null;
			setEnabled(false);
			// System.out.println("Disabled: " + selectedNode.getName() + " " +
			// this.getClass().toString());
		}
	}

	public IModelNode getSelection() {
		return node;
	}

	public void setSelection(IModelNode selection) {
		node = selection;
	}
}
