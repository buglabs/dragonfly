/*******************************************************************************
 * Copyright (c) 2011 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.model.project.wizard;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.buglabs.util.xml.XmlNode;
import com.buglabs.util.xml.XmlParser;
import com.buglabs.util.xml.XpathQuery;

/**
 * Model elements for OSGi service registry data relating to BUG modules.
 * 
 * @author kgilmer
 *
 */
public class BUGModule {
	private final String name;
	private final String imageFilename;
	private final String description;
	private final List<BUGModuleService> services;

	public BUGModule(String name, String imageFilename, String description, List<BUGModuleService> services) {
		this.name = name;
		this.imageFilename = imageFilename;
		this.description = description;
		this.services = services;		
	}
	
	/**
	 * @return the module-name as defined in the module's bundle manifest header MODULE-ID.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return A relative path to an image that can be used to represent the module.
	 */
	public String getImageFilename() {
		return imageFilename;
	}
	
	/**
	 * @return A list of services that the module supports.
	 */
	public List<BUGModuleService> getServices() {
		return services;
	}
	
	/**
	 * @return A brief summary of the functionality provided by the module.
	 */
	public String getDescription() {
		return description;
	}
	
	public static List<BUGModule> load(Reader xmlReader) throws IOException {
		List<BUGModule> bml = new ArrayList<BUGModule>();
		XmlNode xnr = XmlParser.parse(xmlReader, false);
		
		for (XmlNode modules: XpathQuery.getNodes("/modules/module", xnr)) {
			List<BUGModuleService> bmsList = new ArrayList<BUGModuleService>();
			for (XmlNode services: XpathQuery.getNodes("/module/services/service", modules)) 
				bmsList.add(new BUGModuleService(services.getAttribute("shortName"), services.getAttribute("javaName"), services.getAttribute("description")));
			
			bml.add(new BUGModule(
					modules.getAttribute("name"), 
					modules.getChild("imageFile").getValue(), 
					modules.getChild("description").getValue(), 
					bmsList));
		}
		
		return bml;
	}
}
