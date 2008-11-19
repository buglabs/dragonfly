/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model;

/**
 * This model class represents a Program that has been provided from bugnet. The
 * url property lets a user navigate to get more details about this program with
 * the provided url.
 * 
 * @author ken
 * 
 */
public class BUGNetProgramReferenceNode extends BaseNode implements ILinkableModelNode {
	private static final long serialVersionUID = 8099884190409399908L;

	private String url;

	private String programName;

	private String description;

	private String imageKey;

	private String userName;

	private String download_count;

	private String rating;

	public BUGNetProgramReferenceNode(String id, String name, String url) {
		super(id);
		this.programName = name;
		this.url = url + "?context=IDE";
		this.description = "";
		this.imageKey = "";
	}

	public BUGNetProgramReferenceNode(String id, String name, String username, String url, String description, String imageKey,
			String download_count, String rating) {
		super(id);
		this.programName = name;
		this.url = url + "?context=IDE";
		this.description = description;
		this.imageKey = imageKey;
		this.userName = username;
		this.download_count = download_count;
		this.rating = rating;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLabel() {
		return programName;
	}

	public String getImageKey() {
		return imageKey;
	}

	public String getDescription() {
		return description;
	}

	public String getUserName() {
		return userName;
	}

	public String getDownload_count() {
		return download_count;
	}

	public void setDownload_count(String download_count) {
		this.download_count = download_count;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

}
