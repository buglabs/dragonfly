/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import com.buglabs.dragonfly.ui.figures.ISelectionFigure;

public class CanvasSelectionProvider implements ISelectionProvider {
	private static CanvasSelectionProvider me;

	private List listeners;

	private ISelection selection;

	private ISelectionFigure lastSelectedFigure;

	private CanvasSelectionProvider() {
		listeners = new ArrayList();
	}

	public static CanvasSelectionProvider getDefault() {
		if (me == null) {
			me = new CanvasSelectionProvider();
		}

		return me;
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);

	}

	public ISelection getSelection() {

		return selection;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listeners);
	}

	public void setSelection(ISelection selection) {
		this.selection = selection;
		if (selection instanceof StructuredSelection) {
			Object o = ((StructuredSelection) selection).getFirstElement();

			if (o instanceof ISelectionFigure) {
				if (lastSelectedFigure != null) {
					lastSelectedFigure.setFocus(false);
				}

				lastSelectedFigure = (ISelectionFigure) o;

				ISelectionFigure sf = (ISelectionFigure) o;
				if (sf.getModel() != null) {
					for (Iterator i = listeners.iterator(); i.hasNext();) {
						ISelectionChangedListener listener = (ISelectionChangedListener) i.next();

						listener.selectionChanged(new SelectionChangedEvent(this, new StructuredSelection(sf.getModel())));
					}
				}
			}
		}
	}
}