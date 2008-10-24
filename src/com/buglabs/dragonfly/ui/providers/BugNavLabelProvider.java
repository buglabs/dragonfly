/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/

package com.buglabs.dragonfly.ui.providers;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.model.FolderNode;
import com.buglabs.dragonfly.model.IModelNode;
import com.buglabs.dragonfly.model.Module;
import com.buglabs.dragonfly.model.MyLibraryNode;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.ui.Activator;

public class BugNavLabelProvider implements ILabelProvider {
	private Image bugImage;

	private Image programImage;

	private ISharedImages shared;

	private Image moduleImage;

	private Image myLibraryImage;

	public BugNavLabelProvider() {
		bugImage = Activator.getImageDescriptor("icons/bug.gif").createImage(); //$NON-NLS-1$
		shared = PlatformUI.getWorkbench().getSharedImages();
		programImage = Activator.getImageDescriptor("icons/bugscript.gif").createImage(); //$NON-NLS-1$
		moduleImage = Activator.getImageDescriptor("icons/module.gif").createImage(); //$NON-NLS-1$
		myLibraryImage = Activator.getImageDescriptor("icons/library.gif").createImage(); //$NON-NLS-1$
	}

	public Image getImage(Object element) {

		if (element instanceof MyLibraryNode) {
			return myLibraryImage;
		}

		if (element instanceof FolderNode) {
			return shared.getImage(ISharedImages.IMG_OBJ_FOLDER);
		}

		if (element instanceof Module) {
			return moduleImage;
		}

		if (element instanceof ProgramNode) {
			return programImage;
		}

		return bugImage;
	}

	public String getText(Object element) {
		if (element instanceof IModelNode) {
			return ((IModelNode) element).getName();
		}

		return element.toString();

	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		bugImage.dispose();
		programImage.dispose();
		moduleImage.dispose();
		myLibraryImage.dispose();
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

}
