package com.buglabs.dragonfly.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.buglabs.dragonfly.BugApplicationNature;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;

public class BugProjectUtil extends ProjectUtils {


	/**
	 * Formats project name into valid form for use with PDE
	 * NewProjectCreationOperation.
	 * 
	 * projName.toLowerCase().replace(".", "_").trim().replace(" ", "");
	 * 
	 * @param projName
	 * @return
	 */
	public static String formatProjectNameAsPackage(String projName) {

		String temp = removeFaultyCharacters(projName.toLowerCase());
		return temp;
	}

	private static String removeFaultyCharacters(String projName) {
		return projName.replace('.', '_').trim().replaceAll(" ", "_"); // all spaces need to be replaced with '_' as per PDE handling of spaces
	}

	public static String formatProjectNameAsClassName(String projName) {
		String temp = removeFaultyCharacters(projName);

		if (temp.length() > 0) {
			String upperCase = temp.toUpperCase();
			char[] characters = temp.toCharArray();
			characters[0] = upperCase.charAt(0);

			temp = new String(characters);
		}

		return temp;
	}

	/**
	 * Builds a list of Concierge Projects
	 * 
	 * @return A list of projects in the workspace containing the Concierge
	 *         Project Nature.
	 * @throws CoreException
	 */
	public static List getWSBugProjects() {
		return getWSBugProjects(null);
	}
	
	/**
	 * Builds a list of the BUG project names currently in the workspace
	 * 
	 * @return
	 */
	public static List<String> getWSBugProjectNames() {
		return getProjectNames(getWSBugProjects());
	}	
	
	/**
	 * Gets the projects listed in projectNames,  If projectNames is null
	 * it gets all the projects in the workspace
	 * 
	 * @param projectNames - if null, gets all in workspace
	 * @return
	 */
	private static List getWSBugProjects(final String[] projectNames) {
		final List projects = new Vector();
		final List projectNameList;
		if (projectNames != null) 
			projectNameList = Arrays.asList(projectNames);
		else 
			projectNameList = null;
		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
		try {
			wsroot.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if (resource.getType() == IResource.ROOT) {
						return true;
					} else if (resource.getType() == IResource.PROJECT) {
						IProject project = (IProject) resource;
						if (project.isOpen() 
								&& project.hasNature(BugApplicationNature.ID)
								&& (projectNameList == null 
										|| projectNameList.contains(project.getName()))) {
							projects.add(project);
						}
					}
					return false;
				}
			});
		} catch (CoreException e) {
			UIUtils.handleVisualError("Unable to retrieve Bug Projects", e);
		}
		return projects;		
	}

	private static List<String> getProjectNames(List projects) {
		ArrayList<String> names = new ArrayList<String>(projects.size());
		Iterator projIter = projects.iterator();

		while (projIter.hasNext()) {
			names.add(((IProject) projIter.next()).getName());
		}

		return names;
	}
	
}
