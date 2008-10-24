package com.buglabs.dragonfly.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * An editor that opens a web browser with provided <code>URL</code>
 * 
 * @author akravets
 * 
 */
public class GenericBrowserEditor extends EditorPart {

	private String url;

	private Browser browser;

	public static final String ID = "com.buglabs.dragonfly.ui.editors.genericBrowserEditor"; //$NON-NLS-1$

	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);

		if (input instanceof GenericBrowserInput) {
			this.url = ((GenericBrowserInput) input).getUrl();
		}
	}

	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void createPartControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));

		browser = new Browser(comp, SWT.None);

		browser.setLayoutData(new GridData(GridData.FILL_BOTH));

		if (!browser.setUrl(url)) {
			MessageDialog.openError(this.getSite().getShell(), Messages.getString("BrowserEditor.6"), //$NON-NLS-1$
					Messages.getString("BrowserEditor.7")); //$NON-NLS-1$
		}
	}

	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
