/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import java.io.IOException;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.exception.BugnetAuthenticationException;
import com.buglabs.dragonfly.model.AuthenticationData;
import com.buglabs.dragonfly.ui.editors.BrowserEditorInput;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Launch the browser editor with specified URL.
 * 
 * @author ken
 * 
 */
public class LaunchBrowserAction extends Action {

	private AuthenticationData authData;

	private final URL url;

	private final String title;

	private boolean checkAuthentication = true;

	/**
	 * Deprecated because not used.
	 * Call LaunchBrowserAction(URL url, ...) and pre-prepare url with token if needed
	 * 
	 * @deprecated - use LaunchBrowserAction(URL url, String title)
	 * 
	 * @param authData
	 *            Authentication Data
	 * @param url2
	 *            Url to connect
	 */
	public LaunchBrowserAction(AuthenticationData authData, URL url) {
		this.authData = authData;
		this.url = url;
		title = Messages.getString("LaunchBrowserAction.0"); //$NON-NLS-1$
	}

	public LaunchBrowserAction(URL url) {
		this.url = url;
		title = Messages.getString("LaunchBrowserAction.0"); //$NON-NLS-1$
		checkAuthentication = false;
	}

	public LaunchBrowserAction(URL url, String title) {
		this.url = url;
		this.title = title;
		checkAuthentication = false;
	}

	/**
	 * @deprecated - use LaunchBrowserAction(URL url, String title)
	 * @param authData
	 * @param url
	 * @param editorTitle
	 */
	public LaunchBrowserAction(AuthenticationData authData, URL url, String editorTitle) {
		this.authData = authData;
		this.url = url;
		title = editorTitle;
	}

	public void run() {
		try {
			IEditorInput input = null;
			
			/* 
			Commented out by BB - not being used AFAIK.
			Token no longer comes from authData, but is asked for anew each time
			Anyway, this.url should have token already on it if it's needed
			
			TODO - clean up?
			
			if (checkAuthentication) {
				
				
				if (authData.getToken() == null) {
					try {
						authData = BUGNetAuthenticationHelper.getAuthenticationData(true);
						input = new BrowserEditorInput(authData, new URL(url.toExternalForm() + "&token=" + authData.getToken()), title); //$NON-NLS-1$						
					} catch (BugnetAuthenticationException e) {
						MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages
								.getString("LaunchBrowserAction.1"), //$NON-NLS-1$
								Messages.getString("LaunchBrowserAction.2") + e.getStackTrace().toString()); //$NON-NLS-1$
						// TODO log TokenInvalidError exception
					}

					if (authData.getToken() == null) {
						UIUtils.handleNonvisualError(Messages.getString("LaunchBrowserAction.3"), null); //$NON-NLS-1$
						return;
					}
				}
			}
			*/

			input = new BrowserEditorInput(new URL(url.toExternalForm()), title); //$NON-NLS-1$

			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input,
					"com.buglabs.dragonfly.ui.editors.BrowserEditor"); //$NON-NLS-1$
		} catch (PartInitException e) {
			UIUtils.handleVisualError(Messages.getString("LaunchBrowserAction.6"), e); //$NON-NLS-1$
		} catch (IOException e) {
			UIUtils.handleVisualError(Messages.getString("LaunchBrowserAction.7"), e); //$NON-NLS-1$
		}
	}

	public void setCheckAuthentication(boolean value) {
		this.checkAuthentication = value;
	}
}