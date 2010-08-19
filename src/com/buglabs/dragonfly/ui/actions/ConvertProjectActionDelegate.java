/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

import com.buglabs.dragonfly.felix.ConciergeUtils;
import com.buglabs.dragonfly.ui.Activator;

public class ConvertProjectActionDelegate implements IActionDelegate {

	protected IProject project;

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(false);
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IAdaptable) {
				project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
			}

			//Enable this action for BUG projects that do not have the PDE nature.  This should be all pre-2.0 BUG project types.
			try {
				if (!project.hasNature("org.eclipse.pde.PluginNature")) {
					action.setEnabled(true);
					return;
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void run(IAction action) {
		Job job = new Job("Convert Project") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					IJavaProject jproj = JavaCore.create(project);

					jproj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.WARNING);
					jproj.setOption(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.WARNING);

					IClasspathEntry[] importCP = jproj.getRawClasspath();
					List cpl = new ArrayList();
					IClasspathEntry jre = JavaCore.newContainerEntry(JavaRuntime.newDefaultJREContainerPath());
					IClasspathEntry pde = JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins"));

					for (int i = 0; i < importCP.length; ++i) {
						String cpName = importCP[i].getPath().toString();

						if (cpName.equals("com.buglabs.osgi.concierge.jdt.ConciergeClasspathContainerInitializer")
								|| cpName.equals("com.buglabs.phoneme.personal.PhoneMEClasspathContainer")
								|| cpName.equals("com.buglabs.osgi.concierge.jdt.OSGiBundleClassPathContainerInitializer")
								|| cpName.equals("org.eclipse.jdt.launching.JRE_CONTAINER")) {

							if (!cpl.contains(jre)) {
								cpl.add(jre);
							}

							if (!cpl.contains(pde)) {
								cpl.add(pde);
							}
						} else {
							System.out.println(cpName);
							cpl.add(importCP[i]);
						}
					}

					jproj.setRawClasspath((IClasspathEntry[]) cpl.toArray(new IClasspathEntry[cpl.size()]), monitor);
					ConciergeUtils.addNatureToProject(project, "org.eclipse.pde.PluginNature", monitor);
					ConciergeUtils.removeNatureFromProject(project, "com.buglabs.osgi.concierge.natures.ConciergeProjectNature", monitor);

					project.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);

					return Status.OK_STATUS;
				} catch (Exception e) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to convert BUG project.", e);
				}
			}

		};
		
		job.schedule();
	}
}
