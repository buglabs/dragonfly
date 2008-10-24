/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.ui.Activator;

public class ProgramEditorInput implements IEditorInput {

	private final ProgramNode pkg;

	// private final IPackage pkg;
	private final IFile file;

	public ProgramEditorInput(ProgramNode pkgn, IFile file) {
		this.pkg = pkgn;
		this.file = file;
	}

	public ProgramEditorInput(ProgramNode pkg) {
		this.pkg = pkg;
		file = null;
	}

	public ProgramNode getPackageNode() {
		return pkg;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {

		return Activator.getImageDescriptor("icons/sample.gif"); //$NON-NLS-1$
	}

	public String getName() {

		return pkg.getName();
	}

	public IPersistableElement getPersistable() {

		return null;
	}

	public String getToolTipText() {

		return pkg.getName() + Messages.getString("ProgramEditorInput.1") + pkg.getPackage().getAuthor(); //$NON-NLS-1$
	}

	public Object getAdapter(Class adapter) {
		if (adapter.getName().equals("org.eclipse.core.resources.IFile") && file != null) { //$NON-NLS-1$
			return file;
		}

		if (adapter.getName().equals("java.lang.Object")) { //$NON-NLS-1$
			return this;
		}

		return null;
	}

	public boolean equals(Object arg0) {
		if (arg0 instanceof ProgramEditorInput) {
			ProgramEditorInput otherInput = (ProgramEditorInput) arg0;

			if (otherInput.getName().equals(this.getName())) {
				return true;
			}

			return false;
		}

		return super.equals(arg0);
	}

}
