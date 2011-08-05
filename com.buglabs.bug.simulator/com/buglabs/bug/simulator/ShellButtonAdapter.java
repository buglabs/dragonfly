/* Copyright (c) 2011 Bug Labs, Inc.
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
package com.buglabs.bug.simulator;

import java.util.ArrayList;
import java.util.List;

import org.knapsack.shell.pub.IKnapsackCommand;

import com.buglabs.bug.buttons.ButtonEvent;
import com.buglabs.bug.buttons.IButtonEventListener;
import com.buglabs.bug.buttons.IButtonEventProvider;

/**
 * Allows for 'virtual' buttons on device that can be triggered from OSGi shell.
 * 
 * @author kgilmer
 *
 */
public class ShellButtonAdapter extends AbstractCommand implements IButtonEventProvider, IKnapsackCommand {

	private List<IButtonEventListener> listeners;
	private final String name;
	private int buttonId;

	/**
	 * @param name must be "POWER" or "USER".
	 * 
	 */
	public ShellButtonAdapter(String name) {
		this.name = name;		
		
		if (name.toUpperCase().equals("POWER")) {
			buttonId = ButtonEvent.BUTTON_BUG20_POWER;
		} else if (name.toUpperCase().equals("USER")) {
			buttonId = ButtonEvent.BUTTON_BUG20_USER;
		} else {
			throw new IllegalArgumentException("Unknown button type: " + name);
		}
	}

	
	public void addListener(IButtonEventListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<IButtonEventListener>();
		}
		
		if (!listeners.contains(listener)) {
			listeners.add(listener);	
		}
	}

	
	public void removeListener(IButtonEventListener listener) {
		if (listeners != null && listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	
	public String execute() throws Exception {
		if (listeners != null) {
			for (IButtonEventListener listener: listeners) {
				listener.buttonEvent(new ButtonEvent(buttonId));
			}
		}
		
		return "";
	}

	
	public String getName() {		
		return "btn-" + name;
	}
	
	
	public String getDescription() {
		return "Trigger the " + name + " button.";
	}
}
