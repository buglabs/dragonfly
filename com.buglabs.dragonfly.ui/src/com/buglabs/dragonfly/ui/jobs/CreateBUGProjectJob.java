/*******************************************************************************
 * Copyright (c) 2011 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.jobs;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.buglabs.dragonfly.APIVersionManager;
import com.buglabs.dragonfly.BugApplicationNature;
import com.buglabs.dragonfly.felix.ConciergeUtils;
import com.buglabs.dragonfly.felix.launch.ProjectUtils;
import com.buglabs.dragonfly.generators.jet.Activator;
import com.buglabs.dragonfly.generators.jet.Application;
import com.buglabs.dragonfly.jdt.BugClasspathContainerInitializer;
import com.buglabs.dragonfly.model.BugProjectInfo;
import com.buglabs.dragonfly.model.ServicePropertyHelper;
import com.buglabs.dragonfly.ui.util.BugProjectUtil;
import com.buglabs.osgi.concierge.templates.GeneratorActivator;
import com.buglabs.util.osgi.BUGBundleConstants;

/**
 * Create a BUG project using properties gathered from NewBUGProjectWizard.
 *
 */
public class CreateBUGProjectJob extends WorkspaceModifyOperation {

	private static final String MANIFEST_DIRECTORY = "META-INF"; //$NON-NLS-1$
	private static final String MANIFEST_FILENAME = "MANIFEST.MF"; //$NON-NLS-1$
	private static final String PDE_REQUIREDPLUGINS_ID = "org.eclipse.pde.core.requiredPlugins"; //$NON-NLS-1$
	private static final String PDE_NATURE_ID = "org.eclipse.pde.PluginNature"; //$NON-NLS-1$
	private BugProjectInfo projInfo;
	private List<IClasspathEntry> classpathEntries;

	private IContainer srcContainer;
	private IContainer binContainer;

	/**
	 * @param projInfo BugProjectInfo
	 */
	public CreateBUGProjectJob(BugProjectInfo projInfo) {
		this.projInfo = projInfo;
		classpathEntries = new ArrayList<IClasspathEntry>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.buglabs.osgi.concierge.ui.jobs.CreateConciergeProject#execute(org
	 * .eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
		IProject proj = wsroot.getProject(projInfo.getProjectName());
		proj.create(monitor);
		proj.open(monitor);

		addNatures(proj, monitor);
		createBinFolder(proj, monitor);
		createSrcFolder(proj, monitor);
		setProjectClassPath(proj, monitor);
		createManifest(proj, monitor);

		if (projInfo.isGenerateActivator()) {
			generateActivator(monitor);
		}

		// Set the java version for BUG jvm compatibility
		IJavaProject jproj = JavaCore.create(getProject());

		setJava16Options(jproj);

		jproj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.WARNING);
		jproj.setOption(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.WARNING);

