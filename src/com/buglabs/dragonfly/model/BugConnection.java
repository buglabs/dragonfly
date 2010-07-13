package com.buglabs.dragonfly.model;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.osgi.framework.Bundle;

import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.util.XmlNode;
import com.buglabs.util.XmlParser;

/**
 * Generic BUG connection
 * 
 * @author akravets
 * 
 */
public abstract class BugConnection extends Bug implements IWorkbenchAdapter {
	private static final long serialVersionUID = 7887258005218750811L;

	private static final String URL_PROPERTY_DESCTIPTION = "URL_PROPERTY_DESCTIPTION";

	private Map configProperty;

	private Map appStateMap;

	private HashMap bundleStateDef;

	private AppStatePropertySource appStateProperty;

	private static final String[] MODULE_STATE = { "true", "false" };

	public static final String[] APPLICATION_STATE = { "ACTIVE", "UNINSTALLED" };

	public static final String PROPERTY_TYPE_REGULAR = "PROPERTY_TYPE_REGULAR";

	public static final String PROPERTY_TYPE_APP_STATE = "PROPERTY_TYPE_APP_STATE";

	public BugConnection(String name, URL url) {
		super(name, url);
		configProperty = new HashMap();
		appStateMap = new HashMap();
		appStateProperty = new AppStatePropertySource(appStateMap, this);

		bundleStateDef = new HashMap();
		bundleStateDef.put(new Integer(Bundle.ACTIVE), "ACTIVE");
		bundleStateDef.put(new Integer(Bundle.UNINSTALLED), "UNINSTALLED");
		bundleStateDef.put(new Integer(Bundle.STARTING), "STARTING");
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {

		super.getPropertyDescriptors();

		propertyDescriptorList.clear();
		propertyDescriptorList.add(new PropertyDescriptor(URL_PROPERTY_DESCTIPTION, "URL"));
		propertyDescriptorList.add(new PropertyDescriptor(PROP_NAME, "Name"));

		addConfigurationPropertes();
		IPropertyDescriptor[] ipd = (IPropertyDescriptor[]) propertyDescriptorList.toArray(new IPropertyDescriptor[propertyDescriptorList.size()]);

		return ipd;
	}

	private void addConfigurationPropertes() {
		try {
			String properties = BugWSHelper.getConfigurationProprtiesAsXml(getConfigAdminURL());
			List list = prepareData(properties);

			for (Iterator i = list.iterator(); i.hasNext();) {
				Map propertyMap = ((Map) i.next());
				for (Iterator j = propertyMap.keySet().iterator(); j.hasNext();) {
					IPropertyDescriptor descriptor = (IPropertyDescriptor) j.next();
					String id = (String) descriptor.getId();
					propertyDescriptorList.add(descriptor);
					configProperty.put(id, new DescriptorDefinition(id, descriptor, propertyMap.get(descriptor)));
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List prepareData(String xml) throws IOException {
		XmlParser parser = new XmlParser();
		List props = new ArrayList();

		XmlNode root = parser.parse(new StringReader(xml));
		//XmlNode configurations = root.getChild("configurations");

		for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
			XmlNode e = (XmlNode) i.next();

			String configurationName = e.getAttribute("pid");

			List children = e.getChildren();

			for (Iterator j = children.iterator(); j.hasNext();) {
				XmlNode property = (XmlNode) j.next();

				Map propertyMap = new HashMap();

				String name = property.getAttribute("name");
				String displayName = name;

				// if configuration has a pid signature of PublicWSProvider.PACKAGE_ID then it's a property for a module's state. Prefix module name
				// before the property name. This is a workaround for PropertySource's requirement of a property being unique.
				if (configurationName.indexOf(PublicWSProvider.PACKAGE_ID) != -1 && name.equals("enabled")) {
					displayName = configurationName.substring(configurationName.lastIndexOf(".") + 1, configurationName.length()) + "-" + name;
				}

				PropertyDescriptor descriptor = null;

				// make all properties editable
				descriptor = new TextPropertyDescriptor(displayName, displayName);

				descriptor.setCategory(configurationName);

				propertyMap.put(descriptor, property.getAttribute("value"));

				props.add(propertyMap);

				if (name.equals("app.state")) {
					List applicationStates = property.getChildren();
					appStateMap.clear();
					for (Iterator k = applicationStates.iterator(); k.hasNext();) {
						XmlNode appStateProperty = (XmlNode) k.next();
						String appName = appStateProperty.getAttribute("appName");
						String appState = appStateProperty.getAttribute("state");
						ComboBoxPropertyDescriptor appDescriptor = new ComboBoxPropertyDescriptor(appName, appName, APPLICATION_STATE);

						appStateMap.put(appName, new DescriptorDefinition(appName, appDescriptor, appState));
					}
				}
			}
		}
		return props;
	}

	public Object getPropertyValue(Object id) {
		Object val = super.getPropertyValue(id);

		if (val != null) {
			return val;
		}

		if (id.equals(URL_PROPERTY_DESCTIPTION)) {
			return getUrl();
		} else if (id.equals(PROP_NAME)) {
			return getName();
		}

		val = getConfigurationValue(id);

		if (val == null) {
			val = new String("[null]");
		}

		return val;
	}

	private Object getConfigurationValue(Object id) {
		for (Iterator i = configProperty.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			DescriptorDefinition def = (DescriptorDefinition) configProperty.get(key);
			String value = def.getValue();
			if (id.equals(key)) {
				if (((String) id).indexOf("enabled") != -1 || ((String) id).indexOf("Status Bar contribution") != -1) {
					if (value.equals("true")) {
						return "0";
					}
					return "1";
				} else if (((String) id).indexOf("app.state") != -1) {
					return appStateProperty;
				}
			}
			if (id.equals(key)) {
				return value;
			}
		}
		return null;
	}

	public void setPropertyValue(Object id, Object value) {
		try {
			if (!id.equals("app.state")) {
				setConfigurationValue(id, value);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setConfigurationValue(Object id, Object value) throws MalformedURLException, IOException {
		DescriptorDefinition definition = (DescriptorDefinition) configProperty.get(id);
		String category = definition.getDescriptor().getCategory();
		// TODO: this is bad, needs to be refactored
		if (category.indexOf("com.buglabs.service.ws") != -1 || category.equals("com.buglabs.bug.emulator.base.Activator")
				|| category.equals("com.buglabs.bug.module.gps.Activator")) {
			value = MODULE_STATE[Integer.parseInt(value.toString())];
		}

		// payload for updating state of the module
		XmlNode node = new XmlNode("update");
		node.addAttribute("type", PROPERTY_TYPE_REGULAR);

		XmlNode prop = new XmlNode("property");
		prop.addAttribute("pid", category);
		prop.addAttribute("id", id.toString());
		prop.addAttribute("newValue", value.toString());

		node.addChildElement(prop);
		BugWSHelper.setConfigurationProperty(getConfigAdminURL(), node.toString());
	}

	public boolean equals(Object o) {
		if (o instanceof BugConnection) {
			if (this == o)
				return true;

			if ((o == null) || (o.getClass() != this.getClass()))
				return false;

			BugConnection node = (BugConnection) o;
			if (this.getUrl().equals(node.getUrl())) {
				return true;
			}
		}
		return false;
	}

	public Object[] getChildren(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * Need to implements this method so that name of the bug appears in
	 * PropertyDialog
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return getName();
	}

	public Object getParent(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString() {
		return getClass().getName() + "[" + getName() + ", " + getUrl() + "]";
	}
}
