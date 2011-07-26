/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.util.osgi.BUGBundleConstants;

/**
 * Application folder node
 * 
 * @author ken
 * 
 */
public class ApplicationFolderNode extends FolderNode {
	private static final long serialVersionUID = 8811724393295045129L;

	private boolean childrenLoaded = false;

	private final Bug bug;

	public ApplicationFolderNode(Bug bug) {
		super("Applications", bug);
		this.bug = bug;
	}

	public Collection getChildren() {
		if (!childrenLoaded && bug.isConnected()) {

			try {
				this.setChildren(BugWSHelper.getPrograms(bug.getProgramURL()));
			} catch (MalformedURLException e) {
				UIUtils.handleNonvisualError("Unable to retrieve programs.", e);
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if (cause instanceof ConnectException)
					UIUtils.handleNonvisualWarning("Unable to connect to " + bug.getUrl(), e, true);
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

	/**
	 * @return Bundles that are of type
	 *         <code>BugBundleConstants.BUG_BUNDLE_APPLICATION</code>
	 */
	public List getBugBundles() {
		List bugBundles = new ArrayList();

		Iterator iterator = getChildren().iterator();

		String bundleType;
		while (iterator.hasNext()) {
			ProgramNode bundle = (ProgramNode) iterator.next();
			bundleType = bundle.getBundleType();
			if (!bundleType.equals(BUGBundleConstants.BUG_BUNDLE_APPLICATION) && !bundleType.equals(BUGBundleConstants.BUG_BUNDLE_LIBRARY)) {
				continue;
			}
			bugBundles.add(bundle);
		}
		return bugBundles;
	}

	public void setLoaded(boolean b) {
		this.childrenLoaded = b;
	}
}
