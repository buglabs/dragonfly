package com.buglabs.dragonfly;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.jdt.core.IClasspathEntry;

import com.buglabs.dragonfly.jdt.BugClasspathContainer;
import com.buglabs.osgi.concierge.core.IPackageProvider;


public class BugPackageProvider implements IPackageProvider {

	public BugPackageProvider() {
		// TODO Auto-generated constructor stub
	}

	public List getExportedPackages(){
		BugClasspathContainer container = new BugClasspathContainer();
		IClasspathEntry[] entries = container.getClasspathEntries();
		List packages = new ArrayList();
		try{
		for (int i=0; i< entries.length;++i) {
			JarFile jarFile = new JarFile(entries[i].getPath().toFile());
			Manifest manifest = jarFile.getManifest();
			String value = manifest.getMainAttributes().getValue("Export-Package");
			if(value != null) {
				if(value.length() > 0) {
					packages.addAll(Arrays.asList(value.split(",")));
				}
			}
		}
		}
		catch(IOException e){
			return null;
		}
		return packages;
	}
}
