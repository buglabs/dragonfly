package com.buglabs.dragonfly;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

import com.buglabs.dragonfly.exception.NodeNotUniqueException;
import com.buglabs.dragonfly.model.BaseTreeNode;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.DiscoveredBugConnection;
import com.buglabs.dragonfly.model.IModelNode;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.model.VirtualBUGConnection;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * This singleton class manages the BUG connections.
 * Through this class the sdk knows what BUGs are connected.
 * It keeps a combination of avahi/mDNS connections (DiscoveredBugConnection),
 *  Virtual Bug Connections (VirtualBUGConnection), 
 *  and manually created connections (StaticBugConnection).
 *  
 *  get a collection of connected bugs:
 *  	BugConnectionManager.getInstance().getBugConnections()
 *  
 *  It is important to note that the underlying ITreeNode object, which stores
 *  BugConnectionS, is not thread safe.  All access should be synchronized against
 *  the singleton object.
 *  
 *  NOTE: In nearly every case the logic of modifying bug_connections_root has been implemented
 *  in a synchronized method in this class, so use those synchronized methods. If you make
 *  modifications to this class be careful of the synchronized methods -- don't call synchronized
 *  methods from other synchronized methods in this class
 * 
 * @author bballantine
 *
 */
public class BugConnectionManager {
	
	private static final String BUGS_ROOT_NAME 	= "Connected Bugs Root";
	private static final String SERVICE_TYPE 	= "_bugdevice._tcp.local.";
	private static final String PROTOCOL 		= "http://";
	private static final String VIRTUAL_BUG_URL = PROTOCOL + "localhost";
	private static final String USB_BUG_NAME	= "USB BUG";
	private static final String USB_BUG_IP		= "10.10.10.10";
	
	public static final String REFRESH_BUG 		= "refresh_bug";
	public static final String REMOVE_BUG 		= "remove_bug";
	public static final String ADD_BUG 			= "add_bug";	
	
	private ITreeNode bug_connections_root;
	// this lock is only for creation of the connected bugs_root,
	// used in getBugConnectionsRoot()
	private Object root_creation_lock = new Object();
	private JmDNS jmdns;
	
	/**
	 *  keep track of singleton instance
	 */
	private static BugConnectionManager _instance;

	/**
	 * return the BugConnectionManager instance or 
	 * create and init a new one if it doesn't yet exist
	 * 
	 * @return
	 */
	public static BugConnectionManager getInstance() {
		if (_instance == null) {
			synchronized(BugConnectionManager.class) {
				if (_instance == null) {
					_instance = new BugConnectionManager();
				}
			}
		}
		return _instance;
	}	
	
	/**
	 * private constructor to protect singleton
	 * initializes the the jmdns object which helps manage avahi services
	 */
	private BugConnectionManager() {
		// jmdns is our main connection
		try {
			jmdns = JmDNS.create();
		} catch (IOException e) {
			UIUtils.handleNonvisualWarning("Unable to create JmDNS instance.", e);
		}
		if (jmdns == null) return;
		jmdns.addServiceListener(SERVICE_TYPE, new BugDeviceServiceListener());
		
		// try to get USB BUG
		new Thread() {
			public void run() {
				StaticBugConnection usbBug = null;
				List<Object> programs = null;
				try {
					usbBug = new StaticBugConnection(
							USB_BUG_NAME, new URL(PROTOCOL + USB_BUG_IP));
					programs = BugWSHelper.getPrograms(usbBug.getProgramURL());
				} catch (Exception e) {
					UIUtils.handleNonvisualError("Unable to connect to USB BUG.",e);
					return;
				}
				if (programs == null) return;
				addBugConnection(usbBug);
			};
		}.start();
	}
	
	
	/**
	 * Return current connections as the root ITreeNode
	 * This is used in the jface viewer to display connected bugs
	 * 
	 * @return
	 */
	public ITreeNode getBugConnectionsRoot() {
		if (bug_connections_root == null) {
			synchronized (root_creation_lock) {
				if (bug_connections_root == null) {
					bug_connections_root = new BaseTreeNode(BUGS_ROOT_NAME);
				}
			}
		}
		return bug_connections_root;
	}
	
