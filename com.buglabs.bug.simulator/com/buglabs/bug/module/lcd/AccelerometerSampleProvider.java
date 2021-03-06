package com.buglabs.bug.module.lcd;

import java.io.IOException;

import com.buglabs.bug.accelerometer.pub.AccelerometerSample;
import com.buglabs.bug.accelerometer.pub.AccelerometerSampleStream;
import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleFeed;
import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleProvider;


public class AccelerometerSampleProvider implements IAccelerometerSampleProvider {
	
	private Accelerometer acc;
	private IAccelerometerSampleFeed accFeed;

	public AccelerometerSampleProvider(IAccelerometerSampleFeed accFeed, Accelerometer acc) {
		this.accFeed = accFeed;
		this.acc = acc;
	}

	/**
	 * Reads a sample from the accelerometer input stream
	 * @return an AccelerometerSample.
	 */
	public synchronized AccelerometerSample readSample() throws IOException {
		AccelerometerSampleStream is = accFeed.getSampleInputStream();
		AccelerometerSample sample = is.readSample();
		return sample;
	}
}
