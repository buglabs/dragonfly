/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.dnd;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

public class ProgramFromBugNetDropAdapter extends CommonDropAdapterAssistant {

	public ProgramFromBugNetDropAdapter() {
		// TODO Auto-generated constructor stub
	}

	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {
		// TODO Auto-generated method stub
		return null;
	}

	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		// TODO Auto-generated method stub
		return null;
	}

}
