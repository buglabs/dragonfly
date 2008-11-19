/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Primitive model element implementation.
 * 
 * @author ken
 * 
 */
public class BaseNode implements IModelNode {
	private static final long serialVersionUID = 2769474840703219612L;

	// Subclassers can add to this list.
	protected List propertyDescriptorList;

	private String name;

	protected static final String PROP_NAME = "PROP_NAME";

	public BaseNode(String name) {
		this.name = name;
	}

	public Object getEditableValue() {
		return null;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (propertyDescriptorList == null) {
			propertyDescriptorList = new ArrayList();

			propertyDescriptorList.add(new PropertyDescriptor(PROP_NAME, "Name"));
		}

		return (IPropertyDescriptor[]) propertyDescriptorList.toArray(new IPropertyDescriptor[propertyDescriptorList.size()]);
	}

	public Object getPropertyValue(Object id) {
		String i = (String) id;

		if (i.equals(PROP_NAME)) {
			return this.name;
		}

		return null;
	}

	public boolean isPropertySet(Object id) {

		return false;
	}

	public void resetPropertyValue(Object id) {

	}

	public void setPropertyValue(Object id, Object value) {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
