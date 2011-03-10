package com.buglabs.bug.module.lcd;

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

import com.buglabs.bug.accelerometer.pub.AccelerometerConfiguration;
import com.buglabs.bug.accelerometer.pub.AccelerometerSample;
import com.buglabs.bug.accelerometer.pub.Constants;
import com.buglabs.bug.module.lcd.accelerometer.LCDAccelerometerSampleProvider;
import com.buglabs.bug.module.lcd.pub.ILCDModuleControl;
import com.buglabs.bug.module.lcd.pub.IML8953Accelerometer;
import com.buglabs.bug.module.lcd.pub.IModuleDisplay;
import com.buglabs.bug.module.motion.pub.AccelerationWS;
import com.buglabs.bug.module.pub.IModlet;
import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleLEDController;
import com.buglabs.module.IModuleProperty;
import com.buglabs.module.ModuleProperty;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.util.IStreamMultiplexerListener;
import com.buglabs.util.LogServiceUtil;
import com.buglabs.util.StreamMultiplexerEvent;

public class LCDModlet implements IModlet, IModuleControl, ILCDModuleControl, IModuleDisplay, IStreamMultiplexerListener, IModuleLEDController, IML8953Accelerometer {

	private final BundleContext context;

	private final int slotId;

	private final String moduleName;

	private final LogService logService;

	private ServiceRegistration lcdModRef;

	private ServiceRegistration moduleDisplayServReg;
	
	private Accelerometer accDevice;

	private AccelerometerRawFeed acceld;
	
	private AccelerometerControl accControl;
	
	private Random rng;
	
	private Frame frame;

	private List activeFrames; // list that contains frames that were initialized
	
	private final int LCD_WIDTH = 320;
	private final int LCD_HEIGHT = 200;

	/*
	private ServiceRegistration accRawFeedRef;

	private ServiceRegistration accSampleProvRef;

	private ServiceRegistration accSampleFeedRef;

	private ServiceRegistration accControlRef;
	*/
	
	private ServiceRegistration ledControllerRef;

	private ServiceRegistration moduleControlRef;

	private ServiceRegistration wsAccReg;

	private ServiceRegistration accelRef;

	public LCDModlet(BundleContext context, int slotId, String moduleName, LogService logService) {
		this.context = context;
		this.slotId = slotId;
		this.moduleName = moduleName;
		this.logService = logService;
		activeFrames = Collections.synchronizedList(new ArrayList());
		rng = new Random();
	}

	public String getModuleId() {
		return moduleName;
	}

	public void setup() throws Exception {
		accDevice = new Accelerometer();
		accDevice.setConfiguration(new AccelerometerConfiguration());
		accControl = new AccelerometerControl(accDevice);
	}

	public void start() throws Exception {
		Properties p = new Properties();
		p.put("Module", "Virtual LCD Module");

		moduleControlRef = context.registerService(IModuleControl.class.getName(), this, createBasicServiceProperties());
		lcdModRef = context.registerService(ILCDModuleControl.class.getName(), this, createBasicServiceProperties());
		ledControllerRef = context.registerService(IModuleLEDController.class.getName(), this, createBasicServiceProperties());
		accelRef = context.registerService(IML8953Accelerometer.class.getName(), this, createBasicServiceProperties());
		
		Dictionary props = new Hashtable();
		props.put("width", new Integer(LCD_WIDTH));
		props.put("height", new Integer(LCD_HEIGHT));

		moduleDisplayServReg = context.registerService(IModuleDisplay.class.getName(), this, props);
		
		configureAccelerometer();
		
		InputStream data = getAccelerometerSamples();
		
		acceld = new AccelerometerRawFeed(data,accControl);
		
		/*
		accRawFeedRef = context.registerService(IAccelerometerRawFeed.class.getName(), acceld, createBasicServiceProperties());
		IAccelerometerSampleProvider asp = new AccelerometerSampleProvider(acceld, accDevice);
		accSampleProvRef = context.registerService(IAccelerometerSampleProvider.class.getName(), asp, createBasicServiceProperties());
		accSampleFeedRef = context.registerService(IAccelerometerSampleFeed.class.getName(), acceld, createBasicServiceProperties());
		accControlRef = context.registerService(IAccelerometerControl.class.getName(), accControl, createBasicServiceProperties());
		*/
		
		LCDAccelerometerSampleProvider accsp = new LCDAccelerometerSampleProvider(acceld);
		
		AccelerationWS accWs = new AccelerationWS(accsp, LogServiceUtil.getLogService(context));
		wsAccReg = context.registerService(PublicWSProvider.class.getName(), accWs, null);  		
	}
	private Properties createBasicServiceProperties() {
		Properties p = new Properties();
		p.put("Provider", this.getClass().getName());
		p.put("Slot", Integer.toString(slotId));
		return p;
	}
	
