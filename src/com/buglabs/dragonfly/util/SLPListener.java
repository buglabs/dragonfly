package com.buglabs.dragonfly.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.iks.slp.Locator;
import ch.ethz.iks.slp.ServiceLocationEnumeration;
import ch.ethz.iks.slp.ServiceType;
import ch.ethz.iks.slp.ServiceURL;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.SLPBugConnection;
import com.buglabs.dragonfly.model.VirtualBUGConnection;

/**
 * SLP type listener
 * 
 * @author akravets
 * 
 */
public class SLPListener extends BugListener {

	private static final String jSLP_SERVICE_NAME = "ch.ethz.iks.slp.Locator";

	private BundleContext context;

	public SLPListener(BundleContext context, ITreeNode root, List discoveredBugs) {
		super(root, discoveredBugs);
		this.context = context;
	}

	protected List getBugs() throws Exception {
		int virtualBugCounter = 1; // counter that identifies each virtual bug
		boolean virtuaBugExists = false;
		
		List bugs = Collections.synchronizedList(new ArrayList());
		ServiceReference locRef = context.getServiceReference(jSLP_SERVICE_NAME);

		if (locRef != null) {
			Locator locator = (Locator) context.getService(locRef);
			
			// we get all the services that match
			ServiceLocationEnumeration slenum = locator.findServices(new ServiceType("service:bug"), null, null);
			
			while (slenum.hasMoreElements()) {
				ServiceURL service = (ServiceURL) slenum.nextElement();
				BugConnection bug = null;
				
				if (isVirtualBug(service)) {
					String bugName = DragonflyActivator.VIRTUAL_BUG;
					if(virtuaBugExists){
						bugName = DragonflyActivator.VIRTUAL_BUG + " (" + virtualBugCounter + ")";
					}
					
					bug = new VirtualBUGConnection(bugName, new URL("http://" + service.getHost() + ":" + service.getPort()));
					
					if(!virtuaBugExists){
						virtuaBugExists = true;
					}
					
					// virtual bug is connected, check if it's different url
					if(!bugs.contains(bug)){
						virtualBugCounter++;
					}
					else{
						bug = null;
					}
				}
				else{
					bug = new SLPBugConnection(service.getHost(), new URL("http://" + service.getHost() + ":" + service.getPort()));
				}
				if(!bugs.contains(bug) && bug != null)
					bugs.add(bug);

			}
		} else {
			UIUtils.handleNonvisualError("Unable to access the " + jSLP_SERVICE_NAME + " service.  Unable to discover BUGs.", null);
		}

		return bugs;
	}

	/**
	 * It's a virtual BUG if it's found on one of my local interfaces
	 * 
	 * @param service
	 * @return
	 */
	private boolean isVirtualBug(ServiceURL service) {
		return service.getHost().equals(URLUtils.getNetworkIterfaceAddress(service.getHost()));
	}

	
	protected boolean isValidType(Object o) {
		return o instanceof SLPBugConnection;
	}
}