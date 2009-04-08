package com.buglabs.dragonfly.jdt;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

import com.buglabs.dragonfly.DragonflyActivator;

/**
 * Looks in the Bug Kernel Location (BugKernel plug-in) for jar files and adds
 * them to the classpath.
 * 
 * @author Angel Roman
 * 
 */
public class BugClasspathContainer implements IClasspathContainer {

	public static final String ID = "com.buglabs.dragonfly.jdt.BugClasspathContainer";

	public IClasspathEntry[] getClasspathEntries() {
		List libraries = DragonflyActivator.getDefault().getBugKernelJars();

		if (!libraries.isEmpty()) {
			IClasspathEntry[] ceLibs = new IClasspathEntry[libraries.size()];

			for (int i = 0; i < ceLibs.length; ++i) {
				String path = ((File) libraries.get(i)).getAbsolutePath();
				ceLibs[i] = JavaCore.newLibraryEntry(new Path(path), null, null, false);
			}

			return ceLibs;
		}

		return new IClasspathEntry[0];
	}

	public String getDescription() {
		return "BUG Libraries";
	}

	public int getKind() {
		// in eclipse 3.4, returnint CPE_CONTAINER kept code complete
		// from working, so now I return K_APPLICATION
		//return IClasspathEntry.CPE_CONTAINER;
		return K_APPLICATION;
	}

	public IPath getPath() {
		return new Path(ID);
	}
}
