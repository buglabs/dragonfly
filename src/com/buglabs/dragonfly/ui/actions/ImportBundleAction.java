/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import java.io.InputStream;

import org.eclipse.jface.action.Action;

import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Import a bundle (with source) into workspace from a BUG.
 * 
 * @author angel
 * 
 */
public class ImportBundleAction extends Action {

	private final ProgramNode program;

	public ImportBundleAction(ProgramNode program) {
		this.program = program;
	}

	public void run() {
		try {
			if (program != null) {
				InputStream jarContents = BugWSHelper.getProgramJar(program);
				ImportBundleFromStreamAction action = new ImportBundleFromStreamAction(program.getName(), jarContents);
				action.run();
			}
		} catch (Exception e1) {
			UIUtils.handleVisualError(Messages.getString("ImportBundleAction.0") + program.getName() + Messages.getString("ImportBundleAction.1"), e1); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
}
