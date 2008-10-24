package com.buglabs.dragonfly.ui.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.ui.editors.GenericBrowserEditor;
import com.buglabs.dragonfly.ui.editors.GenericBrowserInput;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * An action that will launch a browser
 * 
 * @author akravets
 * 
 */
public class LaunchGenericBrowserAction extends Action {
	private URL url;

	/**
	 * Constructor.
	 * 
	 * @param url
	 *            URL with which browser should open
	 */
	public LaunchGenericBrowserAction(String url) {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			try {
				this.url = new URL("about:blank");
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void run() {
		try {

			IEditorInput input = new GenericBrowserInput(new URL("http://buglabs.net")); //$NON-NLS-1$

			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, GenericBrowserEditor.ID); //$NON-NLS-1$
		} catch (PartInitException e) {
			UIUtils.handleVisualError(Messages.getString("LaunchBrowserAction.6"), e); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
