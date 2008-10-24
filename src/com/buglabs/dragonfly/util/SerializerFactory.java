/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

/**
 * Factory for getting a package/program serializer.
 * 
 * @author ken
 * 
 */
public class SerializerFactory {
	private static PlainTextSerializer pts;

	private static SimpleXMLSerializer xs;

	/**
	 * Return a serializer.
	 * 
	 * @param formatType -
	 *            "text" will return a plaintext serializer, otherwise XML.
	 * @return
	 */
	public static ISerializer getSerializer(String formatType) {
		if (formatType.equalsIgnoreCase("text")) {
			if (pts == null) {
				pts = new PlainTextSerializer();
			}

			return pts;
		}

		if (xs == null) {
			xs = new SimpleXMLSerializer();
		}

		return xs;
	}
}
