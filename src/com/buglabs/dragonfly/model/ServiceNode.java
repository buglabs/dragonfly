package com.buglabs.dragonfly.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Represents a service of the BUG
 * 
 * @author akravets
 * 
 */
abstract public class ServiceNode extends BaseNode {
	private static final long serialVersionUID = 3584433339788701474L;

	private static final String PROPERTY_DESCRIPTION = "PROP_DESCRIPTION";

	private static final String URL_DESCRIPTION = "URL_DESCRIPTION";

	private String description;

	private boolean propertyAdded = false;

	private final String baseUrl;

	public ServiceNode(String name, String description, String baseUrl) {
		super(name);
		this.description = description;
		this.baseUrl = baseUrl + "/" + name;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {

		super.getPropertyDescriptors();

		if (!propertyAdded) {
			propertyDescriptorList.add(new PropertyDescriptor(PROPERTY_DESCRIPTION, "Description"));
			propertyDescriptorList.add(new PropertyDescriptor(URL_DESCRIPTION, "Web Service URL"));
			propertyAdded = true;
		}
		IPropertyDescriptor[] ipd = (IPropertyDescriptor[]) propertyDescriptorList.toArray(new IPropertyDescriptor[propertyDescriptorList
				.size()]);

		return ipd;
	}

	public String getUrl() {
		return baseUrl;
	}

	public Object getPropertyValue(Object id) {

		Object val = super.getPropertyValue(id);

		if (val != null) {
			return val;
		}

		if (id.equals(PROPERTY_DESCRIPTION)) {
			return description;
		}

		if (id.equals(URL_DESCRIPTION)) {
			return baseUrl;
		}

		return null;
	}

}
