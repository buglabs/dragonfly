/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

import java.io.IOException;

import com.buglabs.dragonfly.model.IPackage;

class PlainTextSerializer implements ISerializer {
	public IPackage getDeserializedPackage(Object obj) throws Exception {
		throw new RuntimeException("This serializer is deprecated.");
	}

	public String getSerializedPackage(IPackage pkg) throws IOException {
		throw new RuntimeException("This serializer is deprecated.");
	}

}
