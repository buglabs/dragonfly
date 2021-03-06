/* Copyright (c) 2007, 2008 Bug Labs, Inc.
 * All rights reserved.
 *   
 * This program is free software; you can redistribute it and/or  
 * modify it under the terms of the GNU General Public License version  
 * 2 only, as published by the Free Software Foundation.   
 *   
 * This program is distributed in the hope that it will be useful, but  
 * WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  
 * General Public License version 2 for more details (a copy is  
 * included at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).   
 *   
 * You should have received a copy of the GNU General Public License  
 * version 2 along with this work; if not, write to the Free Software  
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  
 * 02110-1301 USA   
 *
 */
package com.buglabs.bug.simulator;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.knapsack.shell.pub.IKnapsackCommand;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import com.buglabs.bug.base.SupportServlet;
import com.buglabs.bug.base.VBUGSupportInfo;
import com.buglabs.bug.base.pub.IBUG20BaseControl;
import com.buglabs.bug.base.pub.ITimeProvider;
import com.buglabs.bug.bmi.PipeReader;
import com.buglabs.bug.bmi.api.IModlet;
import com.buglabs.bug.bmi.api.IModletFactory;
import com.buglabs.bug.bmi.pub.Manager;
import com.buglabs.bug.buttons.IButtonEventProvider;
import com.buglabs.bug.module.camera.CameraActivator;
import com.buglabs.bug.module.gps.GPSActivator;
import com.buglabs.bug.module.lcd.LCDActivator;
import com.buglabs.bug.module.motion.MotionActivator;
import com.buglabs.bug.module.sierra.GSMActivator;
import com.buglabs.bug.module.video.VideoActivator;
import com.buglabs.bug.module.vonhippel.VHActivator;
import com.buglabs.bug.simulator.controller.Server;
import com.buglabs.bug.simulator.ui.ShellIOThread;
import com.buglabs.bug.simulator.ui.SimulatorModuleCommands;
import com.buglabs.support.SupportInfoTextFormatter;
import com.buglabs.support.SupportInfoXMLFormatter;
import com.buglabs.util.osgi.LogServiceUtil;
import com.buglabs.util.osgi.ServiceTrackerUtil;
import com.buglabs.util.osgi.ServiceTrackerUtil.ManagedRunnable;

/**
 * This bundle offers base unit features to the runtime, such as date/time and
 * power information.
 * 
 * Note: This is a emulator of the base bundle specifically tailored for BUG
 * emulation. Please see com.buglabs.bug.base for hardware specific version of
 * the bundle.
 * 
 * @author kgilmer
 * 
 */
public class Activator implements BundleActivator, ITimeProvider, ServiceListener {

	private static final int BUG_SIMULATOR_CONTROLLER_PORT = 8093;

	private static final String INFO_SERVLET_ALIAS = "/support";

	private static final String INFO_SERVLET_HTML_ALIAS = "/support.html";

	private ServiceRegistration timeReg;

	private ServiceTracker httpST;

	protected HttpService httpService;

	private PipeReader pipeReader;

	private String pipeFilename;

	private static LogService logService;

	private Map modletFactories;

	private Map activeModlets;

	private BundleContext context;

	private GPSActivator gpsActivator;

	private ServiceRegistration shellCommandReg;

	private GSMActivator gsmActivator;

	private Server controllerServer;

	private MotionActivator motionActivator;

	private LCDActivator lcdActivator;

	private VHActivator vhActivator;

	private CameraActivator cameraActivator;

	private ServiceRegistration baseControlReg;

	private VideoActivator videoActivator;

	private ShellIOThread shellThread;

	private List<ServiceRegistration> btnServices;

