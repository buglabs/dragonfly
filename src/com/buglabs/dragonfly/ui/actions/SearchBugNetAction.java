package com.buglabs.dragonfly.ui.actions;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.bugnet.BugnetWSHelper;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.views.BUGNetView;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Action that opens up browser with BUGnet Search page
 * @author akravets
 *
 */
public class SearchBugNetAction extends Action {

	public SearchBugNetAction() {
		setToolTipText("Browse Applications");
	}

	public void run() {
		synchronized (this) {
			String serverName = DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_SERVER_NAME);
			String path = "";
			try {
				path = URLEncoder.encode("/applications?context=IDE", "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			URL url = null;
			try {
				url = new URL(BugnetWSHelper.getBugNetBaseURL() + "helper/redirect?path=" + path);
				LaunchBrowserAction action = new LaunchBrowserAction(url, "BUGnet Search");
				action.run();
			} catch (MalformedURLException e) {
				UIUtils.handleVisualError("Invalid URL: " + url.toString(), e); //$NON-NLS-1$
			}
		}
	}

	public ImageDescriptor getImageDescriptor() {
		return Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_COLOR_APP_SEARCH);
	}
}