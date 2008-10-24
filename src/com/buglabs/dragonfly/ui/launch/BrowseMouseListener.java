package com.buglabs.dragonfly.ui.launch;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

public class BrowseMouseListener implements MouseListener {

	private Text txt;

	public BrowseMouseListener(Text txt) {
		this.txt = txt;
	}

	public void mouseDoubleClick(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseDown(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseUp(MouseEvent e) {
		FileDialog fd = new FileDialog(((Button) e.widget).getShell());
		txt.setText(fd.open());
	}
}
