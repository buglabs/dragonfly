package com.buglabs.bug.module.gps;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

import com.buglabs.bug.bmi.api.BMIModuleProperties;
import com.buglabs.bug.bmi.api.IModlet;
import com.buglabs.bug.bmi.api.IModletFactory;
import com.buglabs.bug.bmi.sysfs.BMIDevice;
import com.buglabs.util.osgi.LogServiceUtil;

public class GPSActivator implements BundleActivator, IModletFactory {
	private static GPSActivator inst;

	private ServiceRegistration sr;

	private LogService logService;

	private BundleContext context;

	public void start(BundleContext context) throws Exception {
		this.context = context;
		Dictionary dict = new Hashtable();
		dict.put("Modlet Provider", getName());
		dict.put("Module", getModuleId());
		inst = this;
		
		sr = context.registerService(IModletFactory.class.getName(), this, dict);
		logService = LogServiceUtil.getLogService(context);
	}

	public void stop(BundleContext context) throws Exception {
		sr.unregister();
	}

	public String getModuleId() {
		return "GPS";
	}

	public String getName() {
		return "com.buglabs.bug.module.gps";
	}

	public String getVersion() {
		return "3.0.0";
	}

	public static GPSActivator getInstance() {
		return inst;
	}

	public BundleContext getBundleContext() {	
		return context;
	}

	@Override
	public IModlet createModlet(BundleContext context, int slotId, BMIDevice properties) {
		return new GPSModlet(context, slotId, getModuleId(), logService);
	}
}
