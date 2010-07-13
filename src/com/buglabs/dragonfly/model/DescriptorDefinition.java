package com.buglabs.dragonfly.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * POJO that holds property information
 * 
 * @author akravets
 * 
 */
public class DescriptorDefinition {
	private IPropertyDescriptor descriptor;
	private String value;
	private String id;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param descriptor
	 * @param value
	 */
	public DescriptorDefinition(String id, IPropertyDescriptor descriptor, Object value) {
		this.descriptor = descriptor;
		this.value = (String) value;
		this.id = id;
	}

	/**
	 * 
	 * @return Returns {@link IPropertyDescriptor} for this property
	 */
	public IPropertyDescriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * 
	 * @return Returns value for this property
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 
	 * @return Returns id for this property
	 */
	public String getId() {
		return id;
	}
}
