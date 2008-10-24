/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.figures;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.buglabs.dragonfly.model.BaseNode;
import com.buglabs.dragonfly.model.Module;
import com.buglabs.dragonfly.model.ModuleSlot;

/**
 * 
 * Contains the physical representation of a bug module inlcuding a reference to
 * the image to use and the clickable outline via an array of points
 * 
 * Abstract parent class, AbstractBugFigure handles everything else
 * 
 * @author Brian
 * 
 */
public abstract class AbstractModuleFigure extends AbstractBugFigure {

	private BaseNode module;

	public AbstractModuleFigure(Module moduleModel, Display display, Point startPoint) {
		super(display, startPoint);

		if (moduleModel != null) {
			add(new Label(moduleModel.getName(), null));
			this.module = moduleModel;
		} else {
			this.module = new ModuleSlot(1);
		}
	}

	private static final Point[] POINTS = { new Point(0, 104), new Point(0, 134), new Point(235, 134), new Point(340, 30),
			new Point(340, 0), new Point(105, 0) };

	public abstract Image getImageOffResource();

	public abstract Image getImageOnResource();

	public Point[] getPolygonPoints() {
		return POINTS;
	}

	public Object getModel() {
		return module;
	}
}
