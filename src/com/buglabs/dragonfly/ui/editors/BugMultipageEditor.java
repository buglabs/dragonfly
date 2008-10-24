/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.util.UIUtils;

public class BugMultipageEditor extends MultiPageEditorPart implements IResourceChangeListener {

	private IEditorPart editor;

	private Bug bug;

	public BugMultipageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	private void createPhysicalPage() throws PartInitException {

		editor = new PhysicalEditor(bug);
		int index = addPage(editor, getEditorInput());
		setPageText(index, Messages.getString("BugMultipageEditor.0")); //$NON-NLS-1$

	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		try {
			createPhysicalPage();
			createBUGNetPage();
		} catch (PartInitException e) {
			UIUtils.handleVisualError(Messages.getString("BugMultipageEditor.1"), e); //$NON-NLS-1$
		}
	}

	private void createBUGNetPage() {
		// TODO Auto-generated method stub

	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {

		if (!(editorInput instanceof BugEditorInput)) {
			throw new PartInitException(Messages.getString("BugMultipageEditor.2") + editorInput.getClass().getName()); //$NON-NLS-1$
		}

		this.bug = ((BugEditorInput) editorInput).getBug();

		super.init(site, editorInput);
		setPartName(bug.getName());
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {

	}
}
