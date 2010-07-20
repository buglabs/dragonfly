/*******************************************************************************
 * Copyright (c) 2008, 2009 Bug Labs, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package com.buglabs.bug.module.gsm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

import com.buglabs.bug.module.gsm.pub.IGSMModuleControl;
import com.buglabs.bug.module.pub.IModlet;
import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleLEDController;
import com.buglabs.module.IModuleProperty;
import com.buglabs.module.ModuleProperty;
import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider2;
import com.buglabs.util.LogServiceUtil;
import com.buglabs.util.RemoteOSGiServiceConstants;

/**
 * The Modlet exports the hardware-level services to the OSGi runtime.
 * 
 * @author jconnolly
 * 
 */
public class GSMModlet implements IModlet, IGSMModuleControl, IModuleControl, PublicWSProvider2, IModuleLEDController {

	private BundleContext context;

	private int slotId;

	private final String moduleId;

	private ServiceRegistration moduleRef;

	protected static final String PROPERTY_MODULE_NAME = "moduleName";
	
	public static final String MODULE_ID = "000B";

	private final String moduleName;

	private ServiceRegistration gsmControlRef;

	private LogService logService;

	private ServiceRegistration ledRef;

	private String serviceName;

	private boolean suspended;

	private boolean deviceOn = true;

	/**
	 * @param context
	 * @param slotId
	 * @param moduleId
	 * @param moduleName
	 */
	public GSMModlet(BundleContext context, int slotId, String moduleId, String moduleName) {
		this.context = context;
		this.slotId = slotId;
		this.moduleName = moduleName;
		this.moduleId = moduleId;
		this.logService = LogServiceUtil.getLogService(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.buglabs.bug.module.pub.IModlet#start()
	 */
	public void start() throws Exception {
		

		moduleRef = context.registerService(IModuleControl.class.getName(), this, null);
		ledRef = context.registerService(IModuleLEDController.class.getName(), this, createRemotableProperties(null));
		gsmControlRef = context.registerService(IGSMModuleControl.class.getName(), this, createRemotableProperties(null));
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



	/*
	 * (non-Javadoc)
	 * 
	 * @see com.buglabs.bug.module.pub.IModlet#stop()
	 */
	public void stop() throws Exception {
		logService.log(LogService.LOG_DEBUG, "GSMModlet stop enter");
	
		moduleRef.unregister();
		gsmControlRef.unregister();
		
		ledRef.unregister();
		
		logService.log(LogService.LOG_DEBUG, "GSMModlet stop leave");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.buglabs.module.IModuleControl#getModuleProperties()
	 */
	public List getModuleProperties() {
		List properties = new ArrayList();

		properties.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
		properties.add(new ModuleProperty("Slot", "" + slotId));
		properties.add(new ModuleProperty("State", Boolean.toString(deviceOn), "Boolean", true));
		properties.add(new ModuleProperty("Power State", suspended ? "Suspended": "Active", "String", true));
		return properties;
	}

	public boolean setModuleProperty(IModuleProperty property) {
		if (!property.isMutable()) {
			return false;
		}
		if (property.getName().equals("State")) {
			return true;
		}	
				
		
		return false;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getModuleId() {
		return moduleId;
	}

	public int getSlotId() {
		return slotId;
	}

	public int resume() throws IOException {
		throw new IOException("GSMModlet resume call is not implemented for BUG Simulator");
	}
	

	public int suspend() throws IOException {
		throw new IOException("GSMModlet suspend call is not implemented for BUG Simulator");
	}

	
	public PublicWSDefinition discover(int operation) {
		if (operation == PublicWSProvider2.GET) {
			return new PublicWSDefinition() {

				public List getParameters() {
					return null;
				}

				public String getReturnType() {
					return "text/xml";
				}
			};
		}

		return null;
	}

	public IWSResponse execute(int operation, String input) {
		if (operation == PublicWSProvider2.GET) {
			
		}
		return null;
	}



	public String getPublicName() {
		return serviceName;
	}

	public String getDescription() {
		return "API for using the GSM Module.";
	}

	public void setup() throws Exception {



	}

	public int LEDGreenOff() throws IOException {
		return setLEDGreen(false);
	}

	public int LEDGreenOn() throws IOException {
		return setLEDGreen(true);
	}

	public int LEDRedOff() throws IOException {
		return setLEDRed(false);
	}

	public int LEDRedOn() throws IOException {
		return setLEDRed(true);
	}

	public int setLEDGreen(boolean state) throws IOException {
		if(state)
			logService.log(LogService.LOG_INFO, "Red LED is on");
		else
			logService.log(LogService.LOG_INFO, "Red LED is off");
		return 0;
	}

	public int setLEDRed(boolean state) throws IOException {
		if(state)
			logService.log(LogService.LOG_INFO, "Red LED is on");
		else
			logService.log(LogService.LOG_INFO, "Red LED is off");
		return 0;
	}

	public void setPublicName(String name) {
		serviceName = name;
	}
}
