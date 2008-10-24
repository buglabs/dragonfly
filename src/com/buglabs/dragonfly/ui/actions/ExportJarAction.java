/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.actions;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.action.Action;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.build.PluginExportJob;
import org.eclipse.ui.progress.IProgressConstants;

import com.buglabs.dragonfly.BugNature;
import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.wizards.bug.NewBugConnectionWizard;
import com.buglabs.dragonfly.util.BugWSHelper;

/**
 * Export a plugin project to a JAR with source. This class intentionally
 * imports internal packages from the PDE.
 * 
 * @author angel
 * 
 */
public class ExportJarAction extends Action {
	IProject project;

	String location;

	String bugUrl;

	IJobChangeListener jobListener;

	/**
	 * @param project
	 */
	public ExportJarAction(IProject project) {
		this.project = project;
		location = Activator.getDefault().getStateLocation().toFile().getAbsolutePath();
	}

	public ExportJarAction(IProject project, String bugUrl, IJobChangeListener jobChangeListener) {
		this.project = project;
		location = Activator.getDefault().getJarLocation();
		this.bugUrl = bugUrl;

		jobListener = jobChangeListener;
	}

	private String findBug(List list) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IProject[] projects = workspace.getRoot().getProjects();

		for (int i = 0; i < projects.length; ++i) {
			if (projects[i].hasNature(BugNature.ID)) {
				String url = projects[i].getPersistentProperty(NewBugConnectionWizard.URL_PERSISTENT_PROPERTY);
				return url;
			}
		}

		return null;
	}

	public void run() {
		if (project != null && location != null) {
			IPluginModelBase model = PDECore.getDefault().getModelManager().findModel(project);
			FeatureExportInfo finfo = new FeatureExportInfo();
			finfo.toDirectory = true;
			finfo.useJarFormat = true;
			finfo.exportSource = true;
			finfo.destinationDirectory = location;
			finfo.items = new Object[] { model };
			BugBundleExportJob job = new BugBundleExportJob(finfo);

			if (jobListener != null) {
				job.addJobChangeListener(jobListener);
			}
			job.setUser(true);
			job.schedule();
			job.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_PLUGIN_OBJ);

		}
	}

	private class BugBundleExportJob extends PluginExportJob {

		public BugBundleExportJob(FeatureExportInfo info) {
			super(info);
		}

		protected IStatus run(IProgressMonitor monitor) {
			IStatus rval;

			try {
				bugUrl = findBug((List) DragonflyActivator.getDefault().getModel());

				if (bugUrl == null) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarAction.0"), null); //$NON-NLS-1$
				}

				String buildDir = location + File.separator + "plugins"; //$NON-NLS-1$
				// Remove previous files from directory.
				clearDirectory(buildDir, project.getName());

				rval = super.run(monitor);

				if (rval.getSeverity() == IStatus.OK) {
					File jarfile = findFile(buildDir, project.getName());
					if (project != null && location != null) {
						IPluginModelBase model = PDECore.getDefault().getModelManager().findModel(project);
						FeatureExportInfo finfo = new FeatureExportInfo();
						finfo.toDirectory = true;
						finfo.useJarFormat = true;
						finfo.exportSource = true;
						finfo.destinationDirectory = location;
						finfo.items = new Object[] { model };
						BugBundleExportJob job = new BugBundleExportJob(finfo);

						if (jobListener != null) {
							job.addJobChangeListener(jobListener);
						}
						job.setUser(true);
						job.schedule();
						job.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_PLUGIN_OBJ);

					}

					String response = BugWSHelper
							.upsertBundle(jarfile, new URL(bugUrl + "/program/" + project.getName().replace(' ', '+'))); //$NON-NLS-1$
					// TODO Check response and determine if error, probably
					// should raise exception in upsertBundle() method and catch
					// here.
				}
			} catch (CoreException e) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarAction.0"), e); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarAction.0"), e); //$NON-NLS-1$
			} catch (IOException e) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.getString("ExportJarAction.0"), e); //$NON-NLS-1$
			}

			return rval;
		}

		private void clearDirectory(String buildDir, final String name) {
			File dir = new File(buildDir);

			if (dir.exists() && dir.isDirectory()) {
				File[] jars = dir.listFiles(new FilenameFilter() {

					public boolean accept(File arg0, String arg1) {
						if (arg1.endsWith(".jar")) { //$NON-NLS-1$
							return true;
						}
						return false;
					}

				});

				if (jars != null) {
					for (int i = 0; i < jars.length; ++i) {
						jars[i].delete();
					}
				}
			}
		}

		private File findFile(String directory, final String prefix) {
			File dir = new File(directory);
			File[] files;
			if (dir.exists() && dir.isDirectory()) {
				files = dir.listFiles(new FilenameFilter() {
					public boolean accept(File arg0, String arg1) {

						return arg1.toUpperCase().startsWith(prefix.toUpperCase());
					}

				});

				if (files != null && files.length == 1) {
					return files[0];
				}
			}
			return null;
		}
	}
}
