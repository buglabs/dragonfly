package com.buglabs.dragonfly.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.buglabs.util.osgi.BUGBundleConstants;
import com.buglabs.util.xml.XmlNode;
import com.buglabs.util.xml.XmlParser;
import com.buglabs.util.xml.XpathQuery;

/**
 * Utils for extracting informatino out of jars
 * 
 * @author Angel Roman
 * 
 */
public class JarUtils {
	public static String getPluginSymbolicName(JarFile jarFile) throws IOException {
		String name = null;

		name = jarFile.getManifest().getMainAttributes().getValue("Bundle-SymbolicName");
		return name;
	}

	public static String getBugBundleType(JarFile jarFile) throws IOException {
		String name = null;

		name = jarFile.getManifest().getMainAttributes().getValue(BUGBundleConstants.BUG_BUNDLE_TYPE_HEADER);

		return name;
	}

	public static String getProjectName(File jarFile) throws ZipException, IOException {
		String projectName = null;

		ZipFile zipFile = new ZipFile(jarFile);
		ZipEntry projectFile = zipFile.getEntry(".project");
		InputStream in = zipFile.getInputStream(projectFile);
		String line;
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuffer buffer = new StringBuffer();
		while ((line = br.readLine()) != null) {
			buffer.append(line);
		}

		XmlNode nameNode = XpathQuery.getNode("/projectDescription/name", XmlParser.parse(buffer.toString()));
		return nameNode.getValue();
	}
}
