/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.figures;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.ui.Activator;

/**
 * 
 * Contains the physical representation of a the base unit inlcuding a reference
 * to the image to use and the clickable outline via an array of points
 * 
 * Abstract parent class, AbstractBugFigure handles everything else
 * 
 * @author Brian
 * 
 */
public class BugBaseUnitFigure extends AbstractBugFigure {

	private Bug bug;

	public BugBaseUnitFigure(Bug bug, Display display, Point startPoint) {
		super(display, startPoint);

		add(new Label(bug.getName(), null));
		this.bug = bug;
	}

	private static final Point[] POINTS = { new Point(0, 102), new Point(0, 152), new Point(475, 152), new Point(580, 50),
			new Point(580, 0), new Point(105, 0) };

	public static final int WIDTH = 580;

	public static final int HEIGHT = 152;

	public Image getImageOffResource() {
		return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_KEY_BASE_UNIT);
	}

	public Image getImageOnResource() {
		return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_KEY_BASE_UNIT);
	}

	public Point[] getPolygonPoints() {
		return POINTS;
	}

	public Object getModel() {
		return bug;
	}
}
