package com.buglabs.dragonfly.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.buglabs.util.xml.XmlNode;

public class PostServiceNode extends ServiceNode {
	private static final long serialVersionUID = 8505755261050519825L;

	private static final String HTTP_PROPERTY_DESCRIPTION = "HTTP_PROPERTY_DESCRIPTION";

	private boolean propertyAdded = false;

	public PostServiceNode(XmlNode node, String url) {

		super(node.getParent().getAttribute("name") + "[POST]", node.getParent().getAttribute("description"), url);
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {

		super.getPropertyDescriptors();

		if (!propertyAdded) {
			propertyDescriptorList.add(new PropertyDescriptor(HTTP_PROPERTY_DESCRIPTION, "HTTP Operation"));
			propertyAdded = true;
		}
		IPropertyDescriptor[] ipd = (IPropertyDescriptor[]) propertyDescriptorList.toArray(new IPropertyDescriptor[propertyDescriptorList.size()]);

		return ipd;
	}

	public Object getPropertyValue(Object id) {

		Object val = super.getPropertyValue(id);

		if (val != null) {
			return val;
		}

		if (id.equals(HTTP_PROPERTY_DESCRIPTION)) {
			return "Post";
		}

		return null;
	}
}
