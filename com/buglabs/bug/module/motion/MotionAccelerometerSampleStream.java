package com.buglabs.bug.module.motion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.buglabs.bug.accelerometer.pub.AccelerometerConfiguration;
import com.buglabs.bug.accelerometer.pub.AccelerometerSample;
import com.buglabs.bug.accelerometer.pub.AccelerometerSampleStream;
import com.buglabs.bug.accelerometer.pub.IAccelerometerConfigurationListener;
import com.buglabs.util.StringUtil;

public class MotionAccelerometerSampleStream extends AccelerometerSampleStream implements IAccelerometerConfigurationListener{
	
	private AccelerometerConfiguration config;
	private AccelerometerControl accControl;
	private BufferedReader buffer;

	public MotionAccelerometerSampleStream(InputStream is, AccelerometerControl accControl) {
		super(is);
		this.accControl = accControl;
		this.config = accControl.getConfiguration();
		InputStreamReader isr = new InputStreamReader(is);
		buffer = new BufferedReader(isr);
		accControl.registerListener(this);
	}

	public void close() throws IOException {
		super.close();
		accControl.unregisterListener(this);
	}

	public AccelerometerSample readSample() throws IOException {
		if(buffer.ready()){
			String line = buffer.readLine();
			if(line != null){
				String[] split = StringUtil.split(line, ",");
				try {
					Thread.sleep(config.getDelay());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return new AccelerometerSample(Float.parseFloat(split[0]),
											   Float.parseFloat(split[1]),
											   Float.parseFloat(split[2]));
			}
			else{
				is.close();
			}
		}
		return null;
	}

	private float convertToGs(short s) {
		float mVperBit = 2900.0f/1024.0f;
		short scale_factors[] = {421, 316, 158, 105};
		float result = 0;
		synchronized(config) {
			result = (((float)(s * mVperBit) - 1450)) / scale_factors[config.getSensitivity()];
		}
		
		return result;
	}

	public void configurationChanged(AccelerometerConfiguration c) {
		synchronized (config) {
			config = c;
		}
	}
}
