package com.buglabs.dragonfly.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.buglabs.util.XmlNode;

public class GetServiceNode extends ServiceNode {
	private static final long serialVersionUID = 8505755261050519825L;

	private static final String HTTP_PROPERTY_DESCRIPTION = "HTTP_PROPERTY_DESCRIPTION";

	private static final String DATATYPE_DESCRIPTION = "DATATYPE_DESCRIPTION";

	private static final String PARAMETERS_DESCRIPTION = "PARAMETERS_DESCRIPTION";

	private boolean propertyAdded = false;

	private final XmlNode node;

	public GetServiceNode(XmlNode node, String url) {
		super(node.getParent().getAttribute("name"), node.getParent().getAttribute("description"), url);
		XmlNode top = node.getParent().getParent().getParent();

		this.node = node;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {

		super.getPropertyDescriptors();

		if (!propertyAdded) {
			propertyDescriptorList.add(new PropertyDescriptor(HTTP_PROPERTY_DESCRIPTION, "HTTP Operation"));
			propertyDescriptorList.add(new PropertyDescriptor(DATATYPE_DESCRIPTION, "Return Mime-Type"));
			propertyDescriptorList.add(new PropertyDescriptor(PARAMETERS_DESCRIPTION, "Parameters"));

			propertyAdded = true;
		}
		IPropertyDescriptor[] ipd = (IPropertyDescriptor[]) propertyDescriptorList.toArray(new IPropertyDescriptor[propertyDescriptorList
				.size()]);

		return ipd;
	}

	public Object getPropertyValue(Object id) {

		Object val = super.getPropertyValue(id);

		if (val != null) {
			return val;
		}

		if (id.equals(HTTP_PROPERTY_DESCRIPTION)) {
			return "Get";
		}

		if (id.equals(DATATYPE_DESCRIPTION)) {
			return node.getAttribute("returns");
		}

		if (id.equals(PARAMETERS_DESCRIPTION)) {
			return node.getAttribute("parameters");
		}

		return null;
	}

	public Object[] getChildren(Object o) {
		return null;
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	public String getLabel(Object o) {
		return this.getName();
	}

	public Object getParent(Object o) {
		return this.getParent(o);
	}
}
