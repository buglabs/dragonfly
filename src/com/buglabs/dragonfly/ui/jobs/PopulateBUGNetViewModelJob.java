/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/

package com.buglabs.dragonfly.ui.jobs;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.AuthenticationData;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.views.BUGNetView;
import com.buglabs.dragonfly.bugnet.BugnetWSHelper;

/**
 * This job retrieves the bugnet model from bugnet.
 * 
 * @author ken
 * 
 */
public class PopulateBUGNetViewModelJob extends Job {

	private final BUGNetView bugnetView;

	public static final String JOB_TITLE = Messages.getString("PopulateBUGNetViewModelJob.0"); //$NON-NLS-1$

	private final Composite parent;

	private String username;

	private boolean isBUGNetDisabled = false;

	protected String connectionErrorMessage;

	private boolean performAuthentication;

	/**
	 * Constructor.
	 * 
	 * @param view
	 * @param root
	 * @param parent
	 * @param haultAuthenticaton
	 *            will perform authentication if <code>true</code>
	 */
	public PopulateBUGNetViewModelJob(BUGNetView view, ITreeNode root, Composite parent, boolean performAuthentication) {
		super(JOB_TITLE);
		bugnetView = view;
		this.parent = parent;
		username = ""; //$NON-NLS-1$
		this.performAuthentication = performAuthentication;
		connectionErrorMessage = "Unable to access BUGnet, please check Error Log.";
	}

	public boolean belongsTo(Object family) {
		if (family.equals(JOB_TITLE)) {
			return true;
		}

		return false;
	}

	protected IStatus run(IProgressMonitor monitor) {

		final List apps;
		final List userApps;

		try {
			// if BUGNet was disabled throw exception containing explanation
			if (!DragonflyActivator.getDefault().getPluginPreferences().getBoolean(DragonflyActivator.PREF_BUGNET_ENABLED)) {
				isBUGNetDisabled = true;
				throw new NoRouteToHostException("BUGnet is disabled.  You can enable BUGnet in preferences.");
			}
			final int numOfTopApps = DragonflyActivator.getDefault().getPluginPreferences().getInt(DragonflyActivator.PREF_BUGNET_NUM_OF_APPS);

			if (performAuthentication) {
				AuthenticationData authentication_data = DragonflyActivator.getDefault().getAuthenticationData();
				// if we don't have auth data, try to get stored auth data
				if (!authentication_data.hasData())
					DragonflyActivator.getDefault().setAuthDataFromPrefs();
				// if that stored data lets us log in, set username
				if (BugnetWSHelper.verifyCurrentUser()) 
					username = authentication_data.getUsername();
			}

			if (username == null) {
				username = ""; //$NON-NLS-1$
			}

			apps = BugnetWSHelper.getLatestApps(numOfTopApps);
			userApps = BugnetWSHelper.getUserApps(username);

			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

				public void run() {
					Activator.getDefault().setConnectionAvailable(true);
					
					String userAppCounter = "";
					int numOfUserApps = userApps.size();
					if(numOfUserApps > 0){
						userAppCounter = " (" + numOfUserApps + ")";
					}
					// My Apps Section
					Composite myApplicationsComposite = PopulateBUGNetViewModelJob.this.bugnetView.createSection(parent, Messages
							.getString("PopulateBUGNetViewModelJob.5") + userAppCounter); //$NON-NLS-1$
					
					Composite bugnetComposite = PopulateBUGNetViewModelJob.this.bugnetView.createSection(parent, Messages
							.getString("PopulateBUGNetViewModelJob.4") + " (" + numOfTopApps + ")"); //$NON-NLS-1$
					
					if(apps.size() == 0){
						PopulateBUGNetViewModelJob.this.bugnetView.generateNoApps(bugnetComposite);
					}
					else{
						PopulateBUGNetViewModelJob.this.bugnetView.generateDetail(bugnetComposite, apps);
					}

					if (userApps.isEmpty()) {
						if (username.trim().equals("")) { //$NON-NLS-1$
							PopulateBUGNetViewModelJob.this.bugnetView.generateLogin(myApplicationsComposite);
						} else {
							PopulateBUGNetViewModelJob.this.bugnetView.generateNoUserApps(myApplicationsComposite);
						}
					} else {
						PopulateBUGNetViewModelJob.this.bugnetView.generateDetail(myApplicationsComposite, userApps);
					}

					parent.layout();

					bugnetView.getForm().reflow(true);
				}
			});

			return Status.OK_STATUS;
		} catch (NoRouteToHostException e) {
			// Not an error since not having a network connection is a valid
			// operating state.
			//connectionErrorMessage = "Unable to access BUGnet, please check Error Log";
			IStatus status = null;
			if (isBUGNetDisabled) {
				status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0,
						Messages.getString("BUGnet functionality disabled by user"), null); //$NON-NLS-1$
				connectionErrorMessage = "Connection to BUGnet is disabled in\npreferences.\n\nTo enable connection check BUGnet settings.";
			} else {
				connectionErrorMessage = "Unable to access BUGnet \n" + "Please check your network \n" + "connection.";
				new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, Messages.getString("Unable to access BUGnet"), e); //$NON-NLS-1$
			}
			drawUnableToAccessBugNet();
			return status;
		} catch (IOException e) {
			Activator.getDefault().setConnectionAvailable(false);
			drawUnableToAccessBugNet();
			return new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, Messages.getString("PopulateBUGNetViewModelJob.7"), e); //$NON-NLS-1$
		}
	}

	private void drawUnableToAccessBugNet() {
		final FormToolkit toolkit = this.bugnetView.getToolKit();

		if (parent != null && toolkit != null) {
			Display d = PlatformUI.getWorkbench().getDisplay();
			d.asyncExec(new Runnable() {

				public void run() {
					toolkit.createLabel(parent, connectionErrorMessage);
					parent.layout();
				}
			});
		}
	}

	public void setPerformAuthentication(boolean performAuthentication) {
		this.performAuthentication = performAuthentication;
	}

}