package com.buglabs.bug.module.lcd;

import com.buglabs.bug.accelerometer.pub.AccelerometerConfiguration;

public class Accelerometer {
	private AccelerometerConfiguration config;

	public void setConfiguration(AccelerometerConfiguration config){
		this.config = config;
	}
	
	public AccelerometerConfiguration getConfiguration(){
		return config;
	}
}
