package com.buglabs.bug.base.pub;

import java.io.IOException;

/**
 * Provides control of BUGbase LEDs.
 * 
 * @author aroman
 * @deprecated
 * 
 */
public interface IBUGBaseControl {
	/**
	 * 
	 * @param index
	 *            A number between 0 and 3 specifying which LED to set.
	 * 
	 * @throws IOException
	 *             when underlying native call fails.
	 */
	public void setLED(int index) throws IOException;

	/**
	 * 
	 * @param index
	 *            A number between 0 and 3 specifying which LED to set.
	 * 
	 * @throws IOException
	 *             when underlying native call fails.
	 */
	public void clearLED(int index) throws IOException;

	/**
	 * clear base unit LEDs
	 * 
	 * @param leds
	 *            The bitmask of which LEDs to clear. Specifically, the 4 LSBs.
	 * 
	 * @return value of lower level ioctl call.
	 */
	public void clearLEDs(int leds) throws IOException;

	/**
	 * set base unit LEDs
	 * 
	 * @param leds
	 *            The bitmask of which LEDs to clear. Specifically, the 4 LSBs.
	 * 
	 * @return value of lower level ioctl call.
	 */
	public void setLEDs(int leds) throws IOException;
}
