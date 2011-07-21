/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.util.Map;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

/**
 * A simple Wizard subclass that allows the child pages to determine if the
 * entire wizard is finishable or not by calling <code>setFinishable()</code>.
 * 
 * @author Ken
 */
public abstract class ModelWizard extends Wizard {
	private boolean finishable = false;

	private boolean canFinish;

	ModelWizardPage currentPage;

	ModelWizardPage previousPage;

	public ModelWizard() {
	}

	public boolean performFinish() {
		if (currentPage != null) {
			currentPage.pageCleanup();
		}

		return true;
	}

	/**
	 * @return Returns if the wizard is finishable in its current state.
	 */
	public boolean isFinishable() {
		return finishable;
	}

	/**
	 * @param finishable
	 *            Change the finish state of the wizard.
	 */
	public void setFinishable(boolean finishable) {
		this.finishable = finishable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#createPageControls(org.eclipse.swt.widgets.Composite)
	 */
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public boolean canFinish() {
		if (canFinish)
			return true;
		return super.canFinish();
	}

	public void setCanFinish(boolean canFinish) {
		this.canFinish = canFinish;
	}

	/**
	 * Retrive the model object from the wizard.
	 * 
	 * @return
	 */
	public abstract Map getModel();

	public void pageTransition(ModelWizardPage page, boolean isNew) {

		if (isNew) {
			page.pageDisplay();
		} else {
			if (previousPage != null) {
				previousPage.pageCleanup();
			}

			if (currentPage != null) {
				currentPage.pageDisplay();
			}
		}
	}
}
