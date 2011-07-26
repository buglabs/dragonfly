/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.buglabs.dragonfly.model.IPackage;
import com.buglabs.dragonfly.model.PackageImpl;
import com.buglabs.dragonfly.model.ServiceDetail;
import com.buglabs.dragonfly.model.ServiceProperty;
import com.buglabs.util.xml.XmlNode;
import com.buglabs.util.xml.XmlParser;

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
	private static final String SERVICES2 = "services2";
	private static final String SERVICE = "service";
	private static final String NAME = "name";
	private static final String VALUE = "value";

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

		// this is for the list of services and properties
		if (root.childExists(SERVICES2)) {
			pkg.setServiceDetails(xmlToServiceDetails(root.getFirstElement(SERVICES2)));
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

		root.addChild(new XmlNode(TITLE, pkg.getName()));
		root.addChild(new XmlNode(AUTHOR, pkg.getAuthor()));
		root.addChild(new XmlNode(NOTES, pkg.getNotes()));
		root.addChild(new XmlNode(DATE_CREATED, pkg.getCreated()));
		root.addChild(new XmlNode(DATE_MODIFIED, pkg.getModified()));
		root.getAttributes().put(VERSION, "" + pkg.getVersion());

		root.addChild(listToXml(EVENT_TRIGGERS, pkg.getEventURIs()));

		root.addChild(listToXml(SERVICES, pkg.getServiceDependendies()));

		return root.toString();
	}

	private XmlNode listToXml(String listName, List list) {
		XmlNode root = new XmlNode(listName);

		for (Iterator i = list.iterator(); i.hasNext();) {
			root.addChild(new XmlNode(ITEM_ELEMENT, i.next().toString()));
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

	/**
	 * Given an xml node of service details return a list of ServiceDetail
	 * objects
	 * 
	 * @param servicesDetailsNode
	 * @return
	 */
	private ServiceDetail[] xmlToServiceDetails(XmlNode servicesDetailsNode) {
		// extra check to make sure we don't blow up if the incorrect node is passed
		if (!SERVICES2.equals(servicesDetailsNode.getName())) {
			return new ServiceDetail[0];
		}
		// fill list with Service Details
		List<ServiceDetail> l = new ArrayList<ServiceDetail>();
		List<XmlNode> servicesNodes = servicesDetailsNode.getChildren();
		// make sure some children exist
		if (servicesNodes == null || servicesNodes.size() < 1) {
			return new ServiceDetail[0];
		}

		Iterator<XmlNode> itr = servicesNodes.iterator();
		ServiceDetail tmpDetail;
		while (itr.hasNext()) {
			tmpDetail = xmlToServiceDetail(itr.next());
			if (tmpDetail != null)
				l.add(tmpDetail);
		}

		return l.toArray(new ServiceDetail[l.size()]);
	}

	/**
	 * Creates a ServiceDetail object based on XML Manages the ServiceProperty
	 * object so if a property that we've seen before shows up, we add the value
	 * to the set of values for that same key.
	 * 
	 * @param detailNode
	 * @return
	 */
	private ServiceDetail xmlToServiceDetail(XmlNode detailNode) {
		// make sure we got sent the right type of xml node
		if (!SERVICE.equals(detailNode.getName()))
			return null;

		// make sure serviceName exists
		String serviceName = detailNode.getAttribute(NAME);
		if (serviceName == null)
			return null;

		// make sure the chillins exist
		List<XmlNode> propertiesNodes = detailNode.getChildren();
		if (propertiesNodes == null) {
			return new ServiceDetail(serviceName, new ArrayList<ServiceProperty>());
		}

		String name;
		Map<String, ServiceProperty> propBuilder = new HashMap<String, ServiceProperty>();
		for (XmlNode tmpNode : propertiesNodes) {
			name = tmpNode.getAttribute(NAME);
			if (name == null || name.length() < 1)
				continue;
			if (!propBuilder.containsKey(name))
				propBuilder.put(name, new ServiceProperty(name, new TreeSet<String>()));
			propBuilder.get(name).addValue(tmpNode.getAttribute(VALUE));
		}

		return new ServiceDetail(serviceName, new ArrayList<ServiceProperty>(propBuilder.values()));
	}

}
