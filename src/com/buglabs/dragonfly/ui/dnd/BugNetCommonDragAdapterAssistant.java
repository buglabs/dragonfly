/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.dnd;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;

public class BugNetCommonDragAdapterAssistant extends CommonDragAdapterAssistant {

	public BugNetCommonDragAdapterAssistant() {
		// TODO Auto-generated constructor stub
	}

	public Transfer[] getSupportedTransferTypes() {
		return new Transfer[] { TextTransfer.getInstance() };
	}

	public boolean setDragData(DragSourceEvent anEvent, IStructuredSelection aSelection) {
		// TODO Auto-generated method stub
		return false;
	}

}
