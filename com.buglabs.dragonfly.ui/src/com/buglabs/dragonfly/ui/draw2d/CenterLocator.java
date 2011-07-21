package com.buglabs.dragonfly.ui.draw2d;

import org.eclipse.draw2d.AbstractLocator;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Point;

public class CenterLocator extends AbstractLocator {

	private Figure parent;

	public CenterLocator(Figure parent) {
		this.parent = parent;
	}

	protected Point getReferencePoint() {
		return parent.getBounds().getCenter();
	}
}
