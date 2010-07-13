/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.ui.views.bugnet.BugnetView;
import com.buglabs.dragonfly.ui.views.mybugs.MyBugsView;

public class PerspectiveFactory implements IPerspectiveFactory {

	public static final String ID_PROJECT_EXPLORER = "org.eclipse.ui.navigator.ProjectExplorer"; //$NON-NLS-1$

	public static final String PERSPECTIVE_ID = "com.buglabs.dragonfly.ui.perspective"; //$NON-NLS-1$

	public static final String JAVADOC_VIEW_ID = "org.eclipse.jdt.ui.JavadocView"; //$NON-NLS-1$

	public void createInitialLayout(IPageLayout layout) {
		defineActions(layout);
		defineLayout(layout);
		defineListeners();
	}

	private void defineListeners() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new IPerspectiveListener() {

			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				if (perspective.getId().equals(PERSPECTIVE_ID)) {
					// find the project explorer and register with the viewer.
					IWorkbenchPage[] pages = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();

					for (int i = 0; i < pages.length; ++i) {
						IViewPart view = pages[i].findView(ID_PROJECT_EXPLORER);
						if (view != null) {
							// view.
						}
					}
				}
			}

			public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
			}

		});
	}

	private void defineLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) .19, editorArea); //$NON-NLS-1$
		left.addView(ID_PROJECT_EXPLORER);

		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) .70, editorArea); //$NON-NLS-1$
		bottom.addView(JAVADOC_VIEW_ID);
		bottom.addView(IPageLayout.ID_PROP_SHEET);
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);

		IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, (float) .60, editorArea); //$NON-NLS-1$
		right.addView(BugnetView.VIEW_ID);
		right.addView(MyBugsView.ID);
	}

	private void defineActions(IPageLayout layout) {
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
	}

}
