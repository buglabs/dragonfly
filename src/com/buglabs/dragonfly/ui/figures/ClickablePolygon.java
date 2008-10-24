/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.figures;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.viewers.StructuredSelection;

import com.buglabs.dragonfly.ui.editors.CanvasSelectionProvider;

/**
 * An invisible polygon for capturing click events
 * 
 * @author Brian
 * 
 */
public class ClickablePolygon extends Polygon {
	public ClickablePolygon(final ISelectionFigure parentFigure, PointList pointList) {

		setPoints(pointList);
		setOutline(false);
		setOpaque(false);

		// mouse listener
		this.addMouseListener(new MouseListener() {

			public void mousePressed(MouseEvent me) {
				if (me.button == 1) {
					CanvasSelectionProvider.getDefault().setSelection(new StructuredSelection(parentFigure));
					parentFigure.setFocus(true);
				} else {
					parentFigure.setFocus(false);
				}
			}

			public void mouseReleased(MouseEvent me) {

			}

			public void mouseDoubleClicked(MouseEvent me) {
			}

		});
	}

	/**
	 * override the primTranslate method in Polyline, which does nothing so that
	 * when we move the clickable polygon's parent, it also moves
	 * 
	 */
	public void primTranslate(int dx, int dy) {

		PointList points = getPoints();
		PointList transPoints = new PointList();
		Point point;
		for (int i = 0; i < points.size(); i++) {
			point = points.getPoint(i);
			transPoints.addPoint(point.x + dx, point.y + dy);
		}

		setPoints(transPoints);

		if (useLocalCoordinates()) {
			fireCoordinateSystemChanged();
			return;
		}
	}

}
