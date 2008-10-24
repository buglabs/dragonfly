package com.buglabs.dragonfly.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import com.buglabs.dragonfly.ui.Activator;

public class Rating extends Canvas implements PaintListener {

	private int maxRating = 5;

	private int spacing = 10;

	private double rating = 0;

	public Rating(Composite parent, int style) {
		super(parent, style);
		addPaintListener(this);
	}

	public Point computeSize(int wHint, int hHint) {
		// TODO Auto-generated method stub
		return super.computeSize(wHint, hHint);
	}

	public int getMaxRating() {
		return maxRating;
	}

	public void setMaxRating(int maxRating) {
		this.maxRating = maxRating;
	}

	public void paintControl(PaintEvent e) {
		int currentPixel = 0;

		double remainder = rating - Math.floor(rating);

		for (int i = 0; i < maxRating; ++i) {
			GC gc = e.gc;
			gc.setBackground(gc.getBackground());
			String imageStr = Activator.IMAGE_KEY_RATING_EMPTY;

			if (i < Math.floor(rating) || remainder > 0.75) {
				imageStr = Activator.IMAGE_KEY_RATING_FULL;
			} else if (remainder >= 0.25 && remainder <= 0.75) {
				imageStr = Activator.IMAGE_KEY_RATING_HALF;
				remainder = 0;
			} else {
				imageStr = Activator.IMAGE_KEY_RATING_EMPTY;
			}

			Image image = Activator.getDefault().getImageRegistry().get(imageStr);

			gc.drawImage(image, currentPixel, 4);
			currentPixel += spacing;
		}
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public int getSpacing() {
		return spacing;
	}

	public void setSpacing(int spacing) {
		this.spacing = spacing;
	}
}
