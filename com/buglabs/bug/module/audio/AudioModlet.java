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
package com.buglabs.bug.module.audio;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import com.buglabs.bug.module.audio.pub.AudioWS;
import com.buglabs.bug.module.audio.pub.IAudioModuleControl;
import com.buglabs.bug.module.audio.pub.IModuleAudioPlayer;
import com.buglabs.bug.module.pub.IModlet;
import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleLEDController;
import com.buglabs.module.IModuleProperty;
import com.buglabs.module.ModuleProperty;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.util.LogServiceUtil;
import com.buglabs.util.RemoteOSGiServiceConstants;

public class AudioModlet implements IModlet, IModuleControl {

    protected static final String PROPERTY_MODULE_NAME = "moduleName";
    
	public static final String MODULE_ID = "0007";
	
	private BundleContext context;
	private ServiceRegistration wsAudioReg;
	
	private ServiceRegistration moduleRef;
	private ServiceRegistration auModuleRef;
	private ServiceRegistration ledRef;
	private ServiceRegistration playerRef;

	private String regionKey;
	private int slotId;
	private final String moduleId;
	private final String moduleName;
    private boolean deviceOn = true;
    
	AudioModuleControl audioControl;
	NonworkingAudioPlayer audioPlayer;

	//private ServiceRegistration backendRef;
	
	private static LogService logService;


	
	private static boolean icon[][] = {
			{ false, false, false, false, false, false, false, false, false,
					false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false,
					false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false,
					false, false, false, false, false, false, false },
			{ false, false, true, true, true, true, true, true, true, true,
					true, true, true, true, false, false },
			{ false, true, false, false, false, false, false, false, false,
					false, false, false, false, false, true, false },
			{ false, true, false, false, false, false, false, false, true,
					false, false, false, false, false, true, false },
			{ false, true, false, false, false, false, false, true, true,
					false, false, true, false, false, true, false },
			{ false, true, false, false, false, false, true, true, true, false,
					false, false, true, false, true, false },
			{ false, true, false, true, true, true, true, true, true, false,
					true, false, false, true, true, false },
			{ false, true, false, true, true, true, true, true, true, false,
					false, true, false, true, true, false },
			{ false, true, false, true, true, true, true, true, true, false,
					false, true, false, true, true, false },
			{ false, true, false, false, false, false, true, true, true, false,
					true, false, false, true, true, false },
			{ false, true, false, false, false, false, false, true, true,
					false, false, false, true, false, true, false },
			{ false, true, false, false, false, false, false, false, true,
					false, false, true, false, false, true, false },
			{ false, true, false, false, false, false, false, false, false,
					false, false, false, false, false, true, false },
			{ false, true, true, true, true, true, true, true, true, true,
					true, true, true, true, true, false },
			{ false, true, true, true, true, true, true, true, true, true,
					true, true, true, true, true, false },
			{ false, false, false, false, false, false, false, false, false,
					false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false,
					false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false,
					false, false, false, false, false, false, false } };

	public AudioModlet(BundleContext context, int slotId, String moduleId, String moduleName) {
		this.context = context;
		this.slotId = slotId;
		this.moduleId = moduleId;
		this.moduleName = moduleName;
		logService =  LogServiceUtil.getLogService(context);
	}

	
	public void start() throws Exception {
		audioControl	= new AudioModuleControl();
		moduleRef 	= context.registerService(IModuleControl.class.getName(), 		this, createBasicServiceProperties());
		auModuleRef = context.registerService(IAudioModuleControl.class.getName(),	audioControl, createRemotableProperties(createBasicServiceProperties()));

		
		//backendRef = context.registerService(SoundBackend.class.getName(), backend, createBasicServiceProperties());
	
		audioPlayer = new NonworkingAudioPlayer();
		playerRef = context.registerService(IModuleAudioPlayer.class.getName(), audioPlayer, createRemotableProperties(createBasicServiceProperties()));
		ledRef 		= context.registerService(IModuleLEDController.class.getName(), audioControl, createRemotableProperties(createBasicServiceProperties()));
		AudioWS aWS = new AudioWS(audioControl);
		wsAudioReg = context.registerService(PublicWSProvider.class.getName(), aWS, null);
	}

	public void stop() throws Exception {
		unregisterService(wsAudioReg);
		unregisterService(moduleRef);
		unregisterService(auModuleRef);
		unregisterService(playerRef);
		//unregisterService(backendRef);
		unregisterService(ledRef);
	}
	
	/**
	 * Unregister an osgi service if not null.
	 * @param r
	 */
	private void unregisterService(ServiceRegistration r) {
		if (r != null) {
			r.unregister();
		}
	}
	
	/**
	 * Unregister tracker if not null
	 * @param st
	 */
	private void unregisterTracker(ServiceTracker st) {
		if (st != null) {
			st.close();
		}
	}

	private Properties createBasicServiceProperties() {
		Properties p = new Properties();
		p.put("Provider", this.getClass().getName());
		p.put("Slot", Integer.toString(slotId));
		return p;
	}	

	public String getModuleId() {
		return moduleId;
	}
	

	public void setup() throws Exception {
	}
	
	public static LogService getLogService() {
	    return logService;
	}

    public int getSlotId() {
        return slotId;
    }
    
    public List getModuleProperties() {
        List properties = new ArrayList();
        properties.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
        properties.add(new ModuleProperty("Slot", "" + slotId));
        properties.add(new ModuleProperty("State",
                Boolean.toString(deviceOn),"Boolean", true));
        return properties;
    }

    public boolean setModuleProperty(IModuleProperty property) {
        if (!property.isMutable()) {
            return false;
        }
        if (property.getName().equals("State")) {
            deviceOn = Boolean.valueOf(
                    (String) property.getValue()).booleanValue();
            return true;
        }
        return false;
    }

    public String getModuleName() {
        return moduleName;
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


	public int resume() throws IOException {
		throw new IOException("AudioModlet resume call is not implemented for Virtual BUG");
	}


	public int suspend() throws IOException {
		throw new IOException("AudioModlet suspend call is not implemented for Virtual BUG");
	}
     
}
