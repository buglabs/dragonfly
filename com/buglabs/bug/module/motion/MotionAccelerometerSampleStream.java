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

	/*public AccelerometerSample readSample() throws IOException {
		byte[] data = new byte[6];			
		short[] sample = null;
		
		System.out.println("tring to read data...");
		//int result = is.read(data);
		int result = read(data);
		if(result == data.length) {
			sample = new short[3];

			for(int i = 0; i < sample.length; ++i) {
				short byte0 = (short) (0x00FF & (short)data[i*2 + 1]);
				short byte1 = (short) (0x00FF & (short)data[i*2]);

				sample[i] = (short) (byte1 >> 6);
				sample[i] +=  (byte0 << 2);
			}
		}
		
		return new AccelerometerSample(convertToGs(sample[2]),
									   convertToGs(sample[1]),
									   convertToGs(sample[0]));
	}*/
	
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
