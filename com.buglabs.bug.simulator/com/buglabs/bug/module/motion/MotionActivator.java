package com.buglabs.bug.module.motion;

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

/**
 * @deprecated This module is not supported in BUG 2.0 *
 */
public class MotionActivator implements BundleActivator, IModletFactory {
	private BundleContext context;

	private ServiceRegistration sr;

	private LogService logService;

	private static MotionActivator instance;

	public MotionActivator(){
		instance = this;
	}
	
	public void start(BundleContext context) throws Exception {
		this.context = context;
		
		Dictionary dict = new Hashtable();
		dict.put("Modlet Provider", getName());
		dict.put("Module", getModuleId());
		
		sr = context.registerService(IModletFactory.class.getName(), this, dict);
		
		logService = LogServiceUtil.getLogService(context);
	}

	public void stop(BundleContext context) throws Exception {
		sr.unregister();
	}
	
	public static MotionActivator getDefault(){
		return instance;
	}
	
	public BundleContext getBundleContext(){
		return context;
	}
	
	public String getModuleId() {
		return "MOTION";
	}

	public String getName() {
		return "com.buglabs.bug.module.motion";
	}

	public String getVersion() {
		return "3.0.0";
	}

	@Override
	public IModlet createModlet(BundleContext context, int slotId, BMIDevice properties) {
		return new MotionModlet(context, slotId, getModuleId(),logService);
	}
}
