package com.buglabs.dragonfly.model;

import java.util.Set;
import java.util.TreeSet;

/**
 * Very simple bucket for key value pairs representing properties of an osgi
 * service
 * 
 * A list of these guys is sortable
 * 
 * @author brian
 * 
 */
public class ServiceProperty implements Comparable<ServiceProperty> {
	private String key;
	private Set<String> values;

	public ServiceProperty(String key, Set<String> values) {
		this.key = key;
		this.values = values;
	}

	public ServiceProperty(String key, String value) {
		this.key = key;
		values = new TreeSet<String>();
		values.add(value);
	}

	public String getKey() {
		return key;
	}

	public Set<String> getValues() {
		return values;
	}

	public void addValue(String value) {
		values.add(value);
	}

	public void addValues(Set<String> values) {
		this.values.addAll(values);
	}

	public int compareTo(ServiceProperty o) {
		return key.compareTo(o.getKey());
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ServiceProperty && this.compareTo((ServiceProperty) obj) == 0;
	}

	@Override
	public int hashCode() {
		return (key + values.toString() + values.size()).hashCode();
	}

}
