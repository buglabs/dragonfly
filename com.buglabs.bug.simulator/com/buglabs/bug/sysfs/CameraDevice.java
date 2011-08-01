package com.buglabs.bug.sysfs;

import java.io.File;

import com.buglabs.bug.bmi.sysfs.BMIDevice;

/**
 * A work-in-progress for sysfs class for java clients.
 * @author kgilmer
 *
 */
public final class CameraDevice extends BMIDevice {

	public CameraDevice(File directory, int slot) {
		super(directory, slot);
	}
}
