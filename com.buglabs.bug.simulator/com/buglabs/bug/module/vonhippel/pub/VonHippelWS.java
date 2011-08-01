package com.buglabs.bug.module.vonhippel.pub;

import java.util.List;

import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider2;
import com.buglabs.services.ws.WSResponse;

import com.buglabs.util.xml.XmlNode;

/**
 * A class that exposes WS API for Von Hippel module
 * @author akravets
 *
 */
public class VonHippelWS implements PublicWSProvider2 {

	private String serviceName = "VonHippel";

	public VonHippelWS(){}
	
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
		
		//gpio.  style = <GPIO>
		//                <Pin number="0">0</Pin>
		// 			      <Pin number="1">0</Pin> ...
		XmlNode gpio = new XmlNode("GPIO");
		root.addChild(gpio);
		XmlNode pin0 = new XmlNode("Pin", "0");
		XmlNode pin1 = new XmlNode("Pin", "0");
		XmlNode pin2 = new XmlNode("Pin", "0");
		XmlNode pin3 = new XmlNode("Pin", "0");
		pin0.addAttribute("number", "0");
		pin1.addAttribute("number", "1");
		pin2.addAttribute("number", "2");
		pin3.addAttribute("number", "3");
		gpio.addChild(pin0);
		gpio.addChild(pin1);
		gpio.addChild(pin2);
		gpio.addChild(pin3);
		//iox.  style = <IOX>
		//                <Pin number="0">0</Pin>
		// 			      <Pin number="1">0</Pin> ...
		XmlNode iox = new XmlNode("IOX");
		root.addChild(iox);
		XmlNode ioxpin0 = new XmlNode("Pin", "0");
		XmlNode ioxpin1 = new XmlNode("Pin", "0");
		XmlNode ioxpin2 = new XmlNode("Pin", "0");
		XmlNode ioxpin3 = new XmlNode("Pin", "0");
		XmlNode ioxpin4 = new XmlNode("Pin", "0");
		XmlNode ioxpin5 = new XmlNode("Pin", "0");
		ioxpin0.addAttribute("number", "0");
		ioxpin1.addAttribute("number", "1");
		ioxpin2.addAttribute("number", "2");
		ioxpin3.addAttribute("number", "3");
		ioxpin4.addAttribute("number", "4");
		ioxpin5.addAttribute("number", "5");
		iox.addChild(ioxpin0);
		iox.addChild(ioxpin1);
		iox.addChild(ioxpin2);
		iox.addChild(ioxpin3);
		iox.addChild(ioxpin4);
		iox.addChild(ioxpin5);
	
		return root.toString();
	}

	public String getDescription() {
		return "Von Hippel WS";
	}

	public String getPublicName() {
		return serviceName;
	}

	public void setPublicName(String name) {
		serviceName = name;
	}
}
