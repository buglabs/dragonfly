package com.buglabs.dragonfly.ui.borders;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * A border with round corners.
 * 
 * @author akravets
 * 
 */
public class RoundedLineBorder extends LineBorder {
	protected int arcLength;

	protected int lineStyle = Graphics.LINE_SOLID;

	/**
	 * Constructor.
	 * 
	 * @param color
	 *            Color of the border
	 * @param width
	 *            Width of the border
	 * @param arcLength
	 *            Defines the arc of the border's corner
	 */
	public RoundedLineBorder(Color color, int width, int arcLength) {
		super(color, width);
		this.arcLength = arcLength;
	}

	/**
	 * Constructor.
	 * 
	 * @param width
	 *            Width of the border
	 * @param arcLength
	 *            Defines the arc of the border's corner
	 */
	public RoundedLineBorder(int width, int arcLength) {
		super(width);
		this.arcLength = arcLength;
	}

	/**
	 * Constructor.
	 * 
	 * @param color
	 *            Color of the border
	 * @param width
	 *            Width of the border
	 * @param arcLength
	 *            Defines the arc of the border's corner
	 * @param lineStyle
	 *            Style of the border
	 */
	public RoundedLineBorder(Color color, int width, int arcLength, int lineStyle) {
		super(color, width);
		this.arcLength = arcLength;
		this.lineStyle = lineStyle;
	}

	/**
	 * Constructor.
	 * 
	 * @param width
	 *            Width of the border
	 * @param arcLength
	 *            Defines the arc of the border's corner
	 * @param lineStyle
	 *            Style of the border
	 */
	public RoundedLineBorder(int width, int arcLength, int lineStyle) {
		super(width);
		this.arcLength = arcLength;
		this.lineStyle = lineStyle;
	}

	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		int rlbWidth = getWidth();
		tempRect.setBounds(getPaintRectangle(figure, insets));
		if (rlbWidth % 2 == 1) {
			tempRect.width--;
			tempRect.height--;
		}
		tempRect.shrink(rlbWidth / 2, rlbWidth / 2);
		graphics.setLineWidth(rlbWidth);
		graphics.setLineStyle(lineStyle);
		graphics.setAntialias(SWT.ON);
		if (getColor() != null)
			graphics.setForegroundColor(getColor());
		graphics.drawRoundRectangle(tempRect, arcLength, arcLength);
	}

	public Insets getInsets(IFigure arg0) {
		return new Insets(5, 5, 5, 5);
	}
}
