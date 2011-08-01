package com.buglabs.bug.module.lcd;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

import com.buglabs.bug.bmi.api.IModlet;
import com.buglabs.bug.bmi.api.IModletFactory;
import com.buglabs.bug.bmi.sysfs.BMIDevice;
import com.buglabs.util.osgi.LogServiceUtil;

/**
 * Activator for the LCD bundle (simulator).
 * 
 * @author kgilmer
 * 
 */
public class LCDActivator implements BundleActivator, IModletFactory {

	private BundleContext context;

	private LogService logService;

	private ServiceRegistration sr;

	private static LCDActivator instance;

	public LCDActivator() {
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

	public static LCDActivator getInstance() {
		synchronized (instance) {
			return instance;
		}
	}

	public void stop(BundleContext context) throws Exception {
		try {
			sr.unregister();
		} catch (IllegalStateException e) {
			//Ignore this error
		}
	}

	public String getModuleId() {
		return "LCD";
	}

	public String getName() {
		return "com.buglabs.bug.module.lcd";
	}

	public String getVersion() {
		return "3.0.0";
	}

	public static LCDActivator getDefault() {
		return instance;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	@Override
	public IModlet createModlet(BundleContext context, int slotId, BMIDevice properties) {
		return new LCDModlet(context, slotId, getModuleId(), logService);
	}
}