		if (getBugProjectInfo().getOSGiServices().size() > 0 || getBugProjectInfo().getModuleServices().size() > 0) {
			if (getBugProjectInfo().isGenerateSeparateApplicationClass()) {
				createApplication(monitor);
			}
		}
	}

	/**
	 * Create manifest file.
	 * @param proj IProject
	 * @param monitor IProgressMonitor
	 * @throws CoreException on project error
	 */
	private void createManifest(IProject proj, IProgressMonitor monitor) throws CoreException {
		IFolder metainf = proj.getFolder(MANIFEST_DIRECTORY);
		metainf.create(true, true, monitor);
		IFile manifest = metainf.getFile(MANIFEST_FILENAME);
		String contents = getManifestContents().toString();
		manifest.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
	}

	/**
	 * @param proj
	 * @param monitor
	 * @throws CoreException
	 */
	private void createSrcFolder(IProject proj, IProgressMonitor monitor) throws CoreException {
		srcContainer = proj;

		if (srcContainer.getType() == IResource.FOLDER) {
			((IFolder) srcContainer).create(true, true, monitor);
		}
		classpathEntries.add(JavaCore.newSourceEntry(srcContainer.getFullPath()));
	}

	/**
	 * @param proj
	 * @param monitor
	 * @throws JavaModelException
	 */
	private void setProjectClassPath(IProject proj, IProgressMonitor monitor) throws JavaModelException {
		addClasspathEntries();
		IJavaProject jproj = JavaCore.create(proj);
		jproj.setRawClasspath(getClassPathEntries(proj, monitor), null);
	}

	/**
	 * @param proj
	 * @param monitor
	 * @throws CoreException
	 */
	private void createBinFolder(IProject proj, IProgressMonitor monitor) throws CoreException {
		binContainer = proj;
		// bin.create(true, true, monitor);
		IJavaProject jproj = JavaCore.create(proj);
		jproj.setOutputLocation(binContainer.getFullPath(), monitor);
	}

	/**
	 * @param container
	 * @param childpath
	 * @throws CoreException
	 */
	protected void createDeepFile(IContainer container, Path childpath) throws CoreException {

		IContainer localContainer = container;
		for (int i = 0; i < childpath.segmentCount() - 1; ++i) {
			IFolder folder = localContainer.getFolder(new Path(childpath.segment(i)));
			folder.create(true, true, new NullProgressMonitor());
			localContainer = folder;
		}
	}

	/**
	 * @param project
	 * @param monitor
	 * @return
	 */
	private IClasspathEntry[] getClassPathEntries(IProject project, IProgressMonitor monitor) {
		return (IClasspathEntry[]) classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]);
	}

	/**
	 * @param file
	 * @param contents
	 * @param monitor
	 * @throws CoreException
	 */
	protected void writeContents(IFile file, String contents, IProgressMonitor monitor) throws CoreException {
		if (file.exists()) {
			file.delete(true, monitor);
		}

		file.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
	}

	/**
	 * Generate project Activator class.
	 * @param monitor IProgressMonitor
	 * @throws CoreException on project error
	 */
	protected void generateActivator(IProgressMonitor monitor) throws CoreException {
		String contents = getActivatorContents().toString();

		String fileHandle = projInfo.getActivator().replace('.', '/');
		/*
		 * char[] charArray = fileHandle.toCharArray(); charArray[0] =
		 * Character.toLowerCase(charArray[0]); fileHandle = new
		 * String(charArray);
		 */

		Path activatorpath = new Path(fileHandle + ".java"); //$NON-NLS-1$
		createDeepFile(srcContainer, activatorpath);
		IFile activator = srcContainer.getFile(activatorpath);
		writeContents(activator, contents, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.buglabs.osgi.concierge.ui.jobs.CreateConciergeProject#addClasspathEntries
	 * ()
	 */
	protected void addClasspathEntries() {
		classpathEntries.add(JavaCore.newContainerEntry(JavaRuntime.newDefaultJREContainerPath()));
		classpathEntries.add(JavaCore.newContainerEntry(new Path(PDE_REQUIREDPLUGINS_ID)));
		classpathEntries.add(JavaCore.newContainerEntry(new Path(BugClasspathContainerInitializer.ID)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.buglabs.osgi.concierge.ui.jobs.CreateConciergeProject#getManifestContents
	 * ()
	 */
	protected StringBuffer getManifestContents() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Manifest-Version: 1.0\n");
		buffer.append("Bundle-ManifestVersion: 2\n");
		buffer.append("Bundle-Name: " + projInfo.getProjectName() + "\n");

		if (hasContents(projInfo.getActivator()) && projInfo.isGenerateActivator()) {
			buffer.append("Bundle-Activator: " + projInfo.getActivator() + "\n");
		}

		if (hasContents(projInfo.getSymbolicName())) {
			buffer.append("Bundle-SymbolicName: " + ProjectUtils.formatName(projInfo.getSymbolicName()) + "\n");
		}

		if (hasContents(projInfo.getVersion())) {
			buffer.append("Bundle-Version: " + projInfo.getVersion() + "\n");
		}

		if (hasContents(projInfo.getVendor())) {
			buffer.append("Bundle-Vendor: " + projInfo.getVendor() + "\n");
		}

		if (hasContents(projInfo.getExecutionEnvironment())) {
			buffer.append("Bundle-RequiredExecutionEnvironment: " + projInfo.getExecutionEnvironment() + "\n");
		}
		
		if (hasContents(projInfo.getDescription())) {
			buffer.append("Bundle-Description: " + projInfo.getDescription() + "\n");
		}

		StringBuffer manifestContents = buffer;
		manifestContents.append(BUGBundleConstants.BUG_BUNDLE_TYPE_HEADER + ": " + BUGBundleConstants.BUG_BUNDLE_APPLICATION + "\n");

		// add API Version
		manifestContents.append(APIVersionManager.BUG_API_VERSION_MANIFEST_KEY + ": " + APIVersionManager.getSDKAPIVersion() + "\n");

		BugProjectInfo pinfo = getBugProjectInfo();
		List<String> services = getMergedServices(pinfo);

		List<String> packages = new ArrayList<String>();
		manifestContents.append("Import-Package:");
		manifestContents.append(" org.osgi.framework");
		boolean orgOsgiServiceLogAdded = false;
		
		if (services.size() > 0) {
			manifestContents.append(",\n");
			packages.add("org.osgi.util.tracker");
			packages.add("com.buglabs.util.osgi");
			Iterator serviceIter = services.iterator();
			while (serviceIter.hasNext()) {
				String serviceQualified = (String) serviceIter.next();
				int i = serviceQualified.lastIndexOf(".");
				String packagename = serviceQualified.substring(0, i);
				if (!packages.contains(packagename)) {
					packages.add(packagename);
					manifestContents.append(" " + packagename + ",\n");
				}
				if (packagename.equals("org.osgi.service.log")) {
					orgOsgiServiceLogAdded = true;
				}
			}
			manifestContents.append(" org.osgi.util.tracker,\n");
			if (!orgOsgiServiceLogAdded) {
				manifestContents.append(" org.osgi.service.log,\n");
			}
			manifestContents.append(" com.buglabs.util.osgi\n");
		} else if (pinfo.getGenerateLogMethod()) {
			manifestContents.append(",\n");
			manifestContents.append(" org.osgi.service.log,\n");
			manifestContents.append(" org.osgi.util.tracker,\n");
			manifestContents.append(" com.buglabs.util.osgi\n");
		}  else {
			manifestContents.append("\n");
		}

		return manifestContents;
	}
	
	private boolean hasContents(String s) {
		if (s == null || s.trim().length() == 0) {
			return false;
		}
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.buglabs.osgi.concierge.ui.jobs.CreateConciergeProject#addNatures(
	 * org.eclipse.core.resources.IProject,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void addNatures(IProject proj, IProgressMonitor monitor) throws CoreException {
		ConciergeUtils.addNatureToProject(proj, JavaCore.NATURE_ID, monitor);
		ConciergeUtils.addNatureToProject(proj, PDE_NATURE_ID, monitor);
		ConciergeUtils.addNatureToProject(proj, BugApplicationNature.ID, monitor);
	}

	/**
	 * Helper to return the current bug project from the workspace
	 * 
	 * @return
	 */
	protected IProject getProject() {
		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();

		return wsroot.getProject(getBugProjectInfo().getProjectName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.buglabs.osgi.concierge.ui.jobs.CreateConciergeProject#
	 * getActivatorContents()
	 */
	protected StringBuffer getActivatorContents() {
		if (getBugProjectInfo().getOSGiServices().size() > 0 | getBugProjectInfo().getModuleServices().size() > 0) {

			StringBuffer sb = new StringBuffer();
			String projectName = getBugProjectInfo().getProjectName();
			
			// last param is
			// usePropertyFilters(getBugProjectInfo().getServicePropertyHelperMap())
			// for v1.5 of SDK (which will be built against R1.4.3 or greater of
			// BUG)
			// Use false otherwise

			sb.append(new Activator().generate(
					BugProjectUtil.formatProjectNameAsClassName(projectName), 
					BugProjectUtil.formatProjectNameAsPackage(projectName),
					getServiceTrackerPackageName(projectName), 
					usePropertyFilters(getBugProjectInfo().getServicePropertyHelperMap()),
					getMergedServices(getBugProjectInfo()),
					getBugProjectInfo().isGenerateSeparateApplicationClass(),
					convertHelperMapToMapofStrings(getBugProjectInfo().getServicePropertyHelperMap()),
					getBugProjectInfo()));
			return sb;
		}

		GeneratorActivator gen = new GeneratorActivator();
		return new StringBuffer(gen.generate(projInfo));
	}

	/**
	 * @param pinfo
	 * @return A list of services selected in the osgi and module wizard pages.
	 */
	private List<String> getMergedServices(BugProjectInfo pinfo) {
		List<String> sl = new ArrayList<String>(pinfo.getOSGiServices());
		for (String svc: pinfo.getModuleServices())
			if (!sl.contains(svc))
				sl.add(svc);
		
		return sl;
	}

	/**
	 * returns the current BugProjectInfo object
	 * 
	 * @return
	 */
	private BugProjectInfo getBugProjectInfo() {
		return projInfo;
	}

	/**
	 * Set java project options for using Java 1.6 Called if user selected Java
	 * 1.6 on app creation
	 * 
	 * @param jproj
	 */
	private void setJava16Options(IJavaProject jproj) {
		jproj.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
		jproj.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
		jproj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
	}

	/**
	 * Creates the BUG Application Loop if a user selected
	 * "Generate Application Loop" in wizard
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	private void createApplication(IProgressMonitor monitor) throws CoreException {
		IProject proj = getProject();

		BugProjectInfo pinfo = getBugProjectInfo();
		String projectName = pinfo.getProjectName();
		String packageName = BugProjectUtil.formatProjectNameAsPackage(projectName);
		String path = getPackageNamePath(packageName);
		IFolder mainPackageFolder = proj.getFolder(path);
		IFile appFile = mainPackageFolder.getFile(BugProjectUtil.formatProjectNameAsClassName(projectName) + "Application.java");

		String contents = getApplicationContents();

		writeContents(appFile, contents, monitor);
	}

	/**
	 * Gets application loop code Helper called by createApplication
	 * 
	 * @return
	 */
	private String getApplicationContents() {
		BugProjectInfo pinfo = getBugProjectInfo();
		String projectName = pinfo.getProjectName();

		String appContents = new Application().generate(
				BugProjectUtil.formatProjectNameAsClassName(projectName) + "Application",
				BugProjectUtil.formatProjectNameAsPackage(projectName), 
				getMergedServices(pinfo),
				pinfo);

		return appContents;
	}

	/**
	 * Get formatted package name and add .servicetracker to end for creating
	 * servicetracker package
	 * 
	 * @param projectName
	 * @return
	 */
	private String getServiceTrackerPackageName(String projectName) {
		return BugProjectUtil.formatProjectNameAsPackage(projectName) + ".servicetracker";
	}

	/**
	 * convert package name to a file path
	 * 
	 * @param packageName
	 * @return
	 */
	private String getPackageNamePath(String packageName) {
		return packageName.replace('.', '/');
	}

	/**
	 * Converts a map of service property helpers (keyed by Service Property
	 * IDs) to a more generaic map of Service property string maps: Map<String,
	 * List<ServicePropertyHelper>> ==> Map<String, Map<String, String>>
	 * 
	 * This is for use by the code generators so they can build service property
	 * filters for the new BUG App.
	 * 
	 * @param helperMap
	 * @return
	 */
	private static Map<String, Map<String, String>> convertHelperMapToMapofStrings(Map<String, List<ServicePropertyHelper>> helperMap) {
		Map<String, Map<String, String>> output = new HashMap<String, Map<String, String>>();
		for (String key : helperMap.keySet()) {
			if (!output.containsKey(key))
				output.put(key, new HashMap<String, String>());
			List<ServicePropertyHelper> helperList = helperMap.get(key);
			for (ServicePropertyHelper helper : helperList) {
				output.get(key).put(helper.getKey(), helper.getSelectedValue());
			}
		}
		return output;
	}

	/**
	 * Helper function to look at our property map and determine if we're
	 * actually using property filters, which affects the way the code is
	 * generated
	 */
	private static boolean usePropertyFilters(Map<String, List<ServicePropertyHelper>> helperMap) {
		for (String key : helperMap.keySet()) {
			if (helperMap.get(key) != null && helperMap.get(key).size() > 0)
				return true;
		}
		return false;
	}

}