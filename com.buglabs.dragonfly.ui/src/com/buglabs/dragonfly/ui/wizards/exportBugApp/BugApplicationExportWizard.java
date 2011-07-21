package com.buglabs.dragonfly.ui.wizards.exportBugApp;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IWorkbench;

import com.buglabs.dragonfly.BugApplicationNature;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Exports BUG applications and PDE bundles.
 * @author kgilmer
 *
 */
public class BugApplicationExportWizard extends ExportBundlesWizard {
	public BugApplicationExportWizard() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection(this.getClass().getName());

		if (settings == null) {
			settings = Activator.getDefault().getDialogSettings().addNewSection(this.getClass().getName());
		}

		setSettings(settings);
		setNatureID(BugApplicationNature.ID);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		//Determine type of selection and then modify the wizard accordingly.
		Object o = selection.getFirstElement();
		try {
			if (o instanceof IJavaProject) {
				IProject prj = ((IJavaProject) o).getProject();
				
				if (prj.hasNature(BugApplicationNature.ID)) {
					setNatureID(BugApplicationNature.ID);
				} else if (prj.hasNature("org.eclipse.pde.PluginNature")) {
					setNatureID("org.eclipse.pde.PluginNature");
				}
			}
		} catch (CoreException e) {
			UIUtils.handleNonvisualError("Unable to detect project nature in " + this.getClass().getName(), e);
		}

		super.init(workbench, selection);
	}

	public void addPages() {
		super.addPages();
		WizardPage page1 = getProjectAndDestinationPage();

		if (getNatureID().equals(BugApplicationNature.ID)) {
			page1.setTitle("Export BUG Applications");
			page1.setMessage("Export BUG Applications");
		} else if (getNatureID().equals("org.eclipse.pde.PluginNature")) {
			page1.setTitle("Export OSGi Bundles");
			page1.setMessage("Export OSGi Bundles");
		}
	}
}
