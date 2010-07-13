/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

/**
 * Model class to hold authentication information for BUGNet on a per-BUG basis.
 * 
 * @author ken
 * 
 */
public class AuthenticationData extends BaseNode {
	private static final long serialVersionUID = 8657156741392842097L;

	private String username;

	private String password;

	public AuthenticationData() {
		super("BUGnet Authentication Data");

	}

	/**
	 * if we're missing data return false
	 * 
	 */
	public boolean hasData() {
		if (username == null || username.length() <= 0 || password == null || password.length() <= 1) {
			return false;
		} else {
			return true;
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
