package com.buglabs.bug.base.pub;

/**
 * Describes a provider of battery information.
 * 
 * @author kgilmer
 * 
 */
public interface IBatteryInfoProvider {
	public String getId();

	public double getValue();
}
