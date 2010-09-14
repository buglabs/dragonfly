package com.buglabs.bug.sysfs;

import java.io.File;
import java.io.IOException;

import org.osgi.service.log.LogService;

import com.buglabs.bug.simulator.Activator;
import com.buglabs.bug.sysfs.SysfsNode;

/**
 * A class to control a single-color or multi-color LED.
 * 
 * @author kgilmer
 * 
 */
public class LEDDevice extends SysfsNode {

	/**
	 * LED is single color.
	 */
	public static final int TYPE_MONO_COLOR = 1;
	/**
	 * LED is tri color
	 */
	public static final int TYPE_TRI_COLOR = 2;

	/**
	 * Constant for single color LED
	 */
	public static final int COLOR_MONO = 0;

	/**
	 * Color constants for tri color LEDs.
	 */
	public static final int COLOR_RED = 0;
	public static final int COLOR_GREEN = 1;
	public static final int COLOR_BLUE = 2;

	/**
	 * Name of LED in sysfs
	 */
	private final String name;

	/**
	 * Number of colors LED supports
	 */
	private final int type;
	private int[] brightness;
	private int[] maxBrightness;
	private String[] trigger;
	private int ledCount;
	private LogService log;

	/**
	 * Constructor for mono LED.
	 * 
	 * @param root
	 * @param name
	 * @param color
	 */
	public LEDDevice(File root, String name, String color) {
		super(root);
		this.log = Activator.getLogService();
		this.name = name;
		this.type = TYPE_MONO_COLOR;

		ledCount = 1;

		this.brightness = new int[ledCount];
		this.brightness[0] = 0;

		this.maxBrightness = new int[ledCount];
		this.maxBrightness[0] = 255;

		this.trigger = new String[ledCount];
		this.trigger[0] = "none";
	}

	/**
	 * Constructor for tri-color LED
	 * 
	 * @param root
	 * @param name
	 */
	public LEDDevice(File root, String name) {
		super(root);
		this.name = name;
		this.type = TYPE_TRI_COLOR;

		ledCount = 3;

		this.brightness = new int[ledCount];
		this.brightness[0] = 0;
		this.brightness[1] = 0;
		this.brightness[2] = 0;

		this.maxBrightness = new int[ledCount];
		this.maxBrightness[0] = 255;
		this.maxBrightness[1] = 255;
		this.maxBrightness[2] = 255;

		this.trigger = new String[ledCount];
		this.trigger[0] = "none";
		this.trigger[1] = "none";
		this.trigger[2] = "none";
	}

	/**
	 * @return name of LED.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return type of LED
	 */
	public int getType() {
		return type;
	}

	/**
	 * Set the brightness for the specified LED.
	 * 
	 * @param color
	 * @param brightness
	 * @throws IOException
	 */
	public void setBrightness(int color, int brightness) throws IOException {
		this.brightness[color] = brightness;
		log.log(LogService.LOG_DEBUG, "LED " + this.name + " color " + color + " brightness set to: " + brightness);
	}

	/**
	 * @param color
	 * @return the current brightness of the specified LED.
	 */
	public int getBrightness(int color) {
		return this.brightness[color];
	}

	/**
	 * Set a trigger on a LED.
	 * 
	 * @param color
	 * @param trigger
	 * @throws IOException
	 */
	public void setTrigger(int color, String trigger) throws IOException {
		this.trigger[color] = trigger;
		log.log(LogService.LOG_DEBUG, "LED " + this.name + " color " + color + " trigger set to: " + trigger);
	}

	/**
	 * @param color
	 * @return The LED trigger that is currently set. "none" means no trigger is
	 *         active.
	 * @throws IOException
	 */
	public String getLEDTrigger(int color) throws IOException {
		return this.trigger[color];
	}

	/**
	 * Get the Trigger types this LED supports. These trigger types can be set
	 * using setLEDTrigger.
	 * 
	 * @param color
	 * @return
	 * @throws IOException
	 */
	public String[] getLEDTriggers(int color) throws IOException {
		return "none nand-disk bq27200-0-charging-or-full bq27200-0-charging bq27200-0-full mmc0 mmc1 mmc2 phy0rx phy0tx phy0assoc".split(" ");
	}

	/**
	 * @return The number of colors this LED supports. Zero based.
	 */
	public int getColorCount() {
		return ledCount;
	}
}
