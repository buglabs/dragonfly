package com.buglabs.dragonfly.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.BugApplicationNature;
import com.buglabs.dragonfly.builder.IncrementalProjectBuilder;

/**
 * 
 * @author Angel Roman
 * 
 */
public class BUGApplicationProjectValidator {

	private static boolean isShowError;

	/**
	 * @param project
	 *            Project that needs to be validated
	 * @param showError
	 *            Flag to show errors
	 * @return Returns <code>true</code> if project is valid,
	 *         <code>false</code> otherwise.
	 * @throws CoreException
	 */
	public static boolean validate(IProject project, boolean showError) throws CoreException {
		isShowError = showError;
		if (!project.hasNature(BugApplicationNature.ID)) {
			showError("The project does not appear to be a BUG Application project");
			return false;
		}

		IJavaProject jproj = JavaCore.create(project);
		String targetPlatform = jproj.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true);
		if (!targetPlatform.equals(JavaCore.VERSION_1_2) && !targetPlatform.equals(JavaCore.VERSION_1_6)) {
			showError("Compiler target platform must be 1.4 for a PhoneME BUG or 1.6 for an OpenJDK BUG.  " +
					"Please modify your project's execution environment settings.");
			return false;
		}

		/*project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		if (markers != null && markers.length > 0) {
			for (int i = 0; i < markers.length; ++i) {
				if (markers[i].getAttribute(IMarker.SEVERITY).equals(new Integer(IMarker.SEVERITY_ERROR))) {
					showError("BUG Application contains errors. Please resolve them before uploading to BUGnet");
					return false;
				}
			}
		}*/

		IFile manifest = project.getFile("META-INF/MANIFEST.MF");
		if (!(manifest != null && manifest.exists())) {
			showError("The file META-INF/MANIFEST.MF does not exist. Please create one before uploading to BUGnet");
			return false;
		}
		
		// ensure that the manifest starts w/ Manifest-Version:
		InputStream is = manifest.getContents();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {
			String line = reader.readLine();
			if (line == null || line.length() == 0 || !line.startsWith("Manifest-Version")) {
				showError("The file META-INF/MANIFEST.MF is missing the Manifest-Version. " + 
						"Add 'Manifest-Version: 1.0' (without the quotes) as the first line of META-INF/MANIFEST.MF");
				return false;
			}
		} catch (IOException e) {
			showError("The file META-INF/MANIFEST.MF is corrupt or missing.  Please create one before uploading to BUGnet");
			return false;
		}

		return true;
	}

	private static void showError(String msg) {
		if (isShowError) {
			Shell shell = null;
			IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (win != null) {
				shell = win.getShell();
			}
			MessageDialog.openError(shell, "Project Validation Error", msg);
		}
	}
}
