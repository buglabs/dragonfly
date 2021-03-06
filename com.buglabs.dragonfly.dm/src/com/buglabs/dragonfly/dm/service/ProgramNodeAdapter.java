/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm.service;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.buglabs.dragonfly.dm.BUGResources;
import com.buglabs.dragonfly.dm.actions.UninstallAction;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.ui.Activator;

import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;

public class ProgramNodeAdapter extends AbstractSystemViewAdapter implements IAdaptable {

	final ProgramNode node;

	public ProgramNodeAdapter(ProgramNode next) {
		this.node = next;
	}

	public Object getAdapter(Class adapter) {
		//System.out.println("IAdaptable looking for " + adapter.getName());

		if (adapter == ISystemViewElementAdapter.class) {
			return this;
		}

		return null;
	}

	public String getText(Object element) {

		return node.getName();
	}

	public String getAbsoluteName(Object object) {

		return node.getName();
	}

	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell parent, String menuGroup) {
		if (isApplicationBundle(node)) {
			menu.add(menuGroup, new UninstallAction(node, selection));
		}
	}

	public ImageDescriptor getImageDescriptor(Object element) {
		if (isApplicationBundle(node)) {
			return Activator.getImageDescriptor(Activator.IMAGE_COLOR_APP);
		} else {
			return Activator.getImageDescriptor(Activator.IMAGE_APP);
		}
	}

	private boolean isApplicationBundle(ProgramNode node2) {
		return node2.getBundleType().equals("Application");
	}

	public String getType(Object element) {

		return BUGResources.BUG_Resource_Type;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(IAdaptable element) {
		return false;
	}

	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor) {
		return null;
	}

	protected IPropertyDescriptor[] internalGetPropertyDescriptors() {
		return node.getPropertyDescriptors();
	}

	protected Object internalGetPropertyValue(Object key) {
		return node.getPropertyValue(key);
	}
}
