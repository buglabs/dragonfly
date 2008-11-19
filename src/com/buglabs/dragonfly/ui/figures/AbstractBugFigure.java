/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.figures;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Abstract class, represents module and bug base
 * 
 * @author Brian
 * 
 */
public abstract class AbstractBugFigure extends Figure implements ISelectionFigure {

	private ImageFigure imageFigure;

	private Image imageOff;

	private Image imageOn;

	private ClickablePolygon clickablePolygon;

	public abstract Image getImageOffResource();

	public abstract Image getImageOnResource();

	public abstract Point[] getPolygonPoints();

	public AbstractBugFigure(Display display, Point startPoint) {
		setOpaque(false);

		// set layout
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);

		// Create my images
		imageOn = getImageOnResource();
		imageOff = getImageOffResource();

		imageFigure = new ImageFigure(imageOff);

		// Set up my polygon
		PointList pointList = new PointList();
		Point[] polygonPoints = getPolygonPoints();
		Point point;
		for (int i = 0; i < polygonPoints.length; i++) {
			point = polygonPoints[i];
			pointList.addPoint(startPoint.x + point.x, startPoint.y + point.y);
		}
		// create me clickable polygon to be acquired and used
		// by PhysicalEditor
		clickablePolygon = new ClickablePolygon(this, pointList);

		// add figure
		add(imageFigure);

		// set constraints
		layout.setConstraint(imageFigure, new Rectangle(0, 0, -1, -1));
	}

	public ClickablePolygon getClickablePolygon() {
		return clickablePolygon;
	}

	public void setFocus(boolean on) {
		if (on) {
			imageFigure.setImage(imageOn);
			this.repaint();
		} else {
			imageFigure.setImage(imageOff);
			this.repaint();
		}
	}

}
