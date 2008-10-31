/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly;

import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.buglabs.dragonfly.bug.kernel.BugKernel;
import com.buglabs.dragonfly.model.AuthenticationData;
import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.IModelChangeListener;
import com.buglabs.dragonfly.model.IModelContainer;
import com.buglabs.dragonfly.model.MyLibraryNode;
import com.buglabs.dragonfly.util.UIUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The activator class controls the plug-in life cycle
 */
public class DragonflyActivator extends AbstractUIPlugin implements IModelContainer {
	public static final int MODEL_CHANGE_EVENT_LISTEN_PORT = 8990;

	private List bugList; // stores the list of bugs (model)

	private List modelListeners;

	private List authenticationListeners;

	public File jarFolder;

	private BundleContext context;

	// The plug-in ID
	public static final String PLUGIN_ID = "com.buglabs.dragonfly";

	public static final String ROOT_BUGNET_URL = "http://api.buglabs.net";

	public static final String PREF_SERVER_NAME = "PREF_SERVER_NAME";
	
	public static final String PREF_PROTOCOL = "PREF_PROTOCOL";

	public static final String PREF_BUGNET_NUM_OF_APPS = "PREF_BUGNET_NUM_OF_APPS";

	public static final String PREF_BUGNET_USER = "PREF_BUGNET_USER";

	public static final String PREF_BUGNET_PWD = "PREF_BUGNET_PWD";

	public static final String PREF_BUGNET_ENABLED = "PREF_BUGNET_ENABLED";

	public static final String PROJ_LIBRARY_NAME = "My Library";

	public static final String PREF_DEFAULT_BUGPORT = "PREF_DEFAULT_BUGPORT";
	
	public static final int HTTPS_PORT = 443;
	
	public static final String HTTP = "http://";
	
	public static final String HTTPS = "https://";

	public static final String DEFAULT_PROTOCOL = "DEFAULT_PROTOCOL";
	
	public static final String VIRTUAL_BUG = "Virtual BUG";
	
	private String protocol = "";

	// The shared instance
	private static DragonflyActivator plugin;

	private static AuthenticationData authData;

	private IProject myLibraryProject;

	private ResourceBundle resourceBundle;

	private boolean isVirtualBugRemovedByTerminate;

	public DragonflyActivator() {
		plugin = this;
		bugList = new ArrayList();
		modelListeners = new ArrayList();
		authData = new AuthenticationData();
		try {
			resourceBundle = ResourceBundle.getBundle("com.buglabs.dragonfly.pluginProperties"); //$NON-NLS-1$
			protocol = DragonflyActivator.getString("DEFAULT_PROTOCOL");
		} catch (MissingResourceException e) {
			UIUtils.handleNonvisualError("Unable to load resource file", e);
			resourceBundle = null;
			protocol = HTTPS;
		}
	}

