/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

/**
 * Represents the bug module socket.
 * 
 * @author ken
 * 
 */
public class ModuleSlot extends BaseNode {
	private static final long serialVersionUID = 8971932941226467132L;

	public ModuleSlot(int slot_id) {
		super("Slot " + slot_id);
	}
}
