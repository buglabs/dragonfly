package com.buglabs.dragonfly.model;

import java.net.URL;

/**
 * Virtual BUG connection
 * 
 * @author akravets
 * 
 */
public class VirtualBUGConnection extends BugConnection {
	private static final long serialVersionUID = 2960878830723100719L;

	public VirtualBUGConnection(String name, URL url) {
		super(name, url);
	}
}