	private InputStream getAccelerometerSamples() throws IOException {
		String prop = System.getProperty(Constants.PROPERTY_ACCELEROMETER_LOG);
		
		InputStream is = null;

		if (prop != null && prop.length() > 0) {
			File propFile = new File(prop);
			if (propFile.exists()) {
				is = new FileInputStream(propFile);
			}
		}

		if (is == null) {
			is = LCDModlet.class.getResourceAsStream("accelerometer.out");
		}
		
		return is;
	}

	private void configureAccelerometer() {
		AccelerometerConfiguration config = accDevice.getConfiguration();
		config.setDelay((short)250);
		config.setDelayResolution((byte)5);
		config.setDelayMode((byte)1);
		config.setRun((byte) 1);
		
		accDevice.setConfiguration(config);
	}

	public void stop() throws Exception {
		wsAccReg.unregister();
		lcdModRef.unregister();
		accelRef.unregister();
		moduleDisplayServReg.unregister();
		ledControllerRef.unregister();
		moduleControlRef.unregister();
		/*
		accRawFeedRef.unregister();
		accSampleProvRef.unregister();
		accSampleFeedRef.unregister();
		accControlRef.unregister();	
		*/
		
		// upon stopping of the Modlet all
		// frames need to disposed of.
		for(Iterator iterator = activeFrames.iterator(); iterator.hasNext();) {
			Frame frame = (Frame) iterator.next();
			if(frame != null)
				frame.dispose();
		}
	}

	public String getModuleName() {
		return moduleName;
	}

	public List getModuleProperties() {
		// TODO this is demo mode!
		List properties = new ArrayList();

		properties.add(new ModuleProperty("Slot", "" + slotId));
		properties.add(new ModuleProperty("Width",String.valueOf(LCD_WIDTH)));
		properties.add(new ModuleProperty("Height", String.valueOf(LCD_HEIGHT)));

		return properties;
	}

	public boolean setModuleProperty(IModuleProperty property) {
		return false;
	}

	public int getSlotId() {
		return slotId;
	}

	/**
	 * @return Returns AWT Frame
	 */
	public Frame getFrame() {
		frame = new Frame();
		frame.setSize(LCD_WIDTH,LCD_HEIGHT);
		frame.setResizable(false);

		activeFrames.add(frame);
		return frame;
	}

	public void streamNotification(StreamMultiplexerEvent event) {
		switch(event.type) {
		case StreamMultiplexerEvent.EVENT_STREAM_ADDED:
			if(event.numberOfStreams == 1) {
				//accDevice.ioctl_BMI_MDACC_ACCELEROMETER_RUN();
			}
			break;
		case StreamMultiplexerEvent.EVENT_STREAM_REMOVED:
			if(event.numberOfStreams == 0) {
				//accDevice.ioctl_BMI_MDACC_ACCELEROMETER_STOP();
			}
			break;
		default:
			break;
		}
	}

	public int disable() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int enable() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getStatus() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int setBackLight(int val) throws IOException {
		if(val < 0 || val > 7){
			logService.log(LogService.LOG_ERROR, "Illegal backlight intensity value. Range is 0-7");
			throw new IOException("Illegal backlight intensity value. Range is 0-7");
		}
		logService.log(LogService.LOG_INFO, "Backlight intensity is set to " + val);
		return 0;
	}

	public int setLEDGreen(boolean state) throws IOException {
		if(state)
			logService.log(LogService.LOG_INFO, "Green LED is on");
		else
			logService.log(LogService.LOG_INFO, "Green LED is off");
		return 0;
	}

	public int setLEDRed(boolean state) throws IOException {
		if(state)
			logService.log(LogService.LOG_INFO, "Red LED is on");
		else
			logService.log(LogService.LOG_INFO, "Red LED is off");
		return 0;
	}

	public int LEDGreenOff() throws IOException {
		return setLEDGreen(false);
	}

	public int LEDGreenOn() throws IOException {
		return setLEDGreen(true);
	}

	public int LEDRedOff() throws IOException {
		return setLEDRed(false);
	}

	public int LEDRedOn() throws IOException {
		return setLEDRed(true);
	}

	public int setBlackLight(int val) throws IOException {
		return setBackLight(val);
	}

	public int resume() throws IOException {
		throw new IOException("LCDModlet resume call is not implemented for Virtual BUG");
	}

	public int suspend() throws IOException {
		throw new IOException("LCDModlet suspend call is not implemented for Virtual BUG");
	}

	
	public short readX() throws IOException {	
		return (short) rng.nextInt();
	}

	
	public short readY() throws IOException {
		return (short) rng.nextInt();
	}

	
	public short readZ() throws IOException {
		return (short) rng.nextInt();
	}

	
	public AccelerometerSample readSample() throws IOException {	
		return new AccelerometerSample(rng.nextFloat(), rng.nextFloat(), rng.nextFloat());
	}
}
