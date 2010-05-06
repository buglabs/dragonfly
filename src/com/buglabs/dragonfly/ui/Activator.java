package com.buglabs.dragonfly.ui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.StaticBugConnection;

import com.buglabs.dragonfly.ui.actions.LaunchWelcomeEditorAction;
import com.buglabs.dragonfly.ui.jobs.ConnectBugHelper;
import com.buglabs.dragonfly.ui.jobs.LoadBugsJob;
import com.buglabs.dragonfly.ui.views.mybugs.MyBugsView;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;

public class Activator extends AbstractUIPlugin {

	public static final String IMAGE_KEY_BASE_UNIT_SELECTED = "images/baseUnitSelected.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_BASE_UNIT = "images/baseUnit.gif"; //$NON-NLS-1$
	
	public static final String IMAGE_KEY_BASE_UNIT_DISCONNECTED = "images/baseUnitDisconnected.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_MODULE_SLOT_0 = "images/upperLeftModule.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_MODULE_SLOT_2 = "images/lowerLeftModule.gif"; //$NON-NLS-1$	

	public static final String IMAGE_KEY_MODULE_SLOT_1 = "images/upperRightModule.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_MODULE_SLOT_3 = "images/lowerRightModule.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_MODULE_SLOT_0_SELECTED = "images/upperLeftModuleSelected.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_MODULE_SLOT_2_SELECTED = "images/lowerLeftModuleSelected.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_MODULE_SLOT_1_SELECTED = "images/upperRightModuleSelected.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_MODULE_SLOT_3_SELECTED = "images/lowerRightModuleSelected.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_RATING_FULL = "icons/rating_full.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_RATING_EMPTY = "icons/rating_empty.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_RATING_HALF = "icons/rating_half.gif"; //$NON-NLS-1$

	public static final String IMAGE_KEY_IMAGE_NOT_FOUND = "icons/img_not_found_thm.jpg"; //$NON-NLS-1$

	public static final String IMAGE_KEY_LEFT_MODULE_SELECTED = "IMAGE_KEY_LEFT_MODULE_SELECTED"; //$NON-NLS-1$

	public static final String IMAGE_KEY_RIGHT_MODULE_SELECTED = "IMAGE_KEY_RIGHT_MODULE_SELECTED"; //$NON-NLS-1$

	public static final String IMAGE_PATH_RIGHT_MODULE = "icons/bug_mod_left_off.gif"; //$NON-NLS-1$

	public static final String IMAGE_PATH_RIGHT_MODULE_SELECTED = "icons/bug_mod_left_on.gif"; //$NON-NLS-1$

	public static final String IMAGE_PATH_LEFT_MODULE = "icons/bug_mod_right_off.gif"; //$NON-NLS-1$

	public static final String IMAGE_PATH_LEFT_MODULE_SELECTED = "icons/bug_mod_right_on.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_APP = "icons/color/app.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_BUGVIEW_APPLICATION = "icons/color/application.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_BUGNET = "icons/color/bugnet.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_BUNDLE = "icons/color/bundle.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_DFLY = "icons/color/dfly.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_DWNLD = "icons/color/dwnld.gif"; //$NON-NLS-1$
	
	public static final String IMAGE_COLOR_DWNLD_SDK = "icons/color/dwnld_to_sdk.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_M_CAMERA = "icons/color/cameraModule.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_M_GPS = "icons/color/gpsModule.gif"; //$NON-NLS-1$
	
	public static final String IMAGE_COLOR_M_GSM = "icons/color/gsmModule.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_M_LCD = "icons/color/lcdModule.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_M_MOTION = "icons/color/motionModule.gif"; //$NON-NLS-1$
	
	public static final String IMAGE_COLOR_M_SOUND = "icons/color/soundModule.gif"; //$NON-NLS-1$
	
	public static final String IMAGE_COLOR_M_VH = "icons/color/vhModule.gif"; //$NON-NLS-1$
	
	public static final String IMAGE_COLOR_M_WIFI = "icons/color/wifibluetoothModule.gif"; //$NON-NLS-1$
	
	public static final String IMAGE_COLOR_M_BUGBEE = "icons/color/bugbeeModule.gif"; //$NON-NLS-1$	

