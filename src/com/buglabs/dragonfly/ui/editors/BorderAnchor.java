package com.buglabs.dragonfly.ui.editors;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;

public class BorderAnchor extends AbstractConnectionAnchor implements ConnectionAnchor {

	private int position;

	public BorderAnchor(IFigure figure, int position) {
		super(figure);
		this.position = position;
	}

	public Point getLocation(Point reference) {
		Point ref;

		switch (position) {
		case PositionConstants.NORTH:
			ref = getOwner().getBounds().getTop();
			getOwner().translateToAbsolute(ref);
			break;
		case PositionConstants.SOUTH:
			ref = getOwner().getBounds().getBottom();
			getOwner().translateToAbsolute(ref);
			break;
		case PositionConstants.EAST:
			ref = getOwner().getBounds().getRight();
			getOwner().translateToAbsolute(ref);
			break;
		case PositionConstants.WEST:
			ref = getOwner().getBounds().getLeft();
			getOwner().translateToAbsolute(ref);
			break;
		case PositionConstants.CENTER:
		default:
			ref = getOwner().getBounds().getCenter();
			getOwner().translateToAbsolute(ref);
			break;
		}

		return ref;
	}

}
