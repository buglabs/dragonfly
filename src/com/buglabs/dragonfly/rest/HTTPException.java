/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.rest;

import java.io.IOException;

/**
 * 
 * @author Brian
 *
 */
public class HTTPException extends IOException {
	private static final long serialVersionUID = -4526324236776240815L;
	private int _httpErrorCode;
	
	public HTTPException(int errorCode, String error) {
		super(error);
		_httpErrorCode = errorCode;
	}
	
	public int getErrorCode() {
		return _httpErrorCode;
	}
}
