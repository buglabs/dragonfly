package com.buglabs.bug.module.audio.pub;

import java.util.List;

import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider2;
import com.buglabs.services.ws.WSResponse;
import com.buglabs.util.XmlNode;

/**
 * A class that exposes WS API for audio module
 * @author akravets
 * @deprecated This module is not supported in BUG 2.0
 */
public class AudioWS implements PublicWSProvider2 {

	IAudioModuleControl amc;
	private String serviceName = "Audio";
	
	public AudioWS(IAudioModuleControl amc){
		this.amc = amc;
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
			return new WSResponse(getAccelerationXml(), "text/xml");
		}
		return null;
	}
	
	private Object getAccelerationXml() {
		XmlNode root = new XmlNode("Status");
		
		//do some magic here to read the state of the VH module and display it
		return root.toString();
	}

	public String getDescription() {
		return "Audio WS";
	}

	public String getPublicName() {
		return serviceName ;
	}

	public void setPublicName(String name) {
		serviceName = name;
	}
}
