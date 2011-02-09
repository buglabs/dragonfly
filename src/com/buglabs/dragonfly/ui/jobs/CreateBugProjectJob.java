package com.buglabs.dragonfly.ui.jobs;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
import com.buglabs.util.BugBundleConstants;

public class CreateBugProjectJob extends WorkspaceModifyOperation {

	private BugProjectInfo projInfo;
	private List classpathEntries;

	private IContainer srcContainer;
	private IContainer binContainer;

	public CreateBugProjectJob(BugProjectInfo projInfo) {
		this.projInfo = projInfo;
		classpathEntries = new ArrayList();
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

		if (getBugProjectInfo().getServices().size() > 0) {
			if (getBugProjectInfo().isGenerateSeparateApplicationClass()) {
				createApplication(monitor);
			}
		}
	}

	private void createManifest(IProject proj, IProgressMonitor monitor) throws CoreException {
		IFolder metainf = proj.getFolder("META-INF");
		metainf.create(true, true, monitor);
		IFile manifest = metainf.getFile("MANIFEST.MF");
		String contents = getManifestContents().toString();
		manifest.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
	}

	private void createSrcFolder(IProject proj, IProgressMonitor monitor) throws CoreException {
		srcContainer = proj;// .getFolder("/");// proj.getFolder("src");

		if (srcContainer.getType() == IResource.FOLDER) {
			((IFolder) srcContainer).create(true, true, monitor);
		}
		classpathEntries.add(JavaCore.newSourceEntry(srcContainer.getFullPath()));
	}

	private void setProjectClassPath(IProject proj, IProgressMonitor monitor) throws JavaModelException {
		addClasspathEntries();
		IJavaProject jproj = JavaCore.create(proj);
		jproj.setRawClasspath(getClassPathEntries(proj, monitor), null);
	}

	private void createBinFolder(IProject proj, IProgressMonitor monitor) throws CoreException {
		binContainer = proj;
		// bin.create(true, true, monitor);
		IJavaProject jproj = JavaCore.create(proj);
		jproj.setOutputLocation(binContainer.getFullPath(), monitor);
	}

	protected void createDeepFile(IContainer container, Path childpath) throws CoreException {

		IContainer localContainer = container;
		for (int i = 0; i < childpath.segmentCount() - 1; ++i) {
			IFolder folder = localContainer.getFolder(new Path(childpath.segment(i)));
			folder.create(true, true, new NullProgressMonitor());
			localContainer = folder;
		}
	}

	private IClasspathEntry[] getClassPathEntries(IProject project, IProgressMonitor monitor) {
		return (IClasspathEntry[]) classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]);
	}

	protected void writeContents(IFile file, String contents, IProgressMonitor monitor) throws CoreException {
		if (file.exists()) {
			file.delete(true, monitor);
		}

		file.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
	}

	protected void generateActivator(IProgressMonitor monitor) throws CoreException {
		String contents = getActivatorContents().toString();

		String fileHandle = projInfo.getActivator().replace('.', '/');
		/*
		 * char[] charArray = fileHandle.toCharArray(); charArray[0] =
		 * Character.toLowerCase(charArray[0]); fileHandle = new
		 * String(charArray);
		 */

		Path activatorpath = new Path(fileHandle + ".java");
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
		classpathEntries.add(JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins")));
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
		manifestContents.append(BugBundleConstants.BUG_BUNDLE_TYPE_HEADER + ": " + BugBundleConstants.BUG_BUNDLE_APPLICATION + "\n");

		// add API Version
		manifestContents.append(APIVersionManager.BUG_API_VERSION_MANIFEST_KEY + ": " + APIVersionManager.getSDKAPIVersion() + "\n");

		BugProjectInfo pinfo = getBugProjectInfo();
		List services = pinfo.getServices();

		Vector packages = new Vector();
		manifestContents.append("Import-Package:");
		manifestContents.append(" org.osgi.framework");
		
		if (services.size() > 0) {
			Iterator serviceIter = services.iterator();
			while (serviceIter.hasNext()) {
				String serviceQualified = (String) serviceIter.next();
				int i = serviceQualified.lastIndexOf(".");
				String packagename = serviceQualified.substring(0, i);
				if (!packages.contains(packagename)) {
					packages.add(packagename);
					manifestContents.append(" " + packagename + ",\n");
				}
			}
			manifestContents.append(",\n");
			manifestContents.append(" org.osgi.util.tracker,\n");
			manifestContents.append(" com.buglabs.application,\n");
			manifestContents.append(" com.buglabs.util\n");
		} else if (pinfo.getGenerateLogMethod()) {
			manifestContents.append(",\n");
			manifestContents.append(" org.osgi.util.tracker,\n");
			manifestContents.append(" com.buglabs.util\n");
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
		ConciergeUtils.addNatureToProject(proj, "org.eclipse.pde.PluginNature", monitor);
		ConciergeUtils.addNatureToProject(proj, BugApplicationNature.ID, monitor);
	}

	/**
	 * Called from execute to generate the service tracker code
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	/*protected void createServiceTracker(IProgressMonitor monitor) throws CoreException {
		BugProjectInfo pinfo = getBugProjectInfo();
		String projectName = pinfo.getProjectName();

		String trackerPath = getPackageNamePath(getServiceTrackerPackageName(projectName));
		IFolder folder = getProject().getFolder(trackerPath);
		String fileName = BugProjectUtil.formatProjectNameAsClassName(projectName) + "ServiceTracker.java";
		IFile serviceTracker = folder.getFile(fileName);

		if (!folder.exists()) {
			try {
				folder.create(true, true, new NullProgressMonitor());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String contents = getServiceTrackerContents().toString();
		writeContents(serviceTracker, contents, monitor);
	}*/

	/**
	 * Generate the service tracker code
	 * 
	 * @return
	 */
	/*protected StringBuffer getServiceTrackerContents() {
		StringBuffer sb = new StringBuffer();
		BugProjectInfo pinfo = getBugProjectInfo();
		String projectName = pinfo.getProjectName();

		// last param is
		// usePropertyFilters(getBugProjectInfo().getServicePropertyHelperMap())
		// for v1.5 of SDK (which will be built against R1.4.3 or greater of
		// BUG)
		// Just use false for v1.4 and previous
		sb.append(new ServiceTrackerCustomizer().generate(pinfo.getServices(), convertHelperMapToMapofStrings(pinfo.getServicePropertyHelperMap()),
				BugProjectUtil.formatProjectNameAsClassName(projectName), getServiceTrackerPackageName(projectName), BugProjectUtil.formatProjectNameAsPackage(projectName),
				pinfo.isShouldGenerateApplicationLoop(), usePropertyFilters(getBugProjectInfo().getServicePropertyHelperMap())));

		return sb;
	}*/

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
		if (getBugProjectInfo().getServices().size() > 0) {

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
					getBugProjectInfo().getServices(),
					getBugProjectInfo().isGenerateSeparateApplicationClass(),
					convertHelperMapToMapofStrings(getBugProjectInfo().getServicePropertyHelperMap()),
					getBugProjectInfo()));
			return sb;
		}

		GeneratorActivator gen = new GeneratorActivator();
		return new StringBuffer(gen.generate(projInfo));
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
				pinfo.getServices(),
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