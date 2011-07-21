/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

import java.beans.PropertyChangeEvent;

/**
 * An interface for persistent classes that hold models.
 * 
 * @author Ken
 */
public interface IModelContainer {
	public void addListener(IModelChangeListener listener);

	public void removeListener(IModelChangeListener listener);

	public void fireModelChangeEvent(PropertyChangeEvent event);

	public Object getModel();

}
