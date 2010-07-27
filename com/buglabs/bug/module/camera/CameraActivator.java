package com.buglabs.bug.module.camera;

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

public class CameraActivator implements BundleActivator, IModletFactory {

	private BundleContext context;

	private ServiceRegistration sr;

	private LogService logService;

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

	public IModlet createModlet(BundleContext context, int slotId) {

		return new CameraModlet(context, slotId, getModuleId(), logService);
	}
	
	public IModlet createModlet(BundleContext context, int slotId, BMIModuleProperties properties) {
		return createModlet(context, slotId);
	}	

	public String getModuleId() {
		return "CAMERA";
	}

	public String getName() {
		return "com.buglabs.bug.emulator.module.camera";
	}

	public String getVersion() {
		return "2.0.0";
	}

}
