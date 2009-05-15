package com.buglabs.dragonfly.ui.editors;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.exception.BugnetAuthenticationException;
import com.buglabs.dragonfly.model.AuthenticationData;
import com.buglabs.dragonfly.model.IModelChangeListener;
import com.buglabs.dragonfly.ui.actions.BrowserRefreshEvent;
import com.buglabs.dragonfly.ui.actions.ImportBundleFromStreamAction;

/**
 * An editor for displaying BUGNet content in the editor area.
 * 
 * @author ken
 * 
 */
public class BrowserEditor extends EditorPart implements IModelChangeListener {
	private Composite comp;

	private Browser browser;

	private String url;

	private AuthenticationData authData;

	public BrowserEditor() {
		super();
	}

	public void doSave(IProgressMonitor monitor) {

	}

	public void doSaveAs() {

	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);

		DragonflyActivator.getDefault().addListener(this);

		if (input instanceof BrowserEditorInput) {
			this.url = ((BrowserEditorInput) input).getUrl();
			this.authData = ((BrowserEditorInput) input).getAuthData();
			this.setPartName(((BrowserEditorInput) input).getEditorTitle());
		}
	}

	public boolean isDirty() {

		return false;
	}

	public boolean isSaveAsAllowed() {

		return false;
	}

	public void createPartControl(Composite parent) {
		comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));

		browser = new Browser(comp, SWT.None);

		browser.addProgressListener(new ProgressListener() {
			public void changed(ProgressEvent event) {
			}

			public void completed(ProgressEvent event) {
				String myUrl = browser.getUrl();
				
				if (myUrl == null) {
					return;
				}
				
				try {
					myUrl = URLDecoder.decode(myUrl, "UTF-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				Map qs;
				try {
					qs = urlToMap(myUrl);

					if (qs != null && qs.containsKey("title")) { //$NON-NLS-1$ //$NON-NLS-2$
						downloadProgram((String) qs.get("title")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BugnetAuthenticationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();					
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			private Map urlToMap(String myUrl) throws MalformedURLException {
				URL u = new URL(myUrl);
				/*
				 * URI uri = null; try { uri = new URL(url); } catch
				 * (URISyntaxException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); }
				 */
				myUrl = u.getQuery();

				if (myUrl == null) {
					return null;
				}

				Map map = null;
				String[] t = myUrl.split("&"); //$NON-NLS-1$

				if (t != null && t.length > 0) {
					map = new Hashtable();

					for (int i = 0; i < t.length; ++i) {
						String[] nvp = t[i].split("="); //$NON-NLS-1$

						if (nvp.length == 2) {
							map.put(nvp[0], nvp[1]);
						}
					}
				}

				return map;
			}
		});

		browser.setLayoutData(new GridData(GridData.FILL_BOTH));

		if (!browser.setUrl(url)) {
			MessageDialog.openError(this.getSite().getShell(), Messages.getString("BrowserEditor.6"), //$NON-NLS-1$
					Messages.getString("BrowserEditor.7")); //$NON-NLS-1$
		}
	}

	public void setFocus() {
		browser.setFocus();

	}

	public void dispose() {
		DragonflyActivator.getDefault().removeListener(this);
		super.dispose();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt instanceof BrowserRefreshEvent) {
			browser.refresh();
		}

	}

	/**
	 * downloads program
	 * 
	 * @param id
	 * @throws TokenInvalidError
	 * @throws IOException
	 */
	private void downloadProgram(String project) throws IOException {
			ImportBundleFromStreamAction action = new ImportBundleFromStreamAction(project);
			action.run();
	}

}