	public void start(final BundleContext context) throws Exception {
		// Basic setup ********************************************
		this.context = context;
		Activator.logService = LogServiceUtil.getLogService(context);

		// com.buglabs.bug.base services **************************
		timeReg = context.registerService(ITimeProvider.class.getName(), this, null);
		httpST = ServiceTrackerUtil.openServiceTracker(context, new ManagedRunnable() {
			
			@Override
			public void shutdown() {
				if (httpService != null) {
					httpService.unregister(INFO_SERVLET_ALIAS);
				}
			}
			
			@Override
			public void run(Map<String, Object> services) {
				httpService = (HttpService) services.get(HttpService.class.getName());
				try {
					// xml servlet
					httpService.registerServlet(INFO_SERVLET_ALIAS, new SupportServlet(new VBUGSupportInfo(context), new SupportInfoXMLFormatter()), null, null);
					// html servlet
					httpService.registerServlet(INFO_SERVLET_HTML_ALIAS, new SupportServlet(new VBUGSupportInfo(context), new SupportInfoTextFormatter()), null, null);
				} catch (ServletException e) {
					logService.log(LogService.LOG_ERROR, "Unable to register info servlet.", e);
				} catch (NamespaceException e) {
					logService.log(LogService.LOG_ERROR, "Unable to register info servlet.", e);
				}
			}
		}, HttpService.class.getName());

		// com.buglabs.bug.bmi services ********************************
		modletFactories = new Hashtable();
		activeModlets = new Hashtable();

		context.addServiceListener(this, "(" + Constants.OBJECTCLASS + "=" + IModletFactory.class.getName() + ")");
		registerExistingModletFactories(context);

		// Initialize the bmi manager.
		Manager bmiManager = Manager.getManager(context, logService, modletFactories, activeModlets);

		// com.buglabs.bug.module.gps ***********************
		gpsActivator = new GPSActivator();
		gpsActivator.start(context);

		// com.buglabs.bug.module.sierra ***********************
		// gsmActivator = new GSMActivator();
		// gsmActivator.start(context);

		// com.buglabs.bug.module.lcd ***********************
		lcdActivator = new LCDActivator();
		lcdActivator.start(context);

		// com.buglabs.bug.module.vonhippel ***********************
		vhActivator = new VHActivator();
		vhActivator.start(context);

		// com.buglabs.bug.module.camera ***********************
		cameraActivator = new CameraActivator();
		cameraActivator.start(context);
		
		// com.buglabs.bug.module.camera ***********************
		videoActivator = new VideoActivator();
		videoActivator.start(context);

		// Module Controller *************************************
		try {
			controllerServer = Server.startServer(BUG_SIMULATOR_CONTROLLER_PORT, logService, context);
			
			Dictionary<String, String> d = new Hashtable<String, String>();
			d.put("bug.base.version", "2.0");
			baseControlReg = context.registerService(IBUG20BaseControl.class.getName(), controllerServer, d);
		} catch (BindException e) {
			logService.log(LogService.LOG_ERROR, "BUG Simulator Controller unable to start.  Another process is using it's port: " + BUG_SIMULATOR_CONTROLLER_PORT);
		}		
		
		if (System.getProperty("org.knapsack.shell.port") != null) {
			shellThread = new ShellIOThread(Integer.parseInt(System.getProperty("org.knapsack.shell.port")));
			shellThread.start();
		}
		
		ShellButtonAdapter userBtn = new ShellButtonAdapter("user");
		ShellButtonAdapter powerBtn = new ShellButtonAdapter("power");
		
		btnServices = new ArrayList<ServiceRegistration>();
		btnServices.add(context.registerService(IButtonEventProvider.class.getName(), userBtn, getUserButtonProperties()));
		btnServices.add(context.registerService(IKnapsackCommand.class.getName(), userBtn, null));
		
		btnServices.add(context.registerService(IButtonEventProvider.class.getName(), powerBtn, getPowerButtonProperties()));
		btnServices.add(context.registerService(IKnapsackCommand.class.getName(), powerBtn, null));		
		
		SimulatorModuleCommands smc = new SimulatorModuleCommands(bmiManager);
		
		for (IKnapsackCommand cmd : smc.getCommands())
			btnServices.add(context.registerService(IKnapsackCommand.class.getName(), cmd, null));
	}

	private Dictionary<String, String> getPowerButtonProperties() {
		Dictionary<String, String> d = new Hashtable<String, String>();
		d.put("Provider", this.getClass().getName());
		d.put("Button", "Power");
		return d;
	}

	private Dictionary<String, String> getUserButtonProperties() {
		Dictionary<String, String> d = new Hashtable<String, String>();
		d.put("Provider", this.getClass().getName());
		d.put("Button", "User");
		return d;
	}
	
	public void stop(BundleContext context) throws Exception {			
		for (ServiceRegistration sr : btnServices)
			sr.unregister();
			
		if (shellThread != null) {
			shellThread.shutdown();
		}
		
		if (controllerServer != null) {
			controllerServer.shutdown();
		}

		if (shellCommandReg != null) {
			shellCommandReg.unregister();
		}

		if (baseControlReg != null) {
			baseControlReg.unregister();
		}

		if (cameraActivator != null) {
			cameraActivator.stop(context);
		}

		if (vhActivator != null) {
			vhActivator.stop(context);
		}

		if (lcdActivator != null) {
			lcdActivator.stop(context);
		}

		if (motionActivator != null) {
			motionActivator.stop(context);
		}

		if (gpsActivator != null) {
			gpsActivator.stop(context);
		}

		if (gsmActivator != null) {
			gsmActivator.stop(context);
		}

		if (videoActivator != null) {
			videoActivator.stop(context);
		}

		try {
			if (httpST != null) {
				httpST.close();
			}
		} catch (Exception e) {
			/* don't show the scary red error when shutting down */
			if (logService != null) {
				logService.log(LogService.LOG_WARNING, "Error stopping bundle " + this.getClass().getName() + ".");
			}
		}
		timeReg.unregister();
		
		// com.buglabs.bug.bmi ****************************
		context.removeServiceListener(this);
		stopModlets(activeModlets);
		if (pipeReader != null) {
			pipeReader.cancel();
			pipeReader.interrupt();
			logService.log(LogService.LOG_INFO, "Deleting pipe " + pipeFilename);
			destroyPipe(new File(pipeFilename));
		}
		modletFactories.clear();
		activeModlets.clear();
	}

