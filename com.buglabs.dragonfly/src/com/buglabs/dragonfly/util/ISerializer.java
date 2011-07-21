/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

import com.buglabs.dragonfly.model.IPackage;

/**
 * Implementor are able to read and write program models to disk in some format.
 * 
 * @author ken
 * 
 */
public interface ISerializer {
	public IPackage getDeserializedPackage(Object obj) throws Exception;

	public String getSerializedPackage(IPackage pkg) throws Exception;

}
