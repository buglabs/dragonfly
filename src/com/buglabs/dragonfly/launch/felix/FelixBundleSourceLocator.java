/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.launch.felix;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

public class FelixBundleSourceLocator extends AbstractSourceLookupDirector {

	public void initializeParticipants() {
		// Theoretically the source containers should be added by a source computer.
		addParticipants(new ISourceLookupParticipant[] { new JavaSourceLookupParticipant() });
		ArrayList sourceContainers = new ArrayList();
		sourceContainers.addAll(Arrays.asList(getSourceContainers()));

		sourceContainers.add(new WorkspaceSourceContainer());
		setSourceContainers((ISourceContainer[]) sourceContainers.toArray(new ISourceContainer[sourceContainers.size()]));
	}

}
