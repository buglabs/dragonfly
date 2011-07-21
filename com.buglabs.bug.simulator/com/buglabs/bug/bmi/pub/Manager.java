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
package com.buglabs.bug.bmi.pub;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;

import com.buglabs.bug.module.pub.IModlet;
import com.buglabs.bug.module.pub.IModletFactory;
import com.buglabs.util.BugBundleConstants;

/**
 * Manages logic of receiving messages from BMI and making changes to runtime.
 * runtime.
 * 
 * @author ken
 * 
 */
public class Manager {
	private static Manager ref;

	private final BundleContext context;

	private static LogService logService;

	private static Map modletFactories;

	private static Map activeModlets;

	private Manager(BundleContext context, LogService logService2, Map modletFactories, Map activeModlets) {
		Manager.logService = logService2;
		Manager.modletFactories = modletFactories;
		Manager.activeModlets = activeModlets;
		this.context = context;
	}

	synchronized static public Manager getManager(BundleContext context, LogService logService, Map modletFactories, Map activeModlets) {
		if (ref == null) {
			ref = new Manager(context, logService, modletFactories, activeModlets);
		}

		return ref;
	}

	/**
	 * @return
	 */
	synchronized public static Manager getManager() {
		return ref;
	}

	public List getAllModuleIds() {
		List l = new ArrayList();

		for (Iterator i = modletFactories.keySet().iterator(); i.hasNext();) {
			l.add(i.next());
		}

		return l;
	}

	/**
	 * This method is responsible for loading and starting any bundles that
	 * provide Modlets for given module type.
	 * 
	 * After bundle(s) are started, those bundles expose IModulet services. The
	 * BMI activator then listens for modlets. Upon new modlet creation, the
	 * setup is called.
	 * 
	 * @param msg
	 */
	public void processMessage(String msg) {
		logService.log(LogService.LOG_DEBUG, "processing: " + msg);
		BMIMessage message = new BMIMessage(msg);
		List ml;
		boolean startedBundles = false;
		try {
			if (message.parse()) {
				
				switch (message.getEvent()) {
				case BMIMessage.EVENT_INSERT:
					// first see if bundle is already installed.
					List matchingBundles = findLocalBundles(message.getModuleId());

					if (matchingBundles.size() > 0) {
						// check to see if any are loaded into the slot.
						for (Iterator i = matchingBundles.iterator(); i.hasNext();) {
							Bundle b = (Bundle) i.next();

							if (b.getState() != Bundle.ACTIVE) {
								b.start();
								logService.log(LogService.LOG_INFO, "Bundle " + b.getLocation()
										+ " has been started to provide Modlets for module " + message.getModuleId());
								startedBundles = true;
							}
						}
					} 

					// Now that all the bundles associated with given module ID
					// have been acquired and started, look for modlets to start.

					if (startedBundles) {
						// this is a hack to let all the modlets get added
						// before acquiring them.
						// TODO determine better strategy for collecting Modlets
						// after starting bundles.
						Thread.sleep(500);
					}

					ml = (List) modletFactories.get(message.getModuleId());

					if (ml != null) {
						for (Iterator i = ml.iterator(); i.hasNext();) {
							IModletFactory mf = (IModletFactory) i.next();

							// TODO we want to do some logic like get only the
							// latest version of a given modlet factory.
							IModlet m = mf.createModlet(context, message.getSlot());
							try {
								m.setup();
							} catch (Exception e) {
								logService.log(LogService.LOG_ERROR, "Unable to setup Modlet " + mf.getName() + ": " + e.getMessage());
								continue;
							}

							logService.log(LogService.LOG_INFO, "Created modlet from factory " + mf.getName() + "...");
							m.start();

							// Add this model to our map of running Modlets.
							if (!activeModlets.containsKey(m.getModuleId())) {
								activeModlets.put(m.getModuleId(), new ArrayList());
							}

							List am = (List) activeModlets.get(m.getModuleId());

							if (!am.contains(m)) {
								am.add(m);
							}
						}
					}

					break;
				case BMIMessage.EVENT_REMOVE:
					ml = (List) activeModlets.get(message.getModuleId());
					
					synchronized (ml) {
						List removalList = new ArrayList();

						if (ml != null) {
							for (Iterator i = ml.iterator(); i.hasNext();) {
								IModlet m = (IModlet) i.next();
								if (m.getSlotId() == message.getSlot()) {
									logService.log(LogService.LOG_INFO, "Stopping modlet " + m.getModuleId() + "...");
									m.stop();
									removalList.add(m);
								}
							}

							for (Iterator i = removalList.iterator(); i.hasNext();) {
								ml.remove(i.next());
							}

							removalList.clear();
						}
						break;	
					}
				}
			} else {
				throw new RuntimeException("Unable to parse message: " + msg);
			}

		} catch (BundleException e) {
			if (logService != null) {
				logService.log(LogService.LOG_ERROR, "Bundle/Modlet error occurred: " + e.getClass().getName() + ", " + e.getMessage());
				StringWriter sw = new StringWriter();				
				e.printStackTrace(new PrintWriter(sw));
				logService.log(LogService.LOG_DEBUG, sw.getBuffer().toString());
			}
			e.printStackTrace();
		} catch (Exception e) {
			if (logService != null) {
				logService.log(LogService.LOG_ERROR, "Bundle/Modlet error occurred: " + e.getClass().getName() + ", " + e.getMessage());
				StringWriter sw = new StringWriter();				
				e.printStackTrace(new PrintWriter(sw));
				logService.log(LogService.LOG_DEBUG, sw.getBuffer().toString());
			}
		}
	}

	/**
	 * Iterate through all runtime bundles and see if a bundle exists that is a
	 * Module Bundle of the type we need.
	 * 
	 * @param moduleId
	 * @return
	 */
	private List findLocalBundles(String moduleId) {
		Bundle[] bundles = context.getBundles();
		List matches = new ArrayList();
		String id;
		for (int i = 0; i < bundles.length; ++i) {
			Dictionary d = bundles[i].getHeaders();
			
			if ((id = (String) d.get(BugBundleConstants.BUG_BUNDLE_MODULE_ID)) != null) {
				if (id.equals(moduleId)) {
					matches.add(bundles[i]);					
				}
			}
		}

		return matches;
	}

	public static Map getActiveModlets() {
		return activeModlets;
	}
}
