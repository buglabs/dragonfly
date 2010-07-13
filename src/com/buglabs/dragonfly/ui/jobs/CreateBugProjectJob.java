package com.buglabs.dragonfly.ui.jobs;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

import com.buglabs.dragonfly.APIVersionManager;
import com.buglabs.dragonfly.BugApplicationNature;
import com.buglabs.dragonfly.generators.jet.Activator;
import com.buglabs.dragonfly.generators.jet.Application;
import com.buglabs.dragonfly.generators.jet.ServiceTrackerCustomizer;
import com.buglabs.dragonfly.jdt.BugClasspathContainerInitializer;
import com.buglabs.dragonfly.ui.info.BugProjectInfo;
import com.buglabs.dragonfly.ui.info.ServicePropertyHelper;
import com.buglabs.dragonfly.ui.util.BugProjectUtil;
import com.buglabs.osgi.concierge.core.utils.ConciergeUtils;
import com.buglabs.osgi.concierge.ui.jobs.CreateConciergeProject;
import com.buglabs.util.BugBundleConstants;

public class CreateBugProjectJob extends CreateConciergeProject {

	public CreateBugProjectJob(BugProjectInfo projInfo) {
		super(projInfo);
	}

	/*
	 * TODO this used? public static String getClassName(String projName) {
	 * return BugProjectUtil.formatProjectNameAsPackage(projName) +
	 * ".Activator"; }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.buglabs.osgi.concierge.ui.jobs.CreateConciergeProject#execute(org
	 * .eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		super.execute(monitor);

		// Set the java version for BUG jvm compatibility
		IJavaProject jproj = JavaCore.create(getProject());

		if (getBugProjectInfo().getExecutionEnvironment().indexOf(JavaCore.VERSION_1_6) != -1)
			setJava16Options(jproj);
		else
			setPhoneMEOptions(jproj);

		jproj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.WARNING);
		jproj.setOption(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.WARNING);

		if (getBugProjectInfo().getServices().size() > 0) {
			createServiceTracker(monitor);
			if (getBugProjectInfo().isShouldGenerateApplicationLoop()) {
				createApplication(monitor);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.buglabs.osgi.concierge.ui.jobs.CreateConciergeProject#addClasspathEntries
	 * ()
	 */
	protected void addClasspathEntries() {
		super.addClasspathEntries();
		getClasspathEntries().remove(JavaCore.newContainerEntry(JavaRuntime.newDefaultJREContainerPath()));
		getClasspathEntries().add(JavaCore.newContainerEntry(new Path(BugClasspathContainerInitializer.ID)));
		getClasspathEntries().add(getJavaRuntimeEntry());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.buglabs.osgi.concierge.ui.jobs.CreateConciergeProject#getManifestContents
	 * ()
	 */
	protected StringBuffer getManifestContents() {
		StringBuffer manifestContents = super.getManifestContents();
		manifestContents.append(BugBundleConstants.BUG_BUNDLE_TYPE_HEADER + ": " + BugBundleConstants.BUG_BUNDLE_APPLICATION + "\n");

		// add API Version
		manifestContents.append(APIVersionManager.BUG_API_VERSION_MANIFEST_KEY + ": " + APIVersionManager.getSDKAPIVersion() + "\n");

		BugProjectInfo pinfo = getBugProjectInfo();
		List services = pinfo.getServices();

		Vector packages = new Vector();

		if (services.size() > 0) {

			manifestContents.append("Import-Package:");
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

			manifestContents.append(" org.osgi.framework,\n");
			manifestContents.append(" org.osgi.util.tracker,\n");
			manifestContents.append(" com.buglabs.application,\n");
			manifestContents.append(" com.buglabs.util\n");
		}

		return manifestContents;
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
		super.addNatures(proj, monitor);
		ConciergeUtils.addNatureToProject(proj, BugApplicationNature.ID, monitor);
	}

	/**
	 * Called from execute to generate the service tracker code
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	protected void createServiceTracker(IProgressMonitor monitor) throws CoreException {
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
	}

	/**
	 * Generate the service tracker code
	 * 
	 * @return
	 */
	protected StringBuffer getServiceTrackerContents() {
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
	}

	/**
	 * TODO Is this called from anywhere -- think this functionality is in
	 * CreateConciergeProject.generateActivator and not being used here.
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	/*
	 * protected void createActivator(IProgressMonitor monitor) throws
	 * CoreException { String packageName =
	 * BugProjectUtil.formatProjectNameAsPackage
	 * (getBugProjectInfo().getProjectName()); String path =
	 * getPackageNamePath(packageName); IFolder mainPackageFolder =
	 * getProject().getFolder(path);
	 * 
	 * IFile activatorFile = mainPackageFolder.getFile("Activator.java"); String
	 * activatorContents = getActivatorContents().toString();
	 * writeContents(activatorFile, activatorContents, monitor); }
	 */

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
			sb.append(new Activator().generate(BugProjectUtil.formatProjectNameAsClassName(projectName), BugProjectUtil.formatProjectNameAsPackage(projectName),
					getServiceTrackerPackageName(projectName), usePropertyFilters(getBugProjectInfo().getServicePropertyHelperMap())));
			return sb;
		}

		return super.getActivatorContents();
	}

	/**
	 * Get the java runtime classpath entry for this project based on the user's
	 * selection. The default is PhoneME. Otherwise, it's Java 1.6
	 * 
	 * @return
	 */
	private IClasspathEntry getJavaRuntimeEntry() {
		// if Java 1.6 Execution Environment was selected, get it via the
		// execution environments

		IExecutionEnvironment[] executionEnvs = JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments();
		for (int i = 0; i < executionEnvs.length; i++) {
			if (executionEnvs[i].getId().indexOf(JavaCore.VERSION_1_6) != -1) {
				return JavaCore.newContainerEntry(JavaRuntime.newJREContainerPath(executionEnvs[i]));
			}
		}

		throw new RuntimeException("Unable to find classpath entry.");
	}

	/**
	 * returns the current BugProjectInfo object
	 * 
	 * @return
	 */
	private BugProjectInfo getBugProjectInfo() {
		return (BugProjectInfo) getProjectInfo();
	}

	/**
	 * Set java project options for using PhoneME Called if the user selected
	 * PhoneME on app creation
	 * 
	 * @param jproj
	 */
	private void setPhoneMEOptions(IJavaProject jproj) {
		jproj.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		jproj.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
		jproj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
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

		String appContents = new Application().generate(BugProjectUtil.formatProjectNameAsClassName(projectName) + "Application",
				BugProjectUtil.formatProjectNameAsPackage(projectName), pinfo.getServices());

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