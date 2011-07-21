package com.buglabs.dragonfly.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a container for Service Details It keeps some properties of a Service
 * to make passing around a service and it's properties easier
 * 
 * @author brian
 * 
 */
public class ServiceDetail {

	private String service_name;
	private List<ServiceProperty> service_properties;

	public ServiceDetail() {
		this(null, null);
	}

	public ServiceDetail(String serviceName, List<ServiceProperty> serviceProperties) {
		service_name = serviceName;
		service_properties = serviceProperties;
	}

	public String getServiceName() {
		return service_name;
	}

	public synchronized List<ServiceProperty> getServiceProperties() {
		if (service_properties == null)
			service_properties = new ArrayList<ServiceProperty>();
		return service_properties;
	}

	/**
	 * Add a property keeping out duplicates (a duplicate has both key and value
	 * equal)
	 * 
	 * @param prop
	 */
	public synchronized void addServiceProperty(ServiceProperty prop) {
		if (!getServiceProperties().contains(prop))
			getServiceProperties().add(prop);
	}

	/**
	 * Adds keeping out duplicates
	 * 
	 * @param properties
	 */
	public synchronized void addServiceProperties(List<ServiceProperty> properties) {
		for (ServiceProperty p : properties) {
			if (getServiceProperties().contains(p))
				getServiceProperties().get(getServiceProperties().indexOf(p)).addValues(p.getValues());
			else
				getServiceProperties().add(p);
		}
	}

	public synchronized void clearServiceProperties() {
		getServiceProperties().clear();
	}

}
