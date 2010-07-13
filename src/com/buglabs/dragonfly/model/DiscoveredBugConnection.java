package com.buglabs.dragonfly.model;

import java.net.URL;

/**
 * A connection object for bugs auto-discovered (via mDNS) See
 * BugConnectionManager
 * 
 * @author bballantine
 * 
 */
public class DiscoveredBugConnection extends BugConnection {
	private static final long serialVersionUID = -7359825904708703100L;

	public DiscoveredBugConnection(String name, URL url) {
		super(name, url);
	}
}
