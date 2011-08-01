package com.buglabs.bug.sysfs;

import java.io.File;

import com.buglabs.bug.bmi.sysfs.BMIDevice;

/**
 * Access sysfs items for video module.
 * WIP
 * 
 * @author kgilmer
 *
 */
public final class VideoOutDevice extends BMIDevice {
	private boolean dviSet = true;
	
	public VideoOutDevice(File directory, int slot) {
		super(directory, slot);
	}

	/*
	 * resolution
	 */

	public String getResolution() {
		// TODO: really read it when exposed by driver
		return "1280x1024";
	}

	public boolean isVGA() {
		return !dviSet;
	}

	public boolean isDVI() {
		return dviSet;
	}
	
	public boolean setVGA() {
		dviSet = false;
		return true;
	}
	
	public boolean setDVI() {
		dviSet = true;
		return true;
	}
}
