package com.buglabs.bug.module.lcd;

import java.io.InputStream;

import com.buglabs.bug.accelerometer.pub.AccelerometerSampleStream;
import com.buglabs.bug.accelerometer.pub.IAccelerometerRawFeed;
import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleFeed;
import com.buglabs.util.LogServiceUtil;
import com.buglabs.util.StreamMultiplexer;

public class AccelerometerRawFeed extends StreamMultiplexer implements IAccelerometerRawFeed, IAccelerometerSampleFeed {
	
	private static final int BUFFER_SIZE = 6;
	private static final int PROCESS_DELAY = 50;
	private static final int READ_DELAY = 250;

	private LCDAccelerometerSampleStream mass;

	public AccelerometerRawFeed(InputStream is, AccelerometerControl control) {
		super(is, BUFFER_SIZE, PROCESS_DELAY, READ_DELAY);
		setName("AccelerometerRawFeed");
		setLogService(LogServiceUtil.getLogService(LCDActivator.getDefault().getBundleContext()));
		mass = new LCDAccelerometerSampleStream(getInputStream(), control);
	}

	public AccelerometerSampleStream getSampleInputStream() {
		return mass;
	}
}
