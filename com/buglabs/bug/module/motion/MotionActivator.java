package com.buglabs.bug.module.motion;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

import com.buglabs.bug.module.pub.BMIModuleProperties;
import com.buglabs.bug.module.pub.IModlet;
import com.buglabs.bug.module.pub.IModletFactory;
import com.buglabs.util.LogServiceUtil;

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

	public IModlet createModlet(BundleContext context, int slotId) {
		MotionModlet modlet = new MotionModlet(context, slotId, getModuleId(),logService);

		return modlet;
	}

	public IModlet createModlet(BundleContext context, int slotId, BMIModuleProperties properties) {
		return createModlet(context, slotId);
	}	
	
	public String getModuleId() {
		return "MOTION";
	}

	public String getName() {
		return "com.buglabs.bug.module.motion";
	}

	public String getVersion() {
		return "1.0.0";
	}

}
