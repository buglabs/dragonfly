/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.exception;

/**
 * Exception if unable to connect to bugnet
 * 
 * @author Brian
 * 
 */
public class BugnetAuthenticationException extends BugnetException {
	private static final long serialVersionUID = -6733260897691716798L;

	public BugnetAuthenticationException(String error) {
		super(error);
	}
}
