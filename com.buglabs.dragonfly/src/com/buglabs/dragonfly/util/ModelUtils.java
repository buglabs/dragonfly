/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

import org.eclipse.core.resources.IFile;

import com.buglabs.dragonfly.model.IPackage;

public class ModelUtils {

	public static IPackage getPackage(IFile file) throws Exception {
		ISerializer serializer = SerializerFactory.getSerializer("xml");

		return serializer.getDeserializedPackage(file.getContents());
	}

}
