package com.buglabs.dragonfly.model;

import java.net.MalformedURLException;
import java.util.Collection;

import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Service folder node. This folder contains information based on the call to
 * http://{server}/service web service.
 * 
 * @author akravets
 * 
 */
public class ServiceFolderNode extends FolderNode {
	private static final long serialVersionUID = 3584433339788701474L;

	private Bug bug;

	private boolean childrenLoaded = false;

	public ServiceFolderNode(Bug bug) {
		super("Services", bug);
		this.bug = bug;
	}

	public Collection getChildren() {
		if (!childrenLoaded && bug.isConnected()) {
			try {
				this.setChildren(BugWSHelper.getServices(bug.getServiceURL()));
			} catch (MalformedURLException e) {
				UIUtils.handleNonvisualError("Unable to retrieve services.", e);
			} catch (Exception e) {
				UIUtils.handleNonvisualError("Unable to retrieve services.", e);
			}
		}
		return super.getChildren();
	}

	public boolean hasChildren() {
		if (!childrenLoaded) {
			return true;
		}
		return super.hasChildren();
	}

	public void setLoaded(boolean b) {
		this.childrenLoaded = b;
	}
}
