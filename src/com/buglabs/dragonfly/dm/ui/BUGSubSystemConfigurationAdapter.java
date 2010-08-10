/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/

package com.buglabs.dragonfly.dm.ui;

import org.eclipse.jface.wizard.IWizard;

import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.view.SubSystemConfigurationAdapter;
import org.eclipse.rse.ui.wizards.newconnection.ISystemNewConnectionWizardPage;

public class BUGSubSystemConfigurationAdapter extends SubSystemConfigurationAdapter {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.SubSystemConfigurationAdapter#getNewConnectionWizardPages(org.eclipse.rse.core.subsystems.ISubSystemConfiguration, org.eclipse.jface.wizard.IWizard)
	 */
	public ISystemNewConnectionWizardPage[] getNewConnectionWizardPages(ISubSystemConfiguration factory, IWizard wizard) {
		ISystemNewConnectionWizardPage[] basepages = super.getNewConnectionWizardPages(factory, wizard);

		if (true) {
			BUGConnectionWizardPage page = new BUGConnectionWizardPage(wizard, factory);
			ISystemNewConnectionWizardPage[] newPages = new ISystemNewConnectionWizardPage[basepages.length + 1];
			newPages[0] = page;
			for (int i = 0; i < basepages.length; i++) {
				newPages[i + 1] = basepages[i];
			}
			basepages = newPages;
		}
		return basepages;
	}

}
