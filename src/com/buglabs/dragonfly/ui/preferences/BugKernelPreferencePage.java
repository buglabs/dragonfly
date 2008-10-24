package com.buglabs.dragonfly.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class BugKernelPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private String bugKernelLoc;

	private Composite top;

	private Text txtBugKernelLoc;

	protected Control createContents(Composite parent) {
		top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		top.setLayout(layout);

		Label lblBugKernelLoc = new Label(top, SWT.NONE);
		lblBugKernelLoc.setText("BUG Library Location:");

		txtBugKernelLoc = new Text(top, SWT.READ_ONLY | SWT.BORDER);
		txtBugKernelLoc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtBugKernelLoc.setText(bugKernelLoc);
		txtBugKernelLoc.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				bugKernelLoc = ((Text) e.widget).getText();
			}
		});

		txtBugKernelLoc.setEnabled(false);

		Button btnBrowse = new Button(top, SWT.PUSH);
		btnBrowse.setText("&Browse");
		btnBrowse.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {

			}

			public void mouseDown(MouseEvent e) {

			}

			public void mouseUp(MouseEvent e) {
				DirectoryDialog dd = new DirectoryDialog(top.getShell());
				FileDialog fd = new FileDialog(top.getShell(), SWT.OPEN);
				String loc = dd.open();
				if (loc != null) {
					bugKernelLoc = loc;
					txtBugKernelLoc.setText(loc);
				}
			}

		});

		return top;
	}

	private void initPreferences() {
		// bugKernelLoc =
		// DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_BUG_KERNEL_LOC);
	}

	public void init(IWorkbench workbench) {
		initPreferences();
	}

	public boolean performOk() {
		// DragonflyActivator.getDefault().getPluginPreferences().setValue(DragonflyActivator.PREF_BUG_KERNEL_LOC,
		// bugKernelLoc);

		return super.performOk();
	}
}
