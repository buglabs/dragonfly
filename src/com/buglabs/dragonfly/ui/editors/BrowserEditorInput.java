/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.editors;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.buglabs.dragonfly.model.AuthenticationData;

/**
 * Input for the web broswer editor.
 * 
 * @author ken
 * 
 */
public class BrowserEditorInput implements IEditorInput {

	private AuthenticationData authData;

	private final URL url;

	private final String editorTitle;

	/**
	 * @param authData
	 *            Authentication data
	 * @param url2
	 * @param editorTitle
	 */
	public BrowserEditorInput(AuthenticationData authData, URL url2, String editorTitle) {
		this.authData = authData;
		this.url = url2;
		this.editorTitle = editorTitle;
	}

	public BrowserEditorInput(URL url2, String editorTitle) {
		this.url = url2;
		this.editorTitle = editorTitle;
	}

	public String getEditorTitle() {
		return editorTitle;
	}

	public boolean exists() {

		return true;
	}

	public ImageDescriptor getImageDescriptor() {

		return null;
	}

	public String getName() {

		return url.getPath();
	}

	public IPersistableElement getPersistable() {

		return null;
	}

	public String getToolTipText() {

		return Messages.getString("BrowserEditorInput.0"); //$NON-NLS-1$
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	public AuthenticationData getAuthData() {
		return authData;
	}

	public String getUrl() {
		return url.toExternalForm();
	}

	public boolean equals(Object obj) {

		if (obj instanceof BrowserEditorInput) {
			BrowserEditorInput otherBEI = (BrowserEditorInput) obj;

			return (otherBEI.getUrl().equalsIgnoreCase(this.getUrl()));
		}

		return super.equals(obj);
	}
}
