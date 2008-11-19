/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.net.URL;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Model class that represents a BUG program.
 * 
 * @author ken
 * 
 */
public class ProgramNode extends BaseNode {
	private static final long serialVersionUID = 2838907666888264594L;

	private static final String PROP_VERSION = "PROP_VERSION";

	private IPackage pkg;

	private URL packageUrl;

	private boolean addedProperties = false;

	public ProgramNode(IPackage pkg, URL url) {
		super(pkg.getName());
		this.packageUrl = url;
		this.pkg = pkg;
	}

	public IPackage getPackage() {
		return pkg;
	}

	public void setPackage(IPackage pkg) {
		this.pkg = pkg;
	}

	public URL getPackageUrl() {
		return packageUrl;
	}

	public void setPackageUrl(URL packageUrl) {
		this.packageUrl = packageUrl;
	}

	public String getName() {
		return pkg.getName();
	}

	public boolean isBUGNetPackage() {
		return !(packageUrl == null);
	}

	public String getBundleType() {
		return pkg.getBundleType();
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {

		super.getPropertyDescriptors();

		if (!addedProperties) {
			propertyDescriptorList.add(new PropertyDescriptor(PROP_VERSION, "Version"));
			addedProperties = true;
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

		if (id.equals(PROP_VERSION)) {
			return pkg.getVersion();
		}

		return null;
	}
}
