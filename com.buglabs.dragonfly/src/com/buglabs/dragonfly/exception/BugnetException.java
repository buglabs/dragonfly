/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.exception;

import java.io.IOException;

/**
 * Generic BugnetException, extend this if you want a new one.
 * 
 * @author Brian
 * 
 */
public class BugnetException extends IOException {
	private static final long serialVersionUID = -4526324236776240815L;

	public BugnetException(String error) {
		super(error);
	}
}
