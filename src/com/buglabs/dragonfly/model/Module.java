package com.buglabs.dragonfly.model;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

public class Module extends BaseTreeNode {
	private static final long serialVersionUID = -1927265878452312818L;

	private URL url;

	private Map propertyMap;

	private int index;

	public Module(ITreeNode p, URL u, String name) {
		super(name);
		setParent(p);
		this.url = u;
	}
	
	public Module(ITreeNode p, URL u, String name, int index){
		this(p,u,name);
		this.index = index;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		IPropertyDescriptor[] spd = super.getPropertyDescriptors();
		List props = new ArrayList();

		// Load super's properties.
		if (spd != null) {
			for (int i = 0; i < spd.length; ++i) {
				props.add(spd[i]);
			}
		}

		try {
			propertyMap = BugWSHelper.getModulePropertyDescriptorMap(this);
		} catch (IOException e) {
			UIUtils.handleNonvisualError("An error occurred while uploading application.", e);
		}

		if(propertyMap != null){
			for (Iterator i = propertyMap.keySet().iterator(); i.hasNext();) {
				String name = (String) i.next();
	
				BugProperty bp = (BugProperty) propertyMap.get(name);
	
				if (bp.isMutable()) {
					props.add(new TextPropertyDescriptor(name, name));
				} else {
					props.add(new PropertyDescriptor(name, name));
				}
			}
		}

		return (IPropertyDescriptor[]) props.toArray(new IPropertyDescriptor[props.size()]);
	}

	public Object getPropertyValue(Object id) {
		if (id instanceof String) {
			Object o = propertyMap.get(id);

			if (o != null && o instanceof BugProperty) {
				return ((BugProperty) o).getValue();
			}
		}

		return super.getPropertyValue(id);
	}

	public void setPropertyValue(Object id, Object value) {
		if (id instanceof String) {
			Object o = propertyMap.get(id);

			if (o != null && o instanceof BugProperty) {
				try {
					BugWSHelper.updateProperty(this, (BugProperty) o, value);
					return;
				} catch (IOException e) {
					UIUtils.handleVisualError("Unable to update property on BUG.", e);
				}
			}
		}

		super.setPropertyValue(id, value);
	}

	public URL getUrl() {
		return url;
	}

	public Map getProperties() {
		if (propertyMap == null) {
			getPropertyDescriptors();
		}
		return propertyMap;
	}

	/**
	 * @return Slot number that this module occupies
	 */
	public int getIndex() {
		return index;
	}

}
