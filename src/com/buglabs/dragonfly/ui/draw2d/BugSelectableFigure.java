package com.buglabs.dragonfly.ui.draw2d;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import com.buglabs.dragonfly.model.BaseTreeNode;
import com.buglabs.dragonfly.ui.editors.CanvasSelectionProvider;
import com.buglabs.dragonfly.ui.figures.ISelectionFigure;

public class BugSelectableFigure extends Figure implements ISelectionFigure {

	private Image selectedImage;

	private Image regImage;

	private BaseTreeNode model;

	private ImageData id;

	private ImageFigure imageFigure;

	private String name;

	public BugSelectableFigure(String name) {
		this.name = name;
		addMouseListener(new MouseListener() {

			public void mousePressed(MouseEvent me) {
				if (me.button == 1) {
					CanvasSelectionProvider.getDefault().setSelection(new StructuredSelection(BugSelectableFigure.this));
				}
			}

			public void mouseReleased(MouseEvent me) {

			}

			public void mouseDoubleClicked(MouseEvent me) {
			}

		});
		setLayoutManager(new FlowLayout());
		imageFigure = new ImageFigure();
		add(imageFigure);
	}

	public void setImage(Image image) {
		imageFigure.setImage(image);
		if (image != null) {
			id = image.getImageData().getTransparencyMask();
		}
	}

	public boolean containsPoint(int x, int y) {
		Point t = new Point(x, y);

		translateFromParent(t);

		if (!getBounds().contains(x, y)) {
			return false;
		}

		if (id != null) {
			int alpha = id.getPixel(t.x, t.y);

			if (alpha != 0) {
				return true;
			}

		}
		return false;
	}

	public void setSelectedImage(Image selectedImage) {
		this.selectedImage = selectedImage;
	}

	public void setModel(BaseTreeNode node) {
		this.model = node;
	}

	public Object getModel() {
		return model;
	}

	protected boolean useLocalCoordinates() {
		return true;
	}

	public void setFocus(boolean on) {
		if (on) {
			if (regImage == null) {
				regImage = imageFigure.getImage();
			}
			setImage(selectedImage);
		} else {
			if (regImage != null) {
				setImage(regImage);
			}
		}

		repaint();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