	/**
	 * returns bug connections as a Collection of IModelNodeS
	 * 
	 * @return
	 */
	public Collection<IModelNode> getBugConnections() {
		return getBugConnectionsRoot().getChildren();
	}
	
	/**
	* Returns a specific bug connection based on its name
	* 
	* @param connectionName
	* @return
	*/
	public synchronized BugConnection getBugConnection(String name) {
		Collection<IModelNode> chillins = getBugConnections();
		for (IModelNode child : chillins) {
			if (child.getName().equals(name))
				return (BugConnection) child;
		}
		return null;
	}
	 
	/**
	* Returns a specific bug connection based on its name
	* 
	* @param connectionName
	* @return
	*/
	public synchronized BugConnection getBugConnectionByIP(String ipaddress) {
		Collection<IModelNode> chillins = getBugConnections();
		for (IModelNode child : chillins) {
			if (child instanceof BugConnection && 
					((BugConnection) child).getUrl().getHost().equals(ipaddress))
				return (BugConnection) child;
		}
		return null;
	}	
	
	/**
	 * Adds a new bug connection to the tracked bugs and fires a
	 *  bug connection change event
	 * 
	 * @param connectedBug
	 */
	public synchronized void addBugConnection(BugConnection connectedBug) {
		try {
			getBugConnectionsRoot().addChild(connectedBug);
			fireBugAddedEvent(this, connectedBug);
		} catch (NodeNotUniqueException e) {
			UIUtils.handleNonvisualError("Bug already exists.", e);
		}
	 }
	
	/**
	 * fires ModelChangeEvent for the BUG removal
	 * 
	 * @param source
	 * @param element
	 */
	public void fireBugRemovedEvent(Object source, IModelNode element) {
		if (DragonflyActivator.getDefault() == null) return;
		DragonflyActivator.getDefault().fireModelChangeEvent(
				new PropertyChangeEvent(source, REMOVE_BUG, null, element));		 
	}
	
	/**
	 * fires ModelChangeEvent for adding a BUG
	 *  
	 * @param source
	 * @param element
	 */
	public void fireBugAddedEvent(Object source, IModelNode element) {
		if (DragonflyActivator.getDefault() == null) return;
		DragonflyActivator.getDefault().fireModelChangeEvent(
				new PropertyChangeEvent(source, ADD_BUG, null, element));
	}
	
