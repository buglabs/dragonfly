/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

/**
 * An interface for model objects that represent entites that are addresable via
 * a URL.
 * 
 * @author ken
 * 
 */
public interface ILinkableModelNode extends IModelNode {
	public String getUrl();

	public String getLabel();
}
