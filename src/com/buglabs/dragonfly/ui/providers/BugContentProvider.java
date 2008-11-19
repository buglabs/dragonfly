/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/

package com.buglabs.dragonfly.ui.providers;

import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BugProjectNode;
import com.buglabs.dragonfly.model.FolderNode;
import com.buglabs.dragonfly.model.IModelChangeListener;
import com.buglabs.dragonfly.model.IModelNode;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.ui.jobs.LaunchPhysicalEditorJob;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Content provider for Common Navigator Framework (CNF) view. Exposes BUG
 * project metadata to Common Navigator.
 * 
 * @author ken
 * 
 */
public class BugContentProvider implements ITreeContentProvider, IModelChangeListener {
	private static Object[] EMPTY_OBJ_ARRAY = {};

	private Viewer viewer;

	public BugContentProvider() {
		DragonflyActivator.getDefault().addListener(this);
	}

	public Object[] getChildren(Object parentElement) {

		if (parentElement instanceof IProject) {
			IProject project = (IProject) parentElement;

			try {
				String bugUrl = null;
				try {
					bugUrl = project.getPersistentProperty(new QualifiedName("com.buglabs", "url")); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (CoreException e) {
					// TODO handle this later by setting defaults and a project
					// nature
					// No such property exists
				}

				if (bugUrl != null) {
					BugProjectNode pbn = new BugProjectNode(project.getName(), new URL(bugUrl), project);

					if (hasBuilder(project, "com.buglabs.dragonfly.Builder")) { //$NON-NLS-1$
						List elements = new ArrayList();

						ITreeNode modulesNode = new FolderNode(Messages.getString("BugContentProvider.3"), pbn); //$NON-NLS-1$
						modulesNode.setChildren(BugWSHelper.getModuleList(modulesNode, pbn.getModuleURL()));
						elements.add(modulesNode);

						ITreeNode programNode = new FolderNode(Messages.getString("BugContentProvider.4"), pbn); //$NON-NLS-1$
						programNode.setChildren(BugWSHelper.getPrograms(pbn.getProgramURL()));
						elements.add(programNode);

						Job j = new LaunchPhysicalEditorJob(pbn);
						j.setPriority(Job.SHORT);
						j.schedule();

						/*
						 * j = new RegisterEventListenerJob(pbn);
						 * j.setPriority(Job.LONG); j.schedule();
						 */

						return elements.toArray(new ITreeNode[elements.size()]);
					}
				}
			} catch (Exception e) {
				UIUtils.handleVisualError(Messages.getString("BugContentProvider.5"), e); //$NON-NLS-1$
				return EMPTY_OBJ_ARRAY;
			}
		} else if (parentElement instanceof ITreeNode) {
			ITreeNode tnode = (ITreeNode) parentElement;
			return tnode.getChildren().toArray(new IModelNode[tnode.getChildren().size()]);
		}

		return EMPTY_OBJ_ARRAY;
	}

	public Object getParent(Object element) {
		if (element instanceof ITreeNode) {
			return ((ITreeNode) element).getParent();
		}

		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ITreeNode) {
			return ((ITreeNode) element).hasChildren();
		}

		if (element instanceof IProject) {
			return ((IProject) element).exists();
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {

		return getChildren(inputElement);
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	private boolean hasBuilder(IProject project, String builderId) throws CoreException {
		IProjectDescription description = project.getDescription();

		ICommand[] buildSpec = description.getBuildSpec();

		for (int i = 0; i < buildSpec.length; ++i) {
			if (buildSpec[i].getBuilderName().equals(builderId)) {
				return true;
			}
		}

		return false;
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.getControl().getDisplay().syncExec(new Runnable() {

				public void run() {
					viewer.refresh();
				}
			});
		}
	}

}
