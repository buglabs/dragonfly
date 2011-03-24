package com.buglabs.dragonfly.ui.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.bugnet.BugnetWSHelper;
import com.buglabs.dragonfly.felix.ConciergeUtils;
import com.buglabs.dragonfly.felix.launch.ProjectUtils;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.util.BugWSHelper;

public class ImportBundleFromStreamAction extends Action {
	String programName;
	InputStream stream;
	String userName;
	private File jarFile;
	private boolean overwriteApp;

	public ImportBundleFromStreamAction() {
		overwriteApp = false;
	}

	public ImportBundleFromStreamAction(String programName) {
		this.programName = programName;
	}

	public ImportBundleFromStreamAction(String programName, InputStream stream) {
		this.programName = programName;
		this.stream = stream;
	}

	public void run() {
		final Job job = new Job("Downloading " + programName) {
			protected IStatus run(IProgressMonitor monitor) {
				IStatus okStatus = new Status(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, "Successful downloaded: " + programName, null);
				monitor.beginTask("Downloading " + programName, 100);
				try {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					programName = URLDecoder.decode(programName, "UTF-8");

					IProject proj = root.getProject(programName);
					overwriteApp = false;
					if (proj.exists()) {
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

							public void run() {
								IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
								Shell shell = new Shell();
								if (window != null) {
									shell = window.getShell();
								}
								overwriteApp = MessageDialog.openQuestion(shell, "Import Warning", "A project with the same name already exists in "
										+ "the current workspace.  Would you like to overwrite?");
								return;
							}
						});

					}

					if (!proj.exists() || overwriteApp) {
						if (stream == null)
							stream = BugnetWSHelper.getProgram(programName);
						jarFile = DragonflyActivator.getDefault().createFile(jarFileName(programName));
						monitor.worked(5);
						FileOutputStream fout = null;
						try {
							fout = new FileOutputStream(jarFile);
							BugWSHelper.pipe(stream, fout);
							monitor.worked(85);
							fout.close();
							stream.close();
						} catch (IOException e) {
							return createErrorStatus("Error occured while importing application", e);
						}
						ProjectUtils.importProjectIntoWorkspace(proj, jarFile);
						IJavaProject jproj = JavaCore.create(proj);

						jproj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.WARNING);
						jproj.setOption(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.WARNING);

						IClasspathEntry[] importCP = jproj.getRawClasspath();
						List cpl = new ArrayList();
						IClasspathEntry jre = JavaCore.newContainerEntry(JavaRuntime.newDefaultJREContainerPath());
						IClasspathEntry pde = JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins"));
						
						for (int i = 0; i < importCP.length; ++i) {
							String cpName = importCP[i].getPath().toString();
							
							if (cpName.equals("com.buglabs.osgi.concierge.jdt.ConciergeClasspathContainerInitializer") || 
								cpName.equals("com.buglabs.phoneme.personal.PhoneMEClasspathContainer") || 
								cpName.equals("com.buglabs.osgi.concierge.jdt.OSGiBundleClassPathContainerInitializer")) {							
								
								if (!cpl.contains(jre)) {
									cpl.add(jre);
								}
								
								if (!cpl.contains(pde)) {
									cpl.add(pde);
								}
							} else {
								cpl.add(importCP[i]);
							}							
						}

						jproj.setRawClasspath((IClasspathEntry[]) cpl.toArray(new IClasspathEntry[cpl.size()]), monitor);
						ConciergeUtils.addNatureToProject(proj, "org.eclipse.pde.PluginNature", monitor);

						monitor.worked(10);
						proj.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
					}
				} catch (IOException e) {
					return createErrorStatus("Unable to download " + programName + " from BUG.", e);
				} catch (InvocationTargetException e) {
					return createErrorStatus("Error occured while importing application", e);
				} catch (InterruptedException e) {
					return createErrorStatus("Error occured while importing application", e);
				} catch (Exception e) {
					return createErrorStatus("Error occured while importing application", e);
				} finally {
					if (jarFile != null) {
						if (jarFile.exists()) {
							jarFile.delete();
						}
					}
				}
				monitor.done();
				return okStatus;
			}

		};
		job.setPriority(Job.LONG);
		job.schedule();
	}

	private IStatus createErrorStatus(String message, Exception e) {
		IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, message, e);
		return errorStatus;
	}

	private String jarFileName(String programName) {
		String name = programName.replaceAll(" ", "") + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return name;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
