package com.buglabs.dragonfly.jdt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class BugClasspathContainerInitializer extends ClasspathContainerInitializer {

	public static final String ID = "com.buglabs.dragonfly.jdt.BugClasspathContainerInitializer";

	public BugClasspathContainerInitializer() {

	}

	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		BugClasspathContainer bugCC = new BugClasspathContainer();
		JavaCore.setClasspathContainer(new Path(ID), new IJavaProject[] { project }, new IClasspathContainer[] { bugCC },
				new NullProgressMonitor());
	}

	public String getDescription(IPath containerPath, IJavaProject project) {
		return super.getDescription(containerPath, project);
	}
}
