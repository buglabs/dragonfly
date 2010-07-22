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

public class Activator implements BundleActivator, IModletFactory {
	private BundleContext context;

	private ServiceRegistration sr;

	private LogService logService;

	private static Activator instance;

	public Activator(){
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
	
	public static Activator getDefault(){
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
		return (String) context.getBundle().getHeaders().get("Bug-Module-Id");
	}

	public String getName() {
		return (String) context.getBundle().getHeaders().get("Bundle-SymbolicName");
	}

	public String getVersion() {
		return (String) context.getBundle().getHeaders().get("Bundle-Version");
	}

}
