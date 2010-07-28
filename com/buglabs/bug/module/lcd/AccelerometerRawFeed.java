package com.buglabs.bug.module.lcd;

import java.io.InputStream;

import com.buglabs.bug.accelerometer.pub.AccelerometerSampleStream;
import com.buglabs.bug.accelerometer.pub.IAccelerometerRawFeed;
import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleFeed;

public class AccelerometerRawFeed implements IAccelerometerRawFeed, IAccelerometerSampleFeed {
	private AccelerometerControl control;
	private final InputStream is;

	public AccelerometerRawFeed(InputStream is, AccelerometerControl control) {	
		this.is = is;
		this.control = control;
	}

	public AccelerometerSampleStream getSampleInputStream() {
		return new LCDAccelerometerSampleStream(getInputStream(), control);
	}

	public InputStream getInputStream() {
		return is;
	}
}
