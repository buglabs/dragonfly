package com.buglabs.dragonfly.ui.draw2d;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;

public class XYFigureAnchor extends AbstractConnectionAnchor {

	Point p;

	public XYFigureAnchor(IFigure owner, Point p) {
		super(owner);
		this.p = p;
	}

	public Point getLocation(Point reference) {
		Point ref = getOwner().getBounds().getCenter().translate(p);
		getOwner().translateToAbsolute(ref);
		return ref;
	}
}
