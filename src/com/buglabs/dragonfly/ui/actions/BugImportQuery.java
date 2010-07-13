package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Class implements the IImportQuery interface used by PluginImportOperation.
 * PDE invokes doQuery when trying to import an already existing project.
 * 
 * @author Angel Roman
 * 
 */
public class BugImportQuery implements IImportQuery {

	private Shell shell;

	private String message;

	private boolean replace = false;

	public BugImportQuery() {

	}

	public int doQuery(String message) {
		this.message = message;
		IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		if (win != null) {
			shell = win.getShell();
		}

		Display disp = PlatformUI.getWorkbench().getDisplay();

		disp.syncExec(new Runnable() {

			public void run() {
				replace = MessageDialog.openQuestion(shell, "BUG Import", BugImportQuery.this.message);
			}

		});

		if (replace) {
			return IImportQuery.YES;
		} else {
			return IImportQuery.NO;
		}
	}
}
