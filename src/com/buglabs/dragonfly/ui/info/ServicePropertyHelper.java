package com.buglabs.dragonfly.ui.info;

import java.util.ArrayList;
import java.util.List;

import com.buglabs.dragonfly.model.ServiceProperty;


/**
 * This is a unified model that is currently helping with
 * the CodeGenerationPage
 * 
 * It keeps tracks of all the possible values for a service
 * and the current selected or set value
 * 
 * @author bballantine
 *
 */
public class ServicePropertyHelper {

	private String propkey;
	private List<String> values;
	private String selected_value;
	
	public static List<ServicePropertyHelper> createHelperList(
			List<ServiceProperty> properties) {
		
		List<ServicePropertyHelper> helper = 
			new ArrayList<ServicePropertyHelper>();
	
		for (ServiceProperty p : properties)
			helper.add(new ServicePropertyHelper(p));
	
		return helper;
	}
	
	
	public ServicePropertyHelper(String propkey, List<String> values) {
		this.propkey = propkey;
		this.values = values;
		this.selected_value = null;
	}
	
	public ServicePropertyHelper(ServiceProperty property) {
		this(property.getKey(), new ArrayList<String>(property.getValues()));
	}
	
	public String getKey() {
		return propkey;
	}


	public String getSelectedValue() {
		if (selected_value == null && values.size() > 0)
			return values.get(0);
		else if (selected_value == null)
			return "";
		else
			return selected_value;
	}


	public List<String> getValues() {
		return values;
	}

	public void setSelectedValue(String selectedValue) {
		selected_value = selectedValue;
	}

	public String getValueAt(int index) {
		if (values.size() > index) 
			return values.get(index);
		return null;
	}


	public int getSelectedIndex() {
		int index = values.indexOf(selected_value);
		if (index < 0) index = 0;
		return index;
	}


	public String[] getValuesAsArray() {
		return values.toArray(new String[values.size()]);
	}
	
}