	public AuthenticationData getAuthenticationData() {
		return authData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ISaveParticipant saveParticipant = new DragonflySaveParticipant();

		ISavedState lastState = ResourcesPlugin.getWorkspace().addSaveParticipant(this, saveParticipant);
		if (lastState != null) {
			IPath location = lastState.lookup(new Path("save"));
			if (location != null) {
				File f = getStateLocation().append(location).toFile();
				if (f.isFile() && f.exists()) {
					loadModel(f);
				}
			}
		} else {
			bugList.add(new MyLibraryNode("My Library", null));
		}

		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static DragonflyActivator getDefault() {
		return plugin;
	}

	public void addListener(IModelChangeListener listener) {
		modelListeners.add(listener);
	}

	public void fireModelChangeEvent(PropertyChangeEvent event) {
		for (Iterator i = modelListeners.iterator(); i.hasNext();) {
			IModelChangeListener l = (IModelChangeListener) i.next();
			l.propertyChange(event);
		}
	}

	public Object getModel() {
		return bugList;
	}

	public void removeListener(IModelChangeListener listener) {
		modelListeners.remove(listener);
	}

	private void loadModel(File f) throws IOException {
		FileReader fr = new FileReader(f);

		XStream xs = new XStream(new DomDriver());

		bugList = (List) xs.fromXML(fr);

		fr.close();
	}

	public void saveModel(File f) throws FileNotFoundException {

		PrintWriter pw = new PrintWriter(new FileOutputStream(f));
		XStream s = new XStream(new DomDriver());

		for (Iterator i = bugList.iterator(); i.hasNext();) {
			Object node = i.next();

			if (node instanceof Bug) {
				Bug b = (Bug) node;
				b.disconnect();
			}
		}

		pw.println(s.toXML(bugList));

		pw.flush();
		pw.close();
	}

	public static String getAppId() {

		return "BUG Dragonfly IDE";
	}

	/*
	public void addProgramToLibrary(ProgramNode prog) {
		IFile progFile = myLibraryProject.getFile(prog.getName() + ".bpx");

		if (!progFile.exists()) {

			try {
				String contents = BugnetWSHelper.getProgramContents(prog.getPackage(), getAuthenticationData());
				progFile.create(new ByteArrayInputStream(contents.getBytes()), true, new NullProgressMonitor());
			} catch (CoreException e) {
				UIUtils.handleVisualError("An error occurred while creating My Library project", e);
			} catch (Exception e) {
				UIUtils.handleVisualError("An error occurred while creating My Library project", e);
			}
		}

	}
	*/
	public File createFile(String fileName) {
		return context.getDataFile(fileName);
	}

	public void saveAuthentication(String username, String pwd) {
		getPluginPreferences().setValue(PREF_BUGNET_USER, username);
		getPluginPreferences().setValue(PREF_BUGNET_PWD, pwd);
	}
	
	public void setAuthDataFromPrefs() {
		authData.setUsername(getPluginPreferences().getString(PREF_BUGNET_USER));
		authData.setPassword(getPluginPreferences().getString(PREF_BUGNET_PWD));	
	}

	public String getBugKernelLocation() {
		try {
			return BugKernel.getDefault().getBugKernelLocation();
		} catch (Exception e) {
			UIUtils.handleVisualError("Unable to determine location of BUG libraries.", e);
		}
		return "";
	}

	/**
	 * Returns a list of Bug Connection projects
	 * 
	 * @return
	 */
	public IProject[] getBugProjects() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ArrayList bugConnectionProjects = new ArrayList();

		for (int i = 0; i < allProjects.length; ++i) {
			IProject proj = allProjects[i];
			if (proj.isOpen()) {
				try {
					if (proj.hasNature(BugNature.ID)) {
						bugConnectionProjects.add(proj);
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return (IProject[]) bugConnectionProjects.toArray(new IProject[bugConnectionProjects.size()]);
	}

	private String getStreamAsString(InputStream in) throws IOException {
		StringBuffer sb = new StringBuffer();

		BufferedReader rd = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = rd.readLine()) != null) {
			if (line.trim().length() > 0) {
				sb.append(line);
			}
		}

		rd.close();

		return sb.toString();
	}

	/**
	 * 
	 * @return a list of java.io.File referring to each jar.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public List getBugKernelJars() {
		try {
			return BugKernel.getDefault().getBugKernelJars();
		} catch (Exception e) {
			UIUtils.handleNonvisualError("Unable to retrieve BUG libraries.", e);
		}

		return new ArrayList(0);
	}

	public synchronized void addBUGnetAuthenticationListener(IBUGnetAuthenticationListener listener) {
		if (authenticationListeners == null) {
			authenticationListeners = new ArrayList();
		}

		if (!authenticationListeners.contains(listener)) {
			authenticationListeners.add(listener);
		}
	}

	public List getBUGnetAuthenticationLIsteners() {
		return authenticationListeners;
	}

	/**
	 * @return http port number defined in preferences
	 */
	public String getHttpPort() {
		return getPluginPreferences().getString(DragonflyActivator.PREF_DEFAULT_BUGPORT);
	}

	public static String getString(String key) {
		return getResourceString(key);
	}

	public static String getResourceString(String key) {
		ResourceBundle bundle = getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public boolean isVirtualBugRemovedByTerminate() {
		return isVirtualBugRemovedByTerminate;
	}
	
	public void setVirtualBugRemovedByTerminate(boolean b){
		isVirtualBugRemovedByTerminate = b;
	}

	/**
	 * Returns current protocol either http or https
	 * @return current protocol
	 */
	/*
	public String getProtocol() {
		return protocol;
	}
	*/
	
	/**
	 * Sets current protocol
	 * @param protocol
	 */
	/*
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	*/
	
	/**
	 * Resets protocol to https
	 */
	/*
	public void resetProtocol() {
		protocol = HTTPS;
	}
	*/
}
