package com.buglabs.dragonfly.ui.dnd;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.BugApplicationNature;
import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.ApplicationFolderNode;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.FolderNode;
import com.buglabs.dragonfly.ui.actions.UpsertToBugAction;
import com.buglabs.dragonfly.ui.jobs.UploadJARToBUGChangeListener;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;

public class ProgramToViewDropAdapter extends ViewerDropAdapter {

	private FolderNode fnode;

	public ProgramToViewDropAdapter(Viewer viewer) {
		super(viewer);
	}

	public boolean performDrop(Object data) {
		Object[] dropData = (Object[]) data;
		if (dropData[0] instanceof IProject) {
			IProject proj = (IProject) dropData[0];
			
			// The following checks the bug to see if the app exists, but the UpsertToBugAction already does this, so commented out
			//
			//ApplicationFolderNode node = (ApplicationFolderNode) fnode;
			//if (bugExists(node, proj)) {
			//	boolean bugApplicationOverwrite = MessageDialog.openQuestion(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
			//			"Export BUG", "BUG Application '" + ((IProject) dropData[0]).getName() + "' already exists in BUG '"
			//					+ node.getParent().getName() + "'\nAre you sure you want overwrite?");
			//	if (!bugApplicationOverwrite) {
			//		return false;
			//	}
			//}
			//
			
			if (proj != null) {
				try {
					if(ProjectUtils.existsProblems(proj)){
						IStatus status = new Status(IStatus.ERROR,DragonflyActivator.PLUGIN_ID,"Application '" + proj.getName() + "' contains errors. Please fix errors before uploading.",null);
						throw new CoreException(status);
					}
					if (proj.hasNature(BugApplicationNature.ID)) {
						BugConnection bugProjNode = (BugConnection) fnode.getParent();

						UpsertToBugAction action = new UpsertToBugAction(bugProjNode.getUrl().toString(), bugProjNode.getName(), proj,
								new UploadJARToBUGChangeListener(bugProjNode));

						action.run();
						return true;
					}
				} catch (CoreException e) {
					UIUtils.handleVisualError(e.getMessage(), e);
					return false;
				}
			}
		}
		return false;
	}

	private boolean bugExists(ApplicationFolderNode programFolder, IProject proj) {
		return (programFolder.getChildren(proj.getName()).size() == 0) ? false : true;
	}

	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		if (target instanceof ApplicationFolderNode) {
			fnode = (FolderNode) target;
			if (fnode.getName().equals(Messages.getString("ProgramDropAdapter.0"))) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

}
