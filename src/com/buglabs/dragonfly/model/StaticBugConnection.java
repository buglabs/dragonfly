package com.buglabs.dragonfly.model;

import java.net.URL;

/**
 * Static bug connection
 * 
 * @author akravets
 * 
 */
public class StaticBugConnection extends BugConnection {
	private static final long serialVersionUID = -499094392978149138L;
	private String defaultUrl = null;

	public StaticBugConnection(String name, URL url) {
		super(name, url);
		defaultUrl = url.toString();
	}
	
	public String getDefaultUrl(){
		return defaultUrl;
	}

}
