/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.figures;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.buglabs.dragonfly.model.Module;
import com.buglabs.dragonfly.ui.Activator;

public class BugRightModuleFigure extends AbstractModuleFigure {

	public BugRightModuleFigure(Module moduleModel, Display display, Point startPoint) {
		super(moduleModel, display, startPoint);
	}

	public Image getImageOffResource() {
		return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_KEY_MODULE_SLOT_2);
	}

	public Image getImageOnResource() {
		return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_KEY_RIGHT_MODULE_SELECTED);
	}

}
