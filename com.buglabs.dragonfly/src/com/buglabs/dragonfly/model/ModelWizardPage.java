/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * A simple subclass of WizardPage that adds MVC-based functionality. All wizard
 * pages share a model in common.
 * 
 * @author Ken
 * 
 */
public abstract class ModelWizardPage extends WizardPage {
	protected Map model = null;

	private boolean previousState = false;

	/**
	 * @param pageName
	 */
	protected ModelWizardPage(String name, Map model) {
		super(name);
		this.model = model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public abstract void createControl(Composite parent);

	protected void setModelWizard() {
		/*
		 * if (wizard == null) { wizard =
		 * (ModelWizard)ModelWizardPage.this.getWizard(); }
		 */}

	public ModelWizard getModelWizard() {
		return (ModelWizard) getWizard();
	}

	/**
	 * Add page validation logic here. Returning <code>true</code> means that
	 * the page is complete and the user can go to the next page.
	 * 
	 * @return
	 */
	protected abstract boolean validatePage();

	/**
	 * This method should be implemented by ModelWizardPage classes. This method
	 * is called after the <code>validatePage()</code> returns successfully.
	 * Update the model with the contents of the controls on the page.
	 */
	protected abstract void updateModel();

	/**
	 * Helper method to see if a field has some sort of text in it.
	 * 
	 * @param value
	 * @return
	 */
	protected boolean hasContents(String value) {
		if (value == null || value.length() == 0) {
			return false;
		}

		return true;
	}

	/**
	 * This method is called right before a page is displayed. This occurs on
	 * user action (Next/Back buttons).
	 */
	public abstract void pageDisplay();

	/**
	 * This method is called on the concrete WizardPage after the user has gone
	 * to the page after.
	 */
	public abstract void pageCleanup();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean currentState) {
		ModelWizard wizard = (ModelWizard) getWizard();
		// System.out.println(getTitle() + " - Previous: " + previousState + "
		// currentState: " + currentState);

		if (!currentState && previousState) {
			wizard.previousPage = this;
			wizard.pageTransition(this, false);
		} else if (currentState && !previousState) {
			wizard.currentPage = this;
			if (wizard.getStartingPage().equals(this)) {
				wizard.pageTransition(this, true);
			}
		}

		previousState = currentState;

		super.setVisible(currentState);
	}

}
