package com.buglabs.bug.module.vonhippel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;

import com.buglabs.bug.module.pub.IModlet;
import com.buglabs.bug.module.vonhippel.pub.IVonHippelModuleControl;
import com.buglabs.bug.module.vonhippel.pub.IVonHippelSerialPort;
import com.buglabs.bug.module.vonhippel.pub.VonHippelWS;
import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleLEDController;
import com.buglabs.module.IModuleProperty;
import com.buglabs.module.ModuleProperty;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.util.ConfigAdminUtil;
import com.buglabs.util.RemoteOSGiServiceConstants;

public class VonHippelModlet implements  IModlet, IModuleControl {

	private 	static final String STREAM_PATH_KEY= "IO Stream Path";
	private		static final String DEFAULT_STREAM_PATH	= "/dev/ttyUSB0";
	protected 	static final String PROPERTY_MODULE_NAME 	= "moduleName";
	public 		static final String MODULE_ID 				= "0007";
	
	private final String moduleId;
	private BundleContext context;
	private boolean deviceOn = true;
	private int slotId;

	private ServiceRegistration moduleRef;
	private ServiceRegistration vonHippelControlRef;
	private ServiceRegistration ledRef;
	private ServiceRegistration vonHippelSerial;

	private ServiceRegistration wsVonHippelReg;

	private LogService logService;

	/**
	 * gets the string serial port from the config admin
	 * 
	 * @return String serial port
	 */
	private String getIOStreamPath() {
		String result = DEFAULT_STREAM_PATH;
		ConfigurationAdmin ca = getConfigurationAdmin();
		if (ca == null) return result;
		try {
			Configuration config = ca.getConfiguration(getModuleName());
			Object configValue = config.getProperties().get(STREAM_PATH_KEY);
			if (configValue != null) {
				result = (String) configValue;
			}
		} catch (IOException e) {
			logService.log(LogService.LOG_ERROR, "Problem retrieving data from cm:", e);
		}
		return result;
	}
	
	/**
	 * Sets up a configuration admin property for the serial port
	 */
	private void setIOStreamPath() {
		ConfigurationAdmin ca = getConfigurationAdmin();
		if (ca == null) return;

		// try to get path string - if not, set it
		try {
			// get the config
			Configuration config = ca.getConfiguration(getModuleName());
			Dictionary properties = ConfigAdminUtil.getPropertiesSafely(config);
			
			// get or set the comm path
			Object configValue = properties.get(STREAM_PATH_KEY);
			if (configValue == null) {
				properties.put(STREAM_PATH_KEY, DEFAULT_STREAM_PATH);
			}
			config.update(properties);
		} catch (IOException e) {
			logService.log(LogService.LOG_ERROR, "Problem retrieving data from cm:", e);
		}
	}
	
	/**
	 *  simply returns the configuration admin
	 *  
	 * @return
	 */
	private ConfigurationAdmin getConfigurationAdmin() {
		// get a reference to a configuration admin object
		ServiceReference sr 
			= context.getServiceReference(ConfigurationAdmin.class.getName());
		if (sr == null) return null;
		return (ConfigurationAdmin) context.getService(sr);		
	}
		
	public VonHippelModlet(BundleContext context, int slotId, String moduleId, LogService logService) {
		this.context = context;
		this.slotId = slotId;
		this.moduleId = moduleId;
		this.logService = logService;
	}

	public void start() throws Exception {
		setIOStreamPath();
		VonHippelModuleControl vonHippelModuleControl = 
			new VonHippelModuleControl(logService, getIOStreamPath());
		vonHippelControlRef = context.registerService(
				IVonHippelModuleControl.class.getName(), vonHippelModuleControl, createRemotableProperties(null));
		vonHippelSerial = context.registerService(
				IVonHippelSerialPort.class.getName(), vonHippelModuleControl, createRemotableProperties(null));
		moduleRef = context.registerService(IModuleControl.class.getName(), this, createBasicServiceProperties());
		ledRef = context.registerService(
				IModuleLEDController.class.getName(), vonHippelModuleControl, createRemotableProperties(null));
		VonHippelWS vhWS = new VonHippelWS();
		wsVonHippelReg = context.registerService(PublicWSProvider.class.getName(), vhWS, null);
	}

	public void stop() throws Exception {

		wsVonHippelReg.unregister();

		//TODO: Throw exception at some point if we encounter a failure
		moduleRef.unregister();
		ledRef.unregister();
		vonHippelSerial.unregister();
		vonHippelControlRef.unregister();
	}

	private Properties createBasicServiceProperties() {
		Properties p = new Properties();
		p.put("Provider", this.getClass().getName());
		p.put("Slot", Integer.toString(slotId));
		return p;
	}
	
	/**
	 * @return A dictionary with R-OSGi enable property.
	 */
	private Dictionary createRemotableProperties(Dictionary ht) {
		if (ht == null) {
			ht = new Hashtable();
		}
		
		ht.put(RemoteOSGiServiceConstants.R_OSGi_REGISTRATION, "true");
		
		return ht;
	}

	public List getModuleProperties() {
		List properties = new ArrayList();

		properties.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
		properties.add(new ModuleProperty("Slot", "" + slotId));
		properties.add(new ModuleProperty("State", Boolean.toString(deviceOn), "Boolean", true));

		return properties;
	}

	public boolean setModuleProperty(IModuleProperty property) {
		if (!property.isMutable()) {
			return false;
		}
		if (property.getName().equals("State")) {
			deviceOn = Boolean.valueOf((String) property.getValue()).booleanValue();
			return true;
		}
		return false;
	}

	public String getModuleName() {
		return moduleId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public int getSlotId() {
		return slotId;
	}

	public void setup() throws Exception {
	}

	public int resume() throws IOException {
		throw new IOException("VonHippelModlet resume call is not implemented for Virtual BUG");
		
	}

	public int suspend() throws IOException {
		throw new IOException("VonHippelModlet suspend call is not implemented for Virtual BUG");
	}

}
