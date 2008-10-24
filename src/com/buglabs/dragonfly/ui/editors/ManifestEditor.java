package com.buglabs.dragonfly.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

public class ManifestEditor extends FormEditor {

	protected void addPages() {
		try {
			addPage(new BugBundleMainPage(this));
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}
}
