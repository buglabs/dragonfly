/* Copyright (c) 2007 Bug Labs, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of Bug Labs nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.buglabs.dragonfly.felix.launch;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.jdt.ui.jarpackager.JarWriter3;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;


/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 * 
 */
public class ProjectUtils {
	public static String formatName(String projectName) {
		projectName = projectName.trim().replaceAll(" ", "_");
		return projectName;
	}

	public static IFile getManifestFile(IProject project) {
		IFile file = project.getFile("META-INF/MANIFEST.MF");

		return file;
	}

	public static File exporToJar(File location, IProject project, boolean deleteExisting, boolean rootClasses) throws CoreException {
		List jarContents = getJarContents(project);
		IFile ManifestFile = project.getFile("META-INF/MANIFEST.MF");

		String projectJarName = getProjectJarName(project);

		if (projectJarName == null) {
			return null;
		}
		
		IJavaProject jproject = JavaCore.create(project);
		if (jproject != null && rootClasses) {
			jproject.setOutputLocation(project.getFullPath(), new NullProgressMonitor());
		}

		File jar = new File(location, projectJarName);
		
		if (jar.exists() && deleteExisting) {
			if (!jar.delete()) {
				//
				throw new CoreException(Status.CANCEL_STATUS);	
			}
		}
		
		ManifestFile.refreshLocal(1, null);
		JarPackageData jpd = new JarPackageData();
		jpd.setElements(jarContents.toArray());
		jpd.setOverwrite(true);
		jpd.setGenerateManifest(false);
		jpd.setManifestLocation(ManifestFile.getFullPath());
		jpd.setJarLocation(new Path(jar.getAbsolutePath()));
		jpd.setUseSourceFolderHierarchy(true);
		jpd.setExportJavaFiles(true);
		jpd.setExportClassFiles(true);
		jpd.setExportErrors(true);
		jpd.setExportWarnings(true);

		if (!jpd.isValid())
			return null;
		
		JarWriter3 jw = new JarWriter3(jpd, null);
		
			
		Iterator iter = jarContents.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof IFile) {
				IFile f = (IFile) obj;
				if (!f.equals(ManifestFile)) {		
					jw.write(f, f.getProjectRelativePath());					
				}
			}
		}

		jw.close();

		return jar;
	}

	public static String getProjectJarName(IProject project) throws CoreException {
		return ProjectUtils.formatName(project.getName()) + ".jar";
	}

	public static URL getProjectJarURL(File destination, IProject project) throws IOException, URISyntaxException, CoreException {
		String jarsLocation = destination.getAbsolutePath();
		File file = new File(jarsLocation + "/" + getProjectJarName(project));
		return file.toURI().toURL();
	}

	public static List getJarContents(IProject proj) throws CoreException {
		final Vector objects = new Vector();

		proj.accept(new IResourceVisitor() {

			public boolean visit(IResource resource) throws CoreException {
				if (resource.getName().equals(".settings") && resource.getType() == IResource.FILE && resource.getParent() != null
						&& resource.getParent().getType() == IResource.PROJECT) {
					return false;
				}

				if (resource.getName().equals("bin") && resource.getType() == IResource.FOLDER && resource.getParent() != null
						&& resource.getParent().getType() == IResource.PROJECT) {
					return true;
				}

				objects.add(resource);
				return true;
			}
		});

		return objects;
	}

	/**
	 * Builds a list of Concierge Projects
	 * 
	 * @return A list of projects in the workspace containing the Concierge
	 *         Project Nature.
	 * @throws CoreException
	 */
	public static List getWSCGProjects() throws CoreException {

		final List projects = new Vector();

		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
		wsroot.accept(new IResourceVisitor() {

			public boolean visit(IResource resource) throws CoreException {

				if (resource.getType() == IResource.ROOT) {
					return true;
				} else if (resource.getType() == IResource.PROJECT) {
					IProject project = (IProject) resource;
					if (project.isOpen() && project.hasNature("com.buglabs.osgi.concierge.natures.ConciergeProjectNature")) {
						projects.add(project);
					} else if (project.isOpen() && project.hasNature("org.eclipse.pde.PluginNature")) {
						projects.add(project);
					}
				}

				return false;
			}
		});

		return projects;
	}

	public static void importProjectIntoWorkspace(IProject proj, File jarFile) throws ZipException, IOException, InvocationTargetException, InterruptedException {
		ZipFile zipFile = new ZipFile(jarFile);
		ZipFileStructureProvider prov = new ZipFileStructureProvider(zipFile);

		ImportOperation op = new ImportOperation(proj.getFullPath(), prov.getRoot(), prov, new IOverwriteQuery() {

			public String queryOverwrite(String pathString) {

				return IOverwriteQuery.ALL;
			}
		});

		op.run(new NullProgressMonitor());
	}

	public static String formatNameToPackage(String projectName) {
		return formatName(projectName).toLowerCase();
	}

	/**
	 * @param proj
	 *            project that needs to be checked for errors
	 * @return <code>true</code> if project has sever errors, <code>false</code>
	 *         otherwise
	 * @throws CoreException
	 */
	public static boolean existsProblems(IProject proj) throws CoreException {
		if (proj != null) {
			IMarker[] markers = proj.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);

			if (markers != null) {
				for (int i = 0; i < markers.length; i++) {
					if (isSevere(markers[i])) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean isSevere(IMarker problemMarker) throws CoreException {
		Integer severity = (Integer) problemMarker.getAttribute(IMarker.SEVERITY);
		if (severity != null) {
			return severity.intValue() >= IMarker.SEVERITY_ERROR;
		}

		return false;
	}

	/**
	 * Configures given project with a builder. Will check if project already
	 * has builder associated with it, if not will add it.
	 * 
	 * @param project
	 * @param builderId
	 * @throws CoreException
	 */
	public static void configureBuilder(IProject project, String builderId) throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		boolean found = false;

		// check if builder exists
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderId)) {
				found = true;
				break;
			}
		}
		if (!found) {
			// add builder to project
			ICommand command = desc.newCommand();
			command.setBuilderName(builderId);
			ICommand[] newCommands = new ICommand[commands.length + 1];

			// Add it before other builders.
			System.arraycopy(commands, 0, newCommands, 1, commands.length);
			newCommands[0] = command;
			desc.setBuildSpec(newCommands);
			project.setDescription(desc, null);
		}
	}
	

	/**
	 * Generate Package string from IFolder
	 * @param folder
	 * @return
	 */
	public static String getPackageFromIFolder(IFolder folder) {
		return folder.getProjectRelativePath().toString().replace(File.separatorChar, '.');
	}
}
