/* Copyright (c) 2007, 2008 Bug Labs, Inc.
 * All rights reserved.
 *   
 * This program is free software; you can redistribute it and/or  
 * modify it under the terms of the GNU General Public License version  
 * 2 only, as published by the Free Software Foundation.   
 *   
 * This program is distributed in the hope that it will be useful, but  
 * WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  
 * General Public License version 2 for more details (a copy is  
 * included at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).   
 *   
 * You should have received a copy of the GNU General Public License  
 * version 2 along with this work; if not, write to the Free Software  
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  
 * 02110-1301 USA   
 *
 */
package com.buglabs.bug.bmi.pub;

/**
 * A message from the BMI system. Format [moduleId] [version] [slot] [event]
 * 
 * @author ken
 * 
 */
public class BMIMessage {
	public static final int EVENT_INSERT = 0;

	public static final int EVENT_REMOVE = 1;

	private String raw;

	private int slot;

	private int event;

	private String moduleId;

	private String version;

	public BMIMessage(String raw) {
		this.raw = raw;
	}

	public BMIMessage(String moduleId, String version, int slot, int event) {
		this.moduleId = moduleId;
		this.version = version;
		this.slot = slot;
		this.event = event;
	}

	public String toString() {
		String eventStr = "";

		switch (event) {
		case EVENT_INSERT:
			eventStr = "add";
			break;
		case EVENT_REMOVE:
			eventStr = "remove";
			break;
		}

		return moduleId + " " + version + " " + slot + " " + eventStr + "\n";
	}

	public int getEvent() {
		return event;
	}

	public String getModuleId() {
		return moduleId;
	}

	public String getRaw() {
		return raw;
	}

	public String getVersion() {
		return version;
	}

	public int getSlot() {
		return slot;
	}

	/**
	 * See http://lurcher/wiki/BMI_-_Runtime_Interface#Message_Definition
	 * 
	 * @return
	 */
	public boolean parse() {
		String[] toks = raw.split(" ");

		if (toks.length != 4) {
			return false;
		}

		this.moduleId = toks[0].trim();
		this.version = toks[1].trim();
		try {
			this.slot = Integer.parseInt(toks[2]);
		} catch (NumberFormatException e) {
			return false;
		}

		String action = toks[3].trim().toUpperCase();

		if (action.equals("ADD")) {
			this.event = BMIMessage.EVENT_INSERT;
		} else if (action.equals("REMOVE")) {
			this.event = BMIMessage.EVENT_REMOVE;
		} else {
			return false;
		}

		return true;
	}
}
