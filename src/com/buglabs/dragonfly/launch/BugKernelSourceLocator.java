/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

import com.buglabs.dragonfly.DragonflyActivator;

public class BugKernelSourceLocator extends AbstractSourceLookupDirector {

	public void initializeParticipants() {
		// Theoretically the source containers should be added by a source computer.
		addParticipants(new ISourceLookupParticipant[] { new JavaSourceLookupParticipant() });
		ArrayList sourceContainers = new ArrayList();
		sourceContainers.addAll(Arrays.asList(getSourceContainers()));

		sourceContainers.add(new WorkspaceSourceContainer());

		List jars = getSourceJars();
		Iterator iter = jars.iterator();

		while (iter.hasNext()) {
			File jar = (File) iter.next();
			ExternalArchiveSourceContainer easc = new ExternalArchiveSourceContainer(jar.getAbsolutePath(), true);
			sourceContainers.add(easc);
		}

		setSourceContainers((ISourceContainer[]) sourceContainers.toArray(new ISourceContainer[sourceContainers.size()]));
	}

	protected List getSourceJars() {
		return DragonflyActivator.getDefault().getBUGOSGiJars();
	}
}
