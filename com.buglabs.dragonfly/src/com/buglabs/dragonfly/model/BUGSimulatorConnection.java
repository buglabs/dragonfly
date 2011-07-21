package com.buglabs.dragonfly.model;

import java.net.URL;

/**
 * BUG Simulator connection
 * 
 * @author akravets
 * 
 */
public class BUGSimulatorConnection extends BugConnection {
	private static final long serialVersionUID = 2960878830723100719L;

	public BUGSimulatorConnection(String name, URL url) {
		super(name, url);
	}
}
