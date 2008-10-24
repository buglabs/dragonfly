/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

/**
 * Responsible for commiting the model to storage.
 * 
 * @author ken
 * 
 */
public class DragonflySaveParticipant implements ISaveParticipant {

	public void doneSaving(ISaveContext context) {

	}

	public void prepareToSave(ISaveContext context) throws CoreException {

	}

	public void rollback(ISaveContext context) {

	}

	public void saving(ISaveContext context) throws CoreException {
		switch (context.getKind()) {
		case ISaveContext.FULL_SAVE:
			DragonflyActivator myPluginInstance = DragonflyActivator.getDefault();
			// save the plug-in state
			int saveNumber = context.getSaveNumber();
			String saveFileName = "save-" + Integer.toString(saveNumber);
			File f = myPluginInstance.getStateLocation().append(saveFileName).toFile();
			// if we fail to write, an exception is thrown and we do not update
			// the path
			try {
				myPluginInstance.saveModel(f);
			} catch (FileNotFoundException e) {
				throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, IStatus.OK, e.getMessage(), null));
			}
			context.map(new Path("save"), new Path(saveFileName));
			context.needSaveNumber();
			break;
		case ISaveContext.PROJECT_SAVE:

			break;
		case ISaveContext.SNAPSHOT:

			break;
		}
	}

}