	private void registerExistingModletFactories(BundleContext context2) throws InvalidSyntaxException {
		ServiceReference sr[] = context2.getServiceReferences(null, "(" + Constants.OBJECTCLASS + "=" + IModletFactory.class.getName() + ")");

		if (sr != null) {
			for (int i = 0; i < sr.length; ++i) {
				registerService(sr[i], ServiceEvent.REGISTERED);
			}
		}
	}

	public Date getTime() {
		return Calendar.getInstance().getTime();
	}

	/**
	 * Stop all active modlets.
	 * 
	 * @param activeModlets
	 */
	private void stopModlets(Map modlets) {
		for (Iterator i = modlets.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();

			List modl = (List) modlets.get(key);

			for (Iterator j = modl.iterator(); j.hasNext();) {
				IModlet m = (IModlet) j.next();

				try {
					m.stop();
				} catch (Exception e) {
					logService.log(LogService.LOG_ERROR, "Error occured while stopping " + m.getModuleId() + ": " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Deletes a file
	 * 
	 * @param file
	 * @throws IOException
	 */
	private void destroyPipe(File file) throws IOException {
		if (!file.delete()) {
			throw new IOException("Unable to delete file " + file.getAbsolutePath());
		}
		logService.log(LogService.LOG_INFO, "Deleted " + file.getAbsolutePath());
	}

	public void serviceChanged(ServiceEvent event) {
		ServiceReference sr = event.getServiceReference();

		registerService(sr, event.getType());
	}

	private void registerService(ServiceReference sr, int eventType) {
		IModletFactory factory = (IModletFactory) context.getService(sr);

		validateFactory(factory);

		switch (eventType) {
		case ServiceEvent.REGISTERED:
			if (!modletFactories.containsKey(factory.getModuleId())) {
				modletFactories.put(factory.getModuleId(), new ArrayList());
			} else {
				logService.log(LogService.LOG_WARNING, "IModletFactory " + factory.getName() + " is already registered, ignoring.");
			}

			List ml = (List) modletFactories.get(factory.getModuleId());

			if (!ml.contains(factory)) {
				ml.add(factory);
			}
			logService.log(LogService.LOG_INFO, "Added modlet factory " + factory.getName() + " (" + factory.getModuleId() + ") to map.");

			// Discovery Mode needs to know of all services a BUG contains. This
			// causes all available modlets to be created and started.
			if (context.getProperty("com.buglabs.bug.discoveryMode") != null && context.getProperty("com.buglabs.bug.discoveryMode").equals("true")) {
				try {
					createModlets(factory);
				} catch (Exception e) {
					logService.log(LogService.LOG_ERROR, "Unable to start modlet in discovery mode: " + e.getMessage());
				}
			}

			break;
		case ServiceEvent.UNREGISTERING:
			if (modletFactories.containsKey(factory.getModuleId())) {
				List ml2 = (List) modletFactories.get(factory.getModuleId());

				if (ml2.contains(factory)) {
					ml2.remove(factory);
				}
			}
			logService.log(LogService.LOG_INFO, "Removed modlet factory " + factory.getName() + " to map.");
			break;
		}
	}

	private void validateFactory(IModletFactory factory) {
		if (isEmpty(factory.getModuleId())) {
			throw new RuntimeException("IModletFactory has empty Module ID.");
		}

		if (isEmpty(factory.getName())) {
			throw new RuntimeException("IModletFactory has empty Name.");
		}
	}

	private boolean isEmpty(String element) {
		return element == null || element.length() == 0;
	}

	private void createModlets(IModletFactory factory) throws Exception {
		IModlet modlet = factory.createModlet(context, 0, null);
		modlet.setup();
		modlet.start();
	}

	protected Map getModletFactories() {
		return modletFactories;
	}

	protected Map getActiveModlets() {
		return activeModlets;
	}

	public static LogService getLogService() {
		return logService;
	}	
}