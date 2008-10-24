package com.buglabs.dragonfly.ui.wizards.exportBugApp;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;

import com.buglabs.dragonfly.BugApplicationNature;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.osgi.concierge.ui.wizards.export.ExportBundlesWizard;

public class BugApplicationExportWizard extends ExportBundlesWizard {
	public BugApplicationExportWizard() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection(this.getClass().getName());

		if (settings == null) {
			settings = Activator.getDefault().getDialogSettings().addNewSection(this.getClass().getName());
		}

		setSettings(settings);
		setNatureID(BugApplicationNature.ID);
	}

	public void addPages() {
		super.addPages();
		WizardPage page1 = getProjectAndDestinationPage();
		page1.setTitle("Export BUG Applications");
		page1.setMessage("Export BUG Applications");
	}
}
