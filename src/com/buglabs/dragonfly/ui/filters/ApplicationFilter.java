/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.util.BugBundleConstants;

public class ApplicationFilter extends ViewerFilter {

	public ApplicationFilter() {
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {

		if (element instanceof ProgramNode) {
			ProgramNode pn = (ProgramNode) element;
			if (pn.getBundleType().equals(BugBundleConstants.BUG_BUNDLE_APPLICATION)) {
				return true;
			} else {
				return false;
			}
		}

		return true;
	}

}