	public static final String IMAGE_COLOR_M_SENSOR = "icons/color/sensorModule.gif";
	
	public static final String IMAGE_COLOR_MANIFEST = "icons/color/manifest.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_MODULE = "icons/color/module.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_PROJECT = "icons/color/project.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_UPLOAD = "icons/color/upload.gif"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_DIALOG_BUGNET = "icons/color/dialogue_bugnet.jpg"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_DIALOG_BUG = "icons/color/dialogue_bug.jpg"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_DIALOG_DRAGONFLY = "icons/color/dialogue_df.jpg"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_DIALOG_PROJECT = "icons/color/dialogue_project.jpg"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_DIALOG_CONNECTION = "icons/color/dialogue_connection.jpg"; //$NON-NLS-1$

	public static final String IMAGE_COLOR_V_EDIT = "icons/color/v_edit.gif"; //$NON-NLS-1$	

	public static final String IMAGE_CONNECTION_PROJECT = "/icons/color/staticConnection.gif"; //$NON-NLS-1$

	public static final String IMAGE_CONNECTION_ADD = "/icons/color/add.gif"; //$NON-NLS-1$	

	public static final String IMAGE_CONNECTION_DELETE = "/icons/color/deleteBug.gif"; //$NON-NLS-1$

	public static final String IMAGE_CONNECTION_REFRESH = "/icons/color/refresh.gif"; //$NON-NLS-1$

	public static final String IMAGE_PROPERTIES = "/icons/color/properties.gif"; //$NON-NLS-1$		

	public static final String IMAGE_COLOR_SERVICES = "/icons/color/services.gif"; //$NON-NLS-1$
	
	public static final String IMAGE_COLOR_APP_REMOVE = "/icons/color/app_remove.gif"; //$NON-NLS-1$
	
	public static final String IMAGE_COLOR_APP_SEARCH = "/icons/color/new/bugnetSearch.gif"; //$NON-NLS-1$

	public static final String PLUGIN_ID = "com.buglabs.dragonfly.ui"; //$NON-NLS-1$

	private static final String saveFileName = "bugs.xml"; //$NON-NLS-1$

	private static final String LOOPBACK_IP_ADDRESS = "127.0.0.1"; //$NON-NLS-1$

	public static final String ICON_VIRTUAL_BUG = "/icons/color/new/myBUG.gif";
	
	public static final String ICON_SLP_BUG = "/icons/color/new/slpBUG.gif";
	
	public static final String ICON_STATIC_BUG = "/icons/color/new/staticBUG.gif";
	
	public static final String ICON_DISCOVERED_BUG = "/icons/color/new/slpBUG.gif";
	

	private static Activator plugin;

	private SimpleHttpSever httpServiceThread;

	private BundleContext context;

	private ResourceBundle resourceBundle;

	private File bugsFileName = null; // file name where bugs are persisted

	private boolean isBugsLoaded = false;

	private boolean launchErrorVisible = true;

	private boolean isConnectionAvaiable = true;

	public Activator() {
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("com.buglabs.dragonfly.ui.messages"); //$NON-NLS-1$
		} catch (MissingResourceException e) {
			UIUtils.handleNonvisualError("Unable to load resource file", e);
			resourceBundle = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.context = context;
		
		File welcomScreenState = Activator.getDefault().getStateLocation().append("sdk-welcome.xml").toFile(); //$NON-NLS-1$

		// if file doesn't exist, create file, populate it with locking data and launch welcome editor
		if (!welcomScreenState.exists()) {
			welcomScreenState.createNewFile();
			XMLMemento xmlMemento = XMLMemento.createWriteRoot("sdk-welcome");
			xmlMemento.putString("loaded", "true");
			Writer writer = new FileWriter(welcomScreenState);
			xmlMemento.save(writer);

			launchWelcomeEditor();
		}
		// otherwise read from the file and if welcome editor has not been launched before, launch it now
		else {
			FileReader reader = new FileReader(welcomScreenState);

			boolean isWelcomeLoaded = false;
			// start reading from file only if it's available
			if (reader != null && reader.ready()) {
				IMemento memento = XMLMemento.createReadRoot(reader);
				isWelcomeLoaded = new Boolean(memento.getString("loaded")).booleanValue(); //$NON-NLS-1$
			}
			if (!isWelcomeLoaded)
				launchWelcomeEditor();
		}
		
		httpServiceThread = new SimpleHttpSever(DragonflyActivator.MODEL_CHANGE_EVENT_LISTEN_PORT);
		httpServiceThread.start();

		// Load bug connections from persistent storage location
		bugsFileName = Activator.getDefault().getStateLocation().append(saveFileName).toFile();
		if (!bugsFileName.exists()) {
			bugsFileName.createNewFile();
		}
		LoadBugsJob loadBugs = new LoadBugsJob(bugsFileName);
		loadBugs.setPriority(Job.SHORT);
		loadBugs.schedule();
	}

