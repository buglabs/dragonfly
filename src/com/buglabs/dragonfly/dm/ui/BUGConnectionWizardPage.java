/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.dm.ui;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.wizards.AbstractSystemNewConnectionWizardPage;

public class BUGConnectionWizardPage extends AbstractSystemNewConnectionWizardPage {

	public BUGConnectionWizardPage(IWizard wizard, ISubSystemConfiguration parentConfig) {
		super(wizard, parentConfig);
	}

	public Control createContents(Composite parent) {
		Text field = new Text(parent, SWT.NONE);
		
		return field;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemNewSubSystemProperties#applyValues(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public boolean applyValues(ISubSystem ss) {
		return true;
	}

}
