package com.buglabs.bug.module.motion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

import com.buglabs.bug.accelerometer.pub.AccelerometerConfiguration;
import com.buglabs.bug.accelerometer.pub.Constants;
import com.buglabs.bug.accelerometer.pub.IAccelerometerControl;
import com.buglabs.bug.accelerometer.pub.IAccelerometerRawFeed;
import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleFeed;
import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleProvider;
import com.buglabs.bug.bmi.api.IModlet;
import com.buglabs.bug.dragonfly.module.IModuleControl;
import com.buglabs.bug.dragonfly.module.IModuleLEDController;
import com.buglabs.bug.dragonfly.module.IModuleProperty;
import com.buglabs.bug.dragonfly.module.ModuleProperty;
import com.buglabs.bug.module.motion.pub.AccelerationWS;
import com.buglabs.bug.module.motion.pub.IMDACCModuleControl;
import com.buglabs.bug.module.motion.pub.IMotionSubject;
import com.buglabs.bug.module.motion.pub.InputEvent;
import com.buglabs.services.ws.PublicWSProvider;

 
/**
 * @deprecated This module is not supported in BUG 2.0 *
 */
public class MotionModlet implements IModlet, IMDACCModuleControl, IModuleControl, IModuleLEDController {

	private BundleContext context;

	private int slotId;

	private String moduleName;

	private ServiceRegistration motionObserverRef;

	private ServiceRegistration moduleRef;

	private ServiceRegistration wsRef;
	
	private Accelerometer accDevice;

	private AccelerometerRawFeed acceld;

	private ServiceRegistration accRawFeedRef;

	private ServiceRegistration accSampleProvRef;

	private ServiceRegistration accSampleFeedRef;

	private AccelerometerControl accControl;

	private ServiceRegistration accControlRef;

	private ServiceRegistration wsAccRef;

	private ServiceRegistration mdaccRef;

	private ServiceRegistration lcdRef;

	private LogService logService;

	private static final String PROPERTY_MODULE_NAME = "moduleName";
	
	public static final String PROPERTY_ACCELEROMETER_LOG = "com.buglabs.bug.emulator.module.accelerometer.log";

	public MotionModlet(BundleContext context, int slotId, String moduleName, LogService logService) {
		this.context = context;
		this.slotId = slotId;
		this.moduleName = moduleName;
		this.logService = logService;
	}

	public void setup() throws Exception {
		accDevice = new Accelerometer();
		accDevice.setConfiguration(new AccelerometerConfiguration());
		accControl = new AccelerometerControl(accDevice);
	}

	public void start() throws Exception {
		IMotionSubject motionSubject = new MotionSubject();
		motionObserverRef = context.registerService(IMotionSubject.class.getName(), motionSubject, createBasicServiceProperties());
		moduleRef = context.registerService(IModuleControl.class.getName(), this, null);
		lcdRef = context.registerService(IModuleLEDController.class.getName(), this, createBasicServiceProperties());

		MotionWS motionWS = new MotionWS();
		motionSubject.register(motionWS);
		wsRef = context.registerService(PublicWSProvider.class.getName(), motionWS, null);
		
		configureAccelerometer();
		
		InputStream data = getAccelerometerSamples();
		
		acceld = new AccelerometerRawFeed(data,accControl);
		
		accRawFeedRef = context.registerService(IAccelerometerRawFeed.class.getName(), acceld, createBasicServiceProperties());
		IAccelerometerSampleProvider asp = new AccelerometerSampleProvider(acceld, accDevice);
		accSampleProvRef = context.registerService(IAccelerometerSampleProvider.class.getName(), asp, createBasicServiceProperties());
		accSampleFeedRef = context.registerService(IAccelerometerSampleFeed.class.getName(), acceld, createBasicServiceProperties());
		accControlRef = context.registerService(IAccelerometerControl.class.getName(), accControl, createBasicServiceProperties());
		AccelerationWS accWs = new AccelerationWS(asp, logService);
		wsAccRef = context.registerService(PublicWSProvider.class.getName(), accWs, null); 
		
		mdaccRef = context.registerService(IMDACCModuleControl.class.getName(), this, createBasicServiceProperties());	
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
			is = MotionModlet.class.getResourceAsStream("accelerometer.log");
		}
		
		return is;
	}

	private Properties createBasicServiceProperties() {
		Properties p = new Properties();
		p.put("Provider", this.getClass().getName());
		p.put("Slot", Integer.toString(slotId));
		return p;
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
		wsRef.unregister();
		wsAccRef.unregister();
		motionObserverRef.unregister();
		moduleRef.unregister();
		lcdRef.unregister();
		accRawFeedRef.unregister();
		accSampleFeedRef.unregister();
		accSampleProvRef.unregister();
		accControlRef.unregister();
		mdaccRef.unregister();
	}

	public List getModuleProperties() {
		List properties = new ArrayList();

		properties.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
		properties.add(new ModuleProperty("Slot", "" + slotId));

		return properties;
	}

	public boolean setModuleProperty(IModuleProperty property) {
		if (!property.isMutable()) {
			return false;
		}

		return false;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getModuleId() {
		return moduleName;
	}

	public int getSlotId() {
		return slotId;
	}

	public String getDescription() {
		return "Notifies observers of motion.";
	}

	public void notifyEventListeners(InputEvent[] events) {
		// TODO Auto-generated method stub
		
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

	public int resume() throws IOException {
		throw new IOException("MotionModlet resume call is not implemented for Virtual BUG");
	}

	public int suspend() throws IOException {
		throw new IOException("MotionModlet suspend call is not implemented for Virtual BUG");
	}
}
