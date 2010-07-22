package com.buglabs.bug.module.motion;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.buglabs.bug.module.motion.pub.IMotionObserver;
import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider2;
import com.buglabs.services.ws.WSResponse;
import com.buglabs.util.SelfReferenceException;
import com.buglabs.util.XmlNode;

public class MotionWS implements PublicWSProvider2, IMotionObserver {
	private Date lastMotion;
	private String serviceName = "Motion";

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
			return new WSResponse(getTimeXml(), "text/xml");
		}
		return null;
	}

	private Object getTimeXml() {
		XmlNode root = new XmlNode("Motion");
		try {
			if (lastMotion != null) {
				root.addChildElement(new XmlNode("date", lastMotion.toString()));
			}
		} catch (SelfReferenceException e) {
			e.printStackTrace();
		}
		return root.toString();
	}

	public String getDescription() {
		return "Returns the last time motion was detected";
	}

	public String getPublicName() {
		return serviceName;
	}

	public void motionDetected() {
		lastMotion = GregorianCalendar.getInstance().getTime();
	}

	public void setPublicName(String name) {
		serviceName = name;
	}
}
