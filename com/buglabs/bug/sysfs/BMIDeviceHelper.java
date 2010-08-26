package com.buglabs.bug.sysfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry-point into the sysfs API for BUG20.  This API is a null-safe way of reading and writing to sysfs entries for BUG drivers that support them.
 * 
 * @author kgilmer
 *
 */
public class BMIDeviceHelper {

	/**
	 * @return The BMIDevices attached at the time of the call.  If a given array element is null, no module is attached to that slot.
	 * @throws IOException
	 */
	public static BMIDevice[] getDevices() {
		throw new RuntimeException(BMIDeviceHelper.class.getName() + " is not available in Bug Simulator.");		
	}
	
	/**
	 * @return list of BMIDevices that are currently attached, or empty list if no modules are attached.
	 */
	public static List getAttachedDevices() {
		throw new RuntimeException(BMIDeviceHelper.class.getName() + " is not available in Bug Simulator.");
	}
	
	/**
	 * @param slot
	 * @return The BMIDevice that exists at the passed slot or null if no device attached.
	 */
	public static BMIDevice getDevice(int slot) {
		throw new RuntimeException(BMIDeviceHelper.class.getName() + " is not available in Bug Simulator.");
	}


	/**
	 * @param prodFile
	 * @return true if a module is inserted and recognized by BMI, false otherwise.
	 */
	private static boolean validBMIDeviceRoot(File prodFile) {
		if (!prodFile.exists() || !prodFile.isDirectory()) {
			return false;
		}
		
		return prodFile.listFiles().length > 0;
	}

	/**
	 * @param i
	 * @return
	 */
	private static File getBMIDeviceRoot(int i) {
		return new File("/sys/devices/platform/omap_bmi_slot." + i + "/bmi-" + i + "/bmi-dev-" + i);
	}


}
