/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.ui.editors.BrowserEditorInput;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Launches welcome editor
 * 
 * @author akravets
 * 
 */
public class LaunchWelcomeEditorAction extends Action {
	private URL url;

	private String title;

	public LaunchWelcomeEditorAction() {
		try {
			url = new URL("http://www.buglabs.net/sdk/start");
			title = "Welcome";
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			IEditorInput input = new BrowserEditorInput(new URL(url.toExternalForm()), title); //$NON-NLS-1$

			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, "com.buglabs.dragonfly.ui.editors.BrowserEditor"); //$NON-NLS-1$
		} catch (PartInitException e) {
			UIUtils.handleVisualError(Messages.getString("LaunchBrowserAction.6"), e); //$NON-NLS-1$
		} catch (IOException e) {
			UIUtils.handleVisualError(Messages.getString("LaunchBrowserAction.7"), e); //$NON-NLS-1$
		}
	}
}