package com.buglabs.dragonfly.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.IBugnetAuthenticationListener;
import com.buglabs.dragonfly.bugnet.BugnetStateProvider;
import com.buglabs.dragonfly.bugnet.BugnetWSHelper;
import com.buglabs.dragonfly.ui.dialogs.AuthenticationDialog;
import com.buglabs.dragonfly.ui.editors.GenericBrowserEditor;
import com.buglabs.dragonfly.ui.editors.GenericBrowserInput;
import com.buglabs.dragonfly.ui.jobs.Messages;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * 
 * Handles attempts to authenticate with BUGnet via dialog
 * 
 * @author brian
 *
 */
public class BugnetAuthenticationHelper {
	
	private static boolean saveAuthentication = false;
	private static boolean canceled = false;
	private static List<IBugnetAuthenticationListener> authenticationListeners;
	
	/*
	 * Add an authentication listener
	 */
	public static synchronized void addBugnetAuthenticationListener(
			IBugnetAuthenticationListener listener) {
		if (authenticationListeners == null) {
			authenticationListeners = new ArrayList<IBugnetAuthenticationListener>();
		}
		
		if (!authenticationListeners.contains(listener)) {
			authenticationListeners.add(listener);
		}
	}	
	
	/**
	 * Checks login without throwing an error so you can do simple
	 * so false means not logged in for whatever reason
	 * 
	 * @return
	 */
	public static boolean isLoggedIn() {
	    boolean logged_in = false;
	    try {
	        logged_in = checkLogin();
	    } catch (IOException e) {
            UIUtils.handleVisualError(
                    "Unable to verify log in, check the BUGnet URL in your preferences page.",e);	        
	    }
	    return logged_in;
	}	
	
	/*
	 * call this to try to login to bugnet with a prompt
	 * it'll try to use the credentials already available to it to login
	 * but if that doesn't work, query for it
	 * 
	 */
	public static boolean processLogin() throws IOException {
	    boolean logged_in = checkLogin();
		
		// don't need to query for login or notifiy listeners
		// because we're already logged in
		if (logged_in) {
			// make sure the authentication data is saved, tho
			saveAuthentication();
			return true;
		}
		
		// not logged in so call regular login method
		return login();
	}
	
	/*
	 * Assumed not logged in
	 */
	public static boolean login() throws IOException {
		canceled = saveAuthentication = false;
		boolean logged_in = false;
		
		// Loop shows prompt until user is logged in
		while(!canceled && !logged_in) {
			loginPrompt();
			if (!canceled && !(logged_in = BugnetWSHelper.verifyCurrentUser())) {
				UIUtils.handleVisualError(
					"Unable to set authentication data. It appears that you have entered an incorrect username or password.",
					new Exception());				
			}
		}
		
		// Login was successful, do some other stuff
		if (logged_in) {
			saveAuthentication();
			notifyLoggedInEvent();
		}
		return logged_in;		
	}
	
	/**
	 *  helper function removes stored authentication data
	 */
	public static void logout() {
		DragonflyActivator.getDefault().getAuthenticationData().setUsername(null);
		DragonflyActivator.getDefault().getAuthenticationData().setPassword(null);
		DragonflyActivator.getDefault().getPluginPreferences().setValue(DragonflyActivator.PREF_BUGNET_USER, "");
		DragonflyActivator.getDefault().getPluginPreferences().setValue(DragonflyActivator.PREF_BUGNET_PWD, "");
		notifyLoggedOutEvent();
	}	
	
	
	private static boolean checkLogin() throws IOException{
        // prepare authentication data
        // if there's something missing, try to get it from preferences
        if (!BugnetStateProvider.getInstance().getAuthenticationData().hasData())
            DragonflyActivator.getDefault().setAuthDataFromPrefs();
        
        return BugnetWSHelper.verifyCurrentUser();	    
	}
	
	
	private static void notifyLoggedInEvent() {
		// if there are no active listeners do not iterate through them
		if(authenticationListeners != null && authenticationListeners.size() != 0){
			Iterator<IBugnetAuthenticationListener> iter = authenticationListeners.iterator();
			while (iter.hasNext()) {
				IBugnetAuthenticationListener l = iter.next();
				l.loggedInEvent();
			}
		}
	}
	
	private static void notifyLoggedOutEvent() {
		// if there are no active listeners do not iterate through them
		if(authenticationListeners != null && authenticationListeners.size() != 0){
			Iterator<IBugnetAuthenticationListener> iter = authenticationListeners.iterator();
			while (iter.hasNext()) {
				IBugnetAuthenticationListener l = iter.next();
				l.loggedOutEvent();
			}
		}
	}
		
	
	/**
	 * if we checked saveAuthentication, try 'n save it
	 */
	private static void saveAuthentication() {
		// if we checked saveAuthentication, try 'n save it
		if (saveAuthentication) {
			DragonflyActivator.getDefault().saveAuthentication(
			        BugnetStateProvider.getInstance().getAuthenticationData().getUsername(), 
			        BugnetStateProvider.getInstance().getAuthenticationData().getPassword());
		}	
	}
	
	/**
	 * subroutine handles display of login window
	 */
	private static void loginPrompt() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				// pop up password dialog
				IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				Shell s;

				if (win != null) {
					s = win.getShell();
				} else {
					s = new Shell();
				}

				AuthenticationDialog d = new AuthenticationDialog(s);
				int userSelection = d.open();

				// Click Sign Up Link in Dialog
				if (userSelection == AuthenticationDialog.ACCOUNT_CREATE_ID) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

						public void run() {
							try {
								String bugNetBaseURL = BugnetWSHelper.getBugNetBaseURL();
								if (bugNetBaseURL == null || bugNetBaseURL.equals("/")) //$NON-NLS-1$
									bugNetBaseURL = Messages.getString("defaultURL"); //$NON-NLS-1$
								
								String path = "";
								try {
									path = URLEncoder.encode("/" + Messages.getString("AccountSingupAppPath") + "?context=IDE", "UTF-8");
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								URL url = new URL(bugNetBaseURL + "helper/redirect?path=" + path);

								GenericBrowserInput gbi = new GenericBrowserInput(url);
								gbi.setName("Create new account");
								gbi.setToolTip("Create new account");

								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(gbi,
										GenericBrowserEditor.ID);
							} catch (PartInitException e) {
								UIUtils.handleVisualError(Messages.getString("LaunchPhysicalEditorJob.1"), e); //$NON-NLS-1$
							} catch (MalformedURLException e) {
								UIUtils.handleVisualError(Messages.getString("LaunchPhysicalEditorJob.1"), e); //$NON-NLS-1$
							}
						}
					});
					canceled = true; // clicking sign up cancels dialog
				
					// click cancel button
				} else if (userSelection != Dialog.OK) {
					canceled = true;
					
				// only other option is to try to login
				} else {
				    BugnetStateProvider.getInstance().getAuthenticationData().setUsername(d.getUsername());
				    BugnetStateProvider.getInstance().getAuthenticationData().setPassword(d.getPwd());
					if (d.getSaveAuthentication()) saveAuthentication = true;
				}
			}
		});		
	}
	

}