	private void launchWelcomeEditor() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){
			public void run() {
					LaunchWelcomeEditorAction welcomeAction = new LaunchWelcomeEditorAction();
					welcomeAction.run();
					
					//ActionFactory.MAXIMIZE.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()).run();
			}
		});
	}

	public File getSavedBugsFileName() {
		return bugsFileName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		httpServiceThread.setRunning(false);
		httpServiceThread.interrupt();
		sendPoisonPill();
		saveBugs();

		plugin = null;
		super.stop(context);
	}

	private void sendPoisonPill() {
		// Connecting to the socket should be enough to wake the server so that
		// it may shutdown.
		try {
			Socket s = new Socket(LOOPBACK_IP_ADDRESS, DragonflyActivator.MODEL_CHANGE_EVENT_LISTEN_PORT);
			s.close();
		} catch (UnknownHostException e) {
			// Swallow any exceptions, not relevant at bundle level.
		} catch (IOException e) {
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	protected void initializeImageRegistry(ImageRegistry imageRegistry) {
		imageRegistry.put(IMAGE_KEY_BASE_UNIT, getImageDescriptor(IMAGE_KEY_BASE_UNIT).createImage());
		imageRegistry.put(IMAGE_KEY_BASE_UNIT_DISCONNECTED, getImageDescriptor(IMAGE_KEY_BASE_UNIT_DISCONNECTED).createImage());
		imageRegistry.put(IMAGE_KEY_BASE_UNIT_SELECTED, getImageDescriptor(IMAGE_KEY_BASE_UNIT_SELECTED).createImage());
		imageRegistry.put(IMAGE_KEY_MODULE_SLOT_0, getImageDescriptor(IMAGE_KEY_MODULE_SLOT_0).createImage());
		imageRegistry.put(IMAGE_KEY_MODULE_SLOT_1, getImageDescriptor(IMAGE_KEY_MODULE_SLOT_1).createImage());
		imageRegistry.put(IMAGE_KEY_MODULE_SLOT_2, getImageDescriptor(IMAGE_KEY_MODULE_SLOT_2).createImage());
		imageRegistry.put(IMAGE_KEY_MODULE_SLOT_3, getImageDescriptor(IMAGE_KEY_MODULE_SLOT_3).createImage());
		imageRegistry.put(IMAGE_KEY_MODULE_SLOT_0_SELECTED, getImageDescriptor(IMAGE_KEY_MODULE_SLOT_0_SELECTED).createImage());
		imageRegistry.put(IMAGE_KEY_MODULE_SLOT_1_SELECTED, getImageDescriptor(IMAGE_KEY_MODULE_SLOT_1_SELECTED).createImage());
		imageRegistry.put(IMAGE_KEY_MODULE_SLOT_2_SELECTED, getImageDescriptor(IMAGE_KEY_MODULE_SLOT_2_SELECTED).createImage());
		imageRegistry.put(IMAGE_KEY_MODULE_SLOT_3_SELECTED, getImageDescriptor(IMAGE_KEY_MODULE_SLOT_3_SELECTED).createImage());
		imageRegistry.put(IMAGE_KEY_LEFT_MODULE_SELECTED, getImageDescriptor(IMAGE_PATH_LEFT_MODULE_SELECTED).createImage());
		imageRegistry.put(IMAGE_KEY_RIGHT_MODULE_SELECTED, getImageDescriptor(IMAGE_PATH_RIGHT_MODULE_SELECTED).createImage());
		imageRegistry.put(IMAGE_COLOR_APP, getImageDescriptor(IMAGE_COLOR_APP).createImage());
		imageRegistry.put(IMAGE_COLOR_BUGVIEW_APPLICATION, getImageDescriptor(IMAGE_COLOR_BUGVIEW_APPLICATION).createImage());
		imageRegistry.put(IMAGE_COLOR_BUGNET, getImageDescriptor(IMAGE_COLOR_BUGNET).createImage());
		imageRegistry.put(IMAGE_COLOR_BUNDLE, getImageDescriptor(IMAGE_COLOR_BUNDLE).createImage());
		imageRegistry.put(IMAGE_COLOR_DFLY, getImageDescriptor(IMAGE_COLOR_DFLY).createImage());
		imageRegistry.put(IMAGE_COLOR_DWNLD, getImageDescriptor(IMAGE_COLOR_DWNLD).createImage());
		imageRegistry.put(IMAGE_COLOR_DWNLD_SDK, getImageDescriptor(IMAGE_COLOR_DWNLD_SDK).createImage());
		imageRegistry.put(IMAGE_COLOR_M_CAMERA, getImageDescriptor(IMAGE_COLOR_M_CAMERA).createImage());
		imageRegistry.put(IMAGE_COLOR_M_GPS, getImageDescriptor(IMAGE_COLOR_M_GPS).createImage());
		imageRegistry.put(IMAGE_COLOR_M_GSM, getImageDescriptor(IMAGE_COLOR_M_GSM).createImage());
		imageRegistry.put(IMAGE_COLOR_M_LCD, getImageDescriptor(IMAGE_COLOR_M_LCD).createImage());
		imageRegistry.put(IMAGE_COLOR_M_MOTION, getImageDescriptor(IMAGE_COLOR_M_MOTION).createImage());
		imageRegistry.put(IMAGE_COLOR_M_SOUND, getImageDescriptor(IMAGE_COLOR_M_SOUND).createImage());
		imageRegistry.put(IMAGE_COLOR_M_VH, getImageDescriptor(IMAGE_COLOR_M_VH).createImage());
		imageRegistry.put(IMAGE_COLOR_M_WIFI, getImageDescriptor(IMAGE_COLOR_M_WIFI).createImage());
		imageRegistry.put(IMAGE_COLOR_M_BUGBEE, getImageDescriptor(IMAGE_COLOR_M_BUGBEE).createImage());
		imageRegistry.put(IMAGE_COLOR_M_SENSOR, getImageDescriptor(IMAGE_COLOR_M_SENSOR).createImage());
		imageRegistry.put(IMAGE_COLOR_MANIFEST, getImageDescriptor(IMAGE_COLOR_MANIFEST).createImage());
		imageRegistry.put(IMAGE_COLOR_MODULE, getImageDescriptor(IMAGE_COLOR_MODULE).createImage());
		imageRegistry.put(IMAGE_COLOR_PROJECT, getImageDescriptor(IMAGE_COLOR_PROJECT).createImage());
		imageRegistry.put(IMAGE_COLOR_UPLOAD, getImageDescriptor(IMAGE_COLOR_UPLOAD).createImage());
		imageRegistry.put(IMAGE_COLOR_V_EDIT, getImageDescriptor(IMAGE_COLOR_V_EDIT).createImage());
		imageRegistry.put(IMAGE_COLOR_DIALOG_BUG, getImageDescriptor(IMAGE_COLOR_DIALOG_BUG).createImage());
		imageRegistry.put(IMAGE_COLOR_DIALOG_BUGNET, getImageDescriptor(IMAGE_COLOR_DIALOG_BUGNET).createImage());
		imageRegistry.put(IMAGE_COLOR_DIALOG_DRAGONFLY, getImageDescriptor(IMAGE_COLOR_DIALOG_DRAGONFLY).createImage());
		imageRegistry.put(IMAGE_COLOR_DIALOG_PROJECT, getImageDescriptor(IMAGE_COLOR_DIALOG_PROJECT).createImage());
		imageRegistry.put(IMAGE_COLOR_DIALOG_CONNECTION, getImageDescriptor(IMAGE_COLOR_DIALOG_CONNECTION).createImage());
		imageRegistry.put(IMAGE_KEY_RATING_FULL, getImageDescriptor(IMAGE_KEY_RATING_FULL).createImage());
		imageRegistry.put(IMAGE_KEY_RATING_EMPTY, getImageDescriptor(IMAGE_KEY_RATING_EMPTY).createImage());
		imageRegistry.put(IMAGE_KEY_RATING_HALF, getImageDescriptor(IMAGE_KEY_RATING_HALF).createImage());
		imageRegistry.put(IMAGE_KEY_IMAGE_NOT_FOUND, getImageDescriptor(IMAGE_KEY_IMAGE_NOT_FOUND).createImage());
		imageRegistry.put(IMAGE_CONNECTION_PROJECT, getImageDescriptor(IMAGE_CONNECTION_PROJECT).createImage());
		imageRegistry.put(IMAGE_CONNECTION_ADD, getImageDescriptor(IMAGE_CONNECTION_ADD).createImage());
		imageRegistry.put(IMAGE_CONNECTION_DELETE, getImageDescriptor(IMAGE_CONNECTION_DELETE).createImage());
		imageRegistry.put(IMAGE_CONNECTION_REFRESH, getImageDescriptor(IMAGE_CONNECTION_REFRESH).createImage());
		imageRegistry.put(IMAGE_PROPERTIES, getImageDescriptor(IMAGE_PROPERTIES).createImage());
		imageRegistry.put(IMAGE_COLOR_SERVICES, getImageDescriptor(IMAGE_COLOR_SERVICES).createImage());
		imageRegistry.put(IMAGE_COLOR_APP_REMOVE, getImageDescriptor(IMAGE_COLOR_APP_REMOVE).createImage());
		imageRegistry.put(IMAGE_COLOR_APP_SEARCH, getImageDescriptor(IMAGE_COLOR_APP_SEARCH).createImage());
		
		imageRegistry.put(ICON_SLP_BUG, getImageDescriptor(ICON_SLP_BUG).createImage());
		imageRegistry.put(ICON_STATIC_BUG, getImageDescriptor(ICON_STATIC_BUG).createImage());
		imageRegistry.put(ICON_VIRTUAL_BUG, getImageDescriptor(ICON_VIRTUAL_BUG).createImage());
		
	}

	public File exportToJar(IProject proj) throws CoreException {
		return ProjectUtils.exporToJar(new File(getJarLocation()), proj);
	}

	public String getJarLocation() {
		return getStateLocation().toFile().getAbsolutePath();
	}

	public BundleContext getContext() {
		return context;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = Activator.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public static String getString(String key) {
		return getResourceString(key);
	}

	public static String getServiceDescription(String key) {
		ResourceBundle bundle = Activator.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return "Description not available";
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void saveBugs() throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(MyBugsView.BUGS_TYPE);
		synchronized (BugConnectionManager.getInstance()) {
			List bugs = (List) BugConnectionManager.getInstance().getBugConnections();
			Iterator iterator = bugs.iterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();
				// if(!(object instanceof LoadingBugNode)){
				BugConnection node = (BugConnection) object;
				if (node instanceof StaticBugConnection) {
					IMemento bug = xmlMemento.createChild(MyBugsView.BUG_TYPE);
					bug.putString(MyBugsView.BUG_NAME, node.getName());
					bug.putString(MyBugsView.BUG_URL, node.getUrl().toExternalForm());
				}
				// }
			}
		}

		Writer writer = new FileWriter(bugsFileName);
		xmlMemento.save(writer);
	}

	public void setBugsLoaded(boolean b) {
		isBugsLoaded = b;
	}

	public boolean isBugsLoaded() {
		return isBugsLoaded;
	}

	public void setLaunchErrorVisible(boolean b) {
		launchErrorVisible = b;
	}

	public boolean getLaunchErrorVisible() {
		return launchErrorVisible;
	}

	public void setConnectionAvailable(boolean b) {
		this.isConnectionAvaiable = b;
	}

	public boolean isConnectionAvailable() {
		return isConnectionAvaiable;
	}
}
