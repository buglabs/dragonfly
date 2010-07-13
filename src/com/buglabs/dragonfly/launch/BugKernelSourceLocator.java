/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.launch;

import java.util.List;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.osgi.concierge.runtime.sourcelocators.ConciergeSourceLocator;

public class BugKernelSourceLocator extends ConciergeSourceLocator {
	protected List getSourceJars() {

		List jars = super.getSourceJars();
		jars.addAll(DragonflyActivator.getDefault().getBUGOSGiJars());
		return jars;
	}
}
