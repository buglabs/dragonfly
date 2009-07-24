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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

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
import com.buglabs.phoneme.personal.PhoneMEClasspathContainer;
import com.buglabs.util.BugBundleConstants;

public class CreateBugProjectJob extends CreateConciergeProject {

	public CreateBugProjectJob(BugProjectInfo projInfo) {
		super(projInfo);
	}

	protected void addClasspathEntries() {
		super.addClasspathEntries();

		getClasspathEntries().remove(JavaCore.newContainerEntry(JavaRuntime.newDefaultJREContainerPath()));
		getClasspathEntries().add(JavaCore.newContainerEntry(new Path(BugClasspathContainerInitializer.ID)));
		getClasspathEntries().add(JavaCore.newContainerEntry(new Path(PhoneMEClasspathContainer.ID)));
	}

	private BugProjectInfo getBugProjectInfo() {
		return (BugProjectInfo) getProjectInfo();
	}

	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		super.execute(monitor);

		// Set the java version for BUG jvm compatibility
		IJavaProject jproj = JavaCore.create(getProject());

		jproj.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		jproj.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
		jproj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
		jproj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.WARNING);
		jproj.setOption(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.WARNING);

		if (getBugProjectInfo().getServices().size() > 0) {
			createServiceTracker(monitor);
			if (getBugProjectInfo().isShouldGenerateApplicationLoop()) {
				createApplication(monitor);
			}
		}
	}

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

	private String getApplicationContents() {
		BugProjectInfo pinfo = getBugProjectInfo();
		String projectName = pinfo.getProjectName();

		String appContents = new Application().generate(BugProjectUtil.formatProjectNameAsClassName(projectName) + "Application",
				BugProjectUtil.formatProjectNameAsPackage(projectName), pinfo.getServices());

		return appContents;
	}

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

	protected StringBuffer getServiceTrackerContents() {
		StringBuffer sb = new StringBuffer();
		BugProjectInfo pinfo = getBugProjectInfo();
		String projectName = pinfo.getProjectName();
		
		sb.append(new ServiceTrackerCustomizer().generate(
					pinfo.getServices(),
					convertHelperMapToMapofStrings(pinfo.getServicePropertyHelperMap()),
					BugProjectUtil.formatProjectNameAsClassName(projectName),
					getServiceTrackerPackageName(projectName), 
					BugProjectUtil.formatProjectNameAsPackage(projectName), 
					pinfo.isShouldGenerateApplicationLoop()));

		return sb;
	}
	
	protected void createActivator(IProgressMonitor monitor) throws CoreException {
		String packageName = BugProjectUtil.formatProjectNameAsPackage(getBugProjectInfo().getProjectName());
		String path = getPackageNamePath(packageName);
		IFolder mainPackageFolder = getProject().getFolder(path);

		IFile activatorFile = mainPackageFolder.getFile("Activator.java");
		String activatorContents = getActivatorContents().toString();
		writeContents(activatorFile, activatorContents, monitor);
	}

	protected IProject getProject() {
		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();

		return wsroot.getProject(getBugProjectInfo().getProjectName());
	}

	protected StringBuffer getActivatorContents() {
		if (getBugProjectInfo().getServices().size() > 0) {

			StringBuffer sb = new StringBuffer();
			String projectName = getBugProjectInfo().getProjectName();
			sb.append(new Activator().generate(BugProjectUtil.formatProjectNameAsClassName(projectName), BugProjectUtil
					.formatProjectNameAsPackage(projectName), getServiceTrackerPackageName(projectName)));
			return sb;
		}

		return super.getActivatorContents();
	}

	private String getServiceTrackerPackageName(String projectName) {
		return BugProjectUtil.formatProjectNameAsPackage(projectName) + ".servicetracker";
	}

	private String getPackageNamePath(String packageName) {
		return packageName.replace('.', '/');
	}

	protected StringBuffer getManifestContents() {
		StringBuffer manifestContents = super.getManifestContents();
		manifestContents.append(BugBundleConstants.BUG_BUNDLE_TYPE_HEADER + ": " + BugBundleConstants.BUG_BUNDLE_APPLICATION + "\n");
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

	protected void addNatures(IProject proj, IProgressMonitor monitor) throws CoreException {
		super.addNatures(proj, monitor);
		ConciergeUtils.addNatureToProject(proj, BugApplicationNature.ID, monitor);
	}

	private static String getBundleSymbolicName(String projName) {
		return projName.toLowerCase().replaceAll(" ", "_");
	}

	private static Map<String, Map<String, String>> convertHelperMapToMapofStrings(
			Map<String, List<ServicePropertyHelper>> helperMap) {
		Map<String, Map<String, String>> output = new HashMap<String, Map<String, String>>();
		for (String key : helperMap.keySet()) {
			if (!output.containsKey(key))
				output.put(key, new HashMap<String, String>());
			List<ServicePropertyHelper> helperList = helperMap.get(key);
			for (ServicePropertyHelper helper : helperList) {
				output.get(key).put(
						helper.getKey(), helper.getSelectedValue());
			}
		}
		return output;
	}
	
	public static String getClassName(String projName) {
		return BugProjectUtil.formatProjectNameAsPackage(projName) + ".Activator";
	}
}