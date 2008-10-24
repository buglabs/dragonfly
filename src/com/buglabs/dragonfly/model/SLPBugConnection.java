package com.buglabs.dragonfly.model;

import java.net.URL;

/**
 * BugConnection for SLP discovery
 * 
 * @author akravets
 * 
 */
public class SLPBugConnection extends BugConnection {
	private static final long serialVersionUID = 6449794999144066893L;

	public SLPBugConnection(String name, URL url) {
		super(name, url);
	}

}
