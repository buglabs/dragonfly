/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.buglabs.dragonfly.model.IPackage;
import com.buglabs.dragonfly.model.PackageImpl;
import com.buglabs.util.SelfReferenceException;
import com.buglabs.util.XmlNode;
import com.buglabs.util.XmlParser;

class SimpleXMLSerializer implements ISerializer {

	private static final String NOTES = "notes";

	private static final String PACKAGE = "program";

	private static final String ITEM_ELEMENT = "item";

	private static final String EVENT_TRIGGERS = "event_triggers";

	private static final String PROGRAM_TYPE_ATTRIBUTE = "type";

	private static final String BUNDLE_TYPE_ATTRIBUTE = "type";

	private static final String PROGRAM_ID_ATTRIBUTE = "id";

	private static final String PROGRAM = "code";

	private static final String VERSION = "version";

	private static final String DATE_CREATED = "date_created";

	private static final String AUTHOR = "author";

	private static final String TITLE = "title";

	private static final String DATE_MODIFIED = "date_updated";

	private static final String SERVICES = "services";

	public IPackage getDeserializedPackage(Object obj) throws Exception {
		String fileXML = null;

		if (obj instanceof File) {
			File packageFile = (File) obj;

			fileXML = getFileContents(packageFile.getAbsolutePath());

		} else if (obj instanceof String) {
			fileXML = (String) obj;
		} else if (obj instanceof InputStream) {
			fileXML = getFileContents((InputStream) obj);
		} else {
			throw new Exception("Unable to get xml from type: " + obj.getClass().getName());
		}

		XmlNode root = XmlParser.parse(fileXML);

		PackageImpl pkg = new PackageImpl();
		pkg.setName(root.getFirstElement(TITLE).getValue());
		pkg.setAuthor(root.getFirstElement(AUTHOR).getValue());
		pkg.setCreated(root.getFirstElement(DATE_CREATED).getValue());
		pkg.setModified(root.getFirstElement(DATE_MODIFIED).getValue());
		pkg.setNotes(root.getFirstElement(NOTES).getValue());
		pkg.setId(root.getAttribute(PROGRAM_ID_ATTRIBUTE));
		pkg.setBundleType(root.getAttribute(BUNDLE_TYPE_ATTRIBUTE));

		if (root.getAttribute(VERSION) != null) {
			pkg.setVersion(root.getAttribute(VERSION));
		}

		// TODO this is for the deprecated 'program.code' node in the program
		// xml format. Remove later.
		if (root.childExists(PROGRAM) && root.getChild(PROGRAM).getAttribute(PROGRAM_TYPE_ATTRIBUTE) != null) {
			pkg.setProgramType(root.getFirstElement(PROGRAM).getAttribute(PROGRAM_TYPE_ATTRIBUTE));
		}

		if (root.childExists(EVENT_TRIGGERS)) {
			pkg.setEventURIs(xmlToList(root.getFirstElement(EVENT_TRIGGERS)));
		}

		if (root.childExists(SERVICES)) {
			pkg.setEventURIs(xmlToList(root.getFirstElement(SERVICES)));
		}
		return pkg;
	}

	private String getFileContents(String filename) throws IOException {
		File f = new File(filename);

		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new FileReader(f));

		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}

		br.close();
		return sb.toString();

	}

	private String getFileContents(InputStream istream) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(istream));

		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}

		br.close();
		return sb.toString();

	}

	public String getSerializedPackage(IPackage pkg) throws IOException {
		XmlNode root = new XmlNode(PACKAGE);
		root.setAttribute(PROGRAM_ID_ATTRIBUTE, pkg.getId());

		root.addChildElement(new XmlNode(TITLE, pkg.getName()));
		root.addChildElement(new XmlNode(AUTHOR, pkg.getAuthor()));
		root.addChildElement(new XmlNode(NOTES, pkg.getNotes()));
		root.addChildElement(new XmlNode(DATE_CREATED, pkg.getCreated()));
		root.addChildElement(new XmlNode(DATE_MODIFIED, pkg.getModified()));
		root.getAttributes().put(VERSION, "" + pkg.getVersion());

		root.addChildElement(listToXml(EVENT_TRIGGERS, pkg.getEventURIs()));

		root.addChildElement(listToXml(SERVICES, pkg.getServiceDependendies()));

		return root.toString();
	}

	private XmlNode listToXml(String listName, List list) throws SelfReferenceException {
		XmlNode root = new XmlNode(listName);

		for (Iterator i = list.iterator(); i.hasNext();) {
			root.addChildElement(new XmlNode(ITEM_ELEMENT, i.next().toString()));
		}

		return root;
	}

	private List xmlToList(XmlNode child) {
		// TODO insure proper ordering.
		List l = new ArrayList();

		for (Iterator i = child.getChildren().iterator(); i.hasNext();) {
			XmlNode c = (XmlNode) i.next();

			l.add(c.getValue());
		}

		return l;
	}

}
