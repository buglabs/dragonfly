package com.buglabs.dragonfly.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.osgi.framework.Bundle;

import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.util.xml.XmlNode;

/**
 * Complex property of application state
 * 
 * @author akravets
 * 
 */
public class AppStatePropertySource implements IPropertySource {

	private Map appStateMap;
	private Map bundleStateDef; // holds bundle state name to value mapping
	private BugConnection bug;

	public AppStatePropertySource(Map appStateMap, BugConnection bug) {
		this.appStateMap = appStateMap;
		this.bug = bug;
		bundleStateDef = new HashMap();
		bundleStateDef.put("ACTIVE", new Integer(Bundle.ACTIVE));
		bundleStateDef.put("UNINSTALLED", new Integer(Bundle.UNINSTALLED));
	}

	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		IPropertyDescriptor[] descriptors = new IPropertyDescriptor[appStateMap.size()];
		int cnt = 0;
		for (Iterator i = appStateMap.keySet().iterator(); i.hasNext();) {
			DescriptorDefinition dd = (DescriptorDefinition) appStateMap.get(i.next());
			descriptors[cnt] = dd.getDescriptor();
			cnt++;
		}
		return descriptors;
	}

	public Object getPropertyValue(Object id) {
		DescriptorDefinition dd = (DescriptorDefinition) appStateMap.get(id);
		int value = Integer.parseInt(dd.getValue());
		switch (value) {
		case Bundle.ACTIVE:
			return new Integer(0);
		case Bundle.UNINSTALLED:
			return new Integer(1);
		}
		return new Integer(1);
	}

	public boolean isPropertySet(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	public void resetPropertyValue(Object id) {
		// TODO Auto-generated method stub

	}

	public void setPropertyValue(Object id, Object value) {
		// send payload to web service describing the update
		try {
			XmlNode node = new XmlNode("update");
			node.addAttribute("type", BugConnection.PROPERTY_TYPE_APP_STATE);

			XmlNode prop = new XmlNode("property");
			prop.addAttribute("pid", "com.buglabs.bug.program.UserAppManager");
			prop.addAttribute("applicationName", id.toString());

			String state = BugConnection.APPLICATION_STATE[Integer.parseInt(value.toString())];
			prop.addAttribute("newValue", bundleStateDef.get(state).toString());

			node.addChild(prop);
			BugWSHelper.setConfigurationProperty(bug.getConfigAdminURL(), node.toString());
		} catch (MalformedURLException e) {
			UIUtils.handleVisualError("Failed to set property for " + id + ", invalid url", e);
		} catch (IOException e) {
			UIUtils.handleVisualError("Failed to set property for " + id, e);
		}
	}

}