	/**
	 * fires ModelChangeEvent for refreshing a BUG
	 *  
	 * @param source
	 * @param element
	 */
	public void fireBugRefreshEvent(Object source, IModelNode element) {
		if (DragonflyActivator.getDefault() == null) return;
		DragonflyActivator.getDefault().fireModelChangeEvent(
				new PropertyChangeEvent(source, REFRESH_BUG, null, element));
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public boolean sameNameConnected(String name) {
    	if (getBugConnection(name) == null)
    			return false;
    	else
    		return true;
	}	
	
	/**
	 * 
	 * @param event
	 * @return
	 */
	public boolean isConnected(ServiceInfo info) {
		if (info == null) return false;
		return (getBugConnectionByIP(
				info.getAddress().getHostAddress()) != null);
	}
	
	/**
	 * Special method to handle adding a new virtual BUG 
	 */
	public void addNewVirtualBugConnection() {
		try {
			addBugConnection(new VirtualBUGConnection(DragonflyActivator.VIRTUAL_BUG, 
					new URL(VIRTUAL_BUG_URL + ":" + DragonflyActivator.getDefault().getHttpPort())));
		} catch (MalformedURLException e) {
			UIUtils.handleNonvisualError("Unable to create Virtual Bug Connection", e);
		}
	}
	
	/**
	 * Special method to handle removing the Virtual BUG
	 * @return
	 */
	public synchronized boolean removeVirtualBugConnection() {
		Collection<IModelNode> chillins = getBugConnections();
		for (IModelNode child : chillins) {
			if (child instanceof VirtualBUGConnection) {
				getBugConnectionsRoot().removeChild(child);
				fireBugRemovedEvent(this, child);
				return true;
			}
		}
		return false;
	}
		
	
	/**
	 * Iterate through all the Bugs in the model and run a refresh action.
	 * 
	 * @throws CoreException
	 * @throws MalformedURLException
	 */
	public synchronized void refreshBugConnections() {
		Collection<IModelNode> chillins = getBugConnections();
		for (IModelNode child : chillins) {
			fireBugRefreshEvent(this, child);
		}
	}
	
	/**
	 * remove a list of bug connections
	 * 
	 * @param connections
	 */
	public synchronized void removeBugConnections(List<BugConnection> connections) {
		for (BugConnection connection : connections) {
			getBugConnections().remove(connection);
			fireBugRemovedEvent(this, connection);
		}
	}

	/**
	 * remove a bug connection
	 * 
	 * @param connection
	 */
	public synchronized void removeBugConnection(BugConnection connection) {
		getBugConnections().remove(connection);
		fireBugRemovedEvent(this, connection);
	}
	
	/**
	 *  call this from plugin shutdown
	 */
	public synchronized void destroy() {
		jmdns.close();
		bug_connections_root.getChildren().clear();
	}
	
	/**
	 * This listens for mDNS events and handles the adding and removal
	 * of BUGs based on these events.
	 * 
	 * @author bballantine
	 *
	 */
	private class BugDeviceServiceListener implements ServiceListener {

		/**
		 * When service is added, kick off request for service info (a request to resolve service)
		 *  when service is resolved, serviceResolved event will get called
		 */
		public void serviceAdded(final ServiceEvent event) {
			UIUtils.log(new Status(Status.INFO, 
					DragonflyActivator.PLUGIN_ID, "BUG Device Added - " + event.getName()));
			new Thread(new Runnable() {
				public void run() {
					event.getDNS().requestServiceInfo(
							event.getType(), event.getName());
				}
			}).start();
        }
		
		/**
		 * The service is removed, remove from list and send event
		 */
        public void serviceRemoved(ServiceEvent event) {
        	UIUtils.log(new Status(Status.INFO, 
        			DragonflyActivator.PLUGIN_ID, "Bug Device Removed - " + event.getName()));
        	IModelNode bug = getBugConnection(event.getName());
        	if (bug == null) return;
        	
        	if (bug instanceof DiscoveredBugConnection) {
        		removeBugConnection((BugConnection)bug);
        	}
        }
        
        /**
         *  The service is resolved, add to list and send event
         *  
         *  TODO - remove some of logging
         */
        public void serviceResolved(final ServiceEvent event) {
        	if (event.getInfo() == null) return;
        	
        	UIUtils.log(new Status(Status.INFO, DragonflyActivator.PLUGIN_ID, 
        			"Bug Device Resolved - " + event.getName() + " " + event.getInfo().getHostAddress()));
        	
        	URL bugUrl = null;
			try {
				bugUrl = new URL(PROTOCOL + event.getInfo().getAddress().getHostAddress());
			} catch (MalformedURLException e) {
				UIUtils.handleNonvisualWarning("Unable to get url of connected BUG.", e);
			}
			if (bugUrl == null) return;
			
			BugConnection bug = null;
			// if bug w/ same IP address get it
			if (isConnected(event.getInfo())) {
				bug = getBugConnectionByIP(bugUrl.getHost());
				// if it's a discovered bug set the name to the new name
				if (bug instanceof DiscoveredBugConnection) {
					bug.setName(event.getName());
				}
				UIUtils.log(new Status(Status.INFO, DragonflyActivator.PLUGIN_ID, 
						"Bug w/ same ip as " + event.getName() + " exists: " + bug.getName()));
			}
			// else if bug w/ same name get it and set IP address
			else if (sameNameConnected(event.getName())) {
				bug = getBugConnection(event.getName());
				// set IP address
				bug.setUrl(bugUrl);
				UIUtils.log(new Status(Status.INFO, DragonflyActivator.PLUGIN_ID, 
						"Bug w/ same name as " + event.getName() + " exists, ip: " + bugUrl.getHost()));				
			}
			// finally it's brand new so create it and add it
			else {
				bug = new DiscoveredBugConnection(event.getName(), bugUrl);
				getBugConnections().add(bug);
			}

			// fire bug added event so view is updated, etc.
			if (bug != null)
				fireBugAddedEvent(this, bug);

        }        
	}
}
