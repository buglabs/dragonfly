/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/

package com.buglabs.dragonfly.ui.providers;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.IModelNode;
import com.buglabs.dragonfly.model.ITreeNode;

public class BugNavContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {

		if (parentElement instanceof ITreeNode) {
			ITreeNode tn = (ITreeNode) parentElement;

			return tn.getChildren().toArray(new IModelNode[tn.getChildren().size()]);
		}

		return null;
	}

	public Object getParent(Object element) {
		if (element instanceof ITreeNode) {
			return ((ITreeNode) element).getParent();
		}

		return null;
	}

	public boolean hasChildren(Object element) {

		if (element instanceof Bug) {
			return true;
		}

		if (element instanceof ITreeNode) {
			return ((ITreeNode) element).hasChildren();
		}

		return false;
	}

	public Object[] getElements(Object inputElement) {

		return ((List) inputElement).toArray(new ITreeNode[((List) inputElement).size()]);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
