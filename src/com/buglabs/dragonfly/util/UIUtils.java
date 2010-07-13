/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.util;

import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.LoadingBugNode;

/**
 * @author Ken
 */
public class UIUtils {

	public static final int FORMAT_METHOD_STRING = 1;

	/**
	 * Creates a 2-field composite with Grid layout and places label and text
	 * widgets in the fields.
	 * 
	 * @param parent
	 * @param label
	 * @return
	 */
	public static Text createTextField(Composite parent, String label) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label labelControl = new Label(c, SWT.NONE);
		labelControl.setText(label);

		Text text = new Text(c, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return text;
	}

	/**
	 * Creates a 2-field composite with GridLayout and specify number of columns
	 * 
	 * @param parent
	 * @param columns
	 * @param label
	 * @return
	 */
	public static Text createTextField(Composite parent, int columns, String label, int style, int limit) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(columns, false));
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label labelControl = new Label(c, SWT.NONE);
		labelControl.setText(label);

		Text text = new Text(c, style);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setTextLimit(limit);

		return text;
	}

	/**
	 * Creates a 2-field composite with GridLayout and specify number of columns
	 * 
	 * @param parent
	 * @param columns
	 * @param label
	 * @return
	 */
	public static Text createNumericField(Composite parent, int columns, String label, int style, int limit) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(columns, false));
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label labelControl = new Label(c, SWT.NONE);
		labelControl.setText(label);

		Text text = new Text(c, style);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setTextLimit(limit);
		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				e.doit = true;
				if (!Character.isDigit(e.character))
					e.doit = false;
				if (e.character == '\b')
					e.doit = true;
			}
		});

		return text;
	}

	/**
	 * Creates a 2-field composite with Grid layout and places label and text
	 * widgets in the fields.
	 * 
	 * @param parent
	 * @param label
	 * @param fieldWidth
	 * @return
	 */
	public static Text createTextField(Composite parent, String label, int fieldWidth) {
		Text field = createTextField(parent, label);

		GridData gData = new GridData();
		gData.widthHint = fieldWidth;
		field.setLayoutData(gData);

		return field;
	}

	/**
	 * Creates a 2-field composite with Grid layout and places label and text
	 * widgets in the fields.
	 * 
	 * @param parent
	 * @param label
	 * @return
	 */
	public static Text createTextNoCompField(Composite parent, String label) {
		Label labelControl = new Label(parent, SWT.NONE);
		labelControl.setText(label);

		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return text;
	}

	/**
	 * Creates a 2-field composite with Grid layout and places label and text
	 * widgets in the fields.
	 * 
	 * @param parent
	 * @param label
	 * @return
	 */
	public static Text createTextNoCompField(Composite parent, String label, int width) {
		Label labelControl = new Label(parent, SWT.NONE);
		labelControl.setText(label);

		Text text = new Text(parent, SWT.BORDER);
		GridData gData = new GridData(GridData.FILL_HORIZONTAL);
		gData.widthHint = width;
		text.setLayoutData(gData);

		return text;
	}

	/**
	 * Creates a 2-field composite with Grid layout and places label and combo
	 * widgets in the fields.
	 * 
	 * @param parent
	 * @param label
	 * @return
	 */
	public static CCombo createCComboField(Composite parent, String label, int style) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label labelControl = new Label(c, SWT.NONE);
		labelControl.setText(label);

		CCombo combo = new CCombo(c, style);

		return combo;
	}

	/**
	 * Creates a 2-field composite with Grid layout and places label and combo
	 * widgets in the fields.
	 * 
	 * @param parent
	 * @param label
	 * @return
	 */
	public static CCombo createCComboNoCompField(Composite parent, String label, int style) {
		Label labelControl = new Label(parent, SWT.NONE);
		labelControl.setText(label);

		CCombo combo = new CCombo(parent, style);

		return combo;
	}

	public static Button createButtonField(Composite parent, String label, int buttonFlags) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label labelControl = new Label(c, SWT.NONE);
		labelControl.setText(label);

		Button button = new Button(c, buttonFlags);

		return button;
	}

	public static Button createButtonNoCompField(Composite parent, String label, int buttonFlags) {
		Label labelControl = new Label(parent, SWT.NONE);
		labelControl.setText(label);

		Button button = new Button(parent, buttonFlags);

		return button;
	}

	public static Button createCheckboxField(Composite parent, String label) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText(label);

		return button;
	}

	public static Text createDirectoryField(final Composite parent, String label) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(3, false));
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label labelControl = new Label(c, SWT.NONE);
		labelControl.setText(label);

		final Text text = new Text(c, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button button = new Button(c, SWT.NONE);
		button.setText("...");
		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());

				dialog.setMessage("Select Directory");

				String path = dialog.open();

				if (!stringEmpty(path)) {
					text.setText(path);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		return text;
	}

	/**
	 * create a location field with label on the top, text field below label and
	 * browse button to the right of text field.
	 * 
	 * @param parent
	 * @param label
	 * @return text field
	 */
	public static Text createDirectoryLabelOnTopField(final Composite parent, String label) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		// composite is used to correctly position widgets, by default this
		// composite is indented.
		// Need to force positioning to the right.
		data.horizontalIndent = -5;
		c.setLayoutData(data);
		GridData gridData = null;

		Label labelControl = new Label(c, SWT.NONE);
		labelControl.setText(label);
		// make label take up whole row and span 2 columns
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		labelControl.setLayoutData(gridData);

		// text and button will be on the same line
		final Text text = new Text(c, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button button = new Button(c, SWT.NONE);
		button.setText("Browse...");

		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());

				dialog.setMessage("Select Directory");

				String path = dialog.open();

				if (!stringEmpty(path)) {
					text.setText(path);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});
		return text;
	}

	public static Text test(Composite parent, String label) {
		Label labelControl = new Label(parent, SWT.NONE);
		labelControl.setText(label);

		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return text;
	}

	public static Text createDirectoryNoCompField(final Composite parent, String label) {
		Label labelControl = new Label(parent, SWT.NONE);
		labelControl.setText(label);

		final Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button button = new Button(parent, SWT.NONE);
		button.setText("...");
		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());

				dialog.setMessage("Select Directory");

				String path = dialog.open();

				if (!stringEmpty(path)) {
					text.setText(path);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		return text;
	}

	public static Text createFileField(final Composite parent, String label) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(3, false));
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label labelControl = new Label(c, SWT.NONE);
		labelControl.setText(label);

		final Text text = new Text(c, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button button = new Button(c, SWT.NONE);

		button.setText("...");
		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell());

				String path = dialog.open();

				if (!stringEmpty(path)) {
					text.setText(path);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});
		text.setData(button);
		return text;
	}

	/**
	 * create XML field with label on the top, text field below label and browse
	 * button to the right of text field.
	 * 
	 * @param parent
	 * @param label
	 * @param filterExtensions
	 * @param filterNames
	 * @return text field
	 */
	public static Text createDyanamicFileField(final Composite parent, String label, final String[] filterExtensions, final String[] filterNames) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		c.setLayoutData(data);
		GridData gridData = null;

		if (label != null) {
			Label labelControl = new Label(c, SWT.NONE);
			labelControl.setText(label);
			// make label take up whole row and span 2 columns
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;
			gridData.grabExcessHorizontalSpace = true;
			labelControl.setLayoutData(gridData);
		}

		// text and button will be on the same line
		final Text text = new Text(c, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button button = new Button(c, SWT.NONE);
		button.setText("...");

		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell());
				dialog.setFilterExtensions(filterExtensions);
				dialog.setFilterNames(filterNames);

				String path = dialog.open();

				if (!stringEmpty(path)) {
					text.setText(path);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});
		text.setData(button);
		return text;
	}

	public static Text createFileNoCompField(final Composite parent, String label) {
		Label labelControl = new Label(parent, SWT.NONE);
		labelControl.setText(label);

		final Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button button = new Button(parent, SWT.NONE);

		button.setText("...");
		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell());

				String path = dialog.open();

				if (!stringEmpty(path)) {
					text.setText(path);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});
		text.setData(button);
		return text;
	}

	/**
	 * Checks to see if a string has visible characters. Null safe.
	 * 
	 * @param string
	 * @return
	 */
	public static boolean stringEmpty(String string) {
		if (string == null) {
			return true;
		}

		if (string.trim().length() == 0) {
			return true;
		}

		return false;
	}

	/**
	 * Removes margins from a GridLayout
	 * 
	 * @param layout
	 * @return
	 */
	public static GridLayout StripGridLayoutMargins(GridLayout layout) {
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		return layout;
	}

	/**
	 * Creates a separator label on a GridLayout composite.
	 * 
	 * @param parent
	 * @return
	 */
	public static Label createHorizontalSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gData = new GridData(GridData.FILL_HORIZONTAL);
		gData.heightHint = 12;
		separator.setLayoutData(gData);

		return separator;
	}

	/**
	 * Return the active workbench shell or null if none exists.
	 * 
	 * @return
	 */
	public static Shell getWorkbenchShell() {
		if (PlatformUI.isWorkbenchRunning()) {
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			}
		}

		return new Shell(new Display());
	}

	/**
	 * "Clear" a composite by calling <code>dispose</code> on it's children.
	 * 
	 * @param composite
	 */
	public static void removeChildren(Composite composite) {
		if (composite != null && composite.getChildren() != null) {
			while (composite.getChildren().length > 0) {
				Control child = composite.getChildren()[0];
				if (child instanceof Composite) {
					removeChildren((Composite) child);
				}
				child.dispose();
			}
		}
	}

	/**
	 * Given a control, attach a GridData that spans cells as defined in
	 * horizontal and vertical parameters. GridData object is returned for
	 * further modification.
	 * 
	 * @param control
	 * @param horizontal
	 * @param vertical
	 * @return
	 */
	public static GridData controlSpanCells(Control control, int horizontal, int vertical) {
		GridData gData = new GridData();
		gData.horizontalSpan = horizontal;
		gData.verticalSpan = vertical;
		control.setLayoutData(gData);

		return gData;
	}

	/**
	 * The table widget has a bug in that getCheckedItems always returns
	 * nothing. This method determines if any items in a table have been
	 * checked. Useful for validation.
	 * 
	 * @param table
	 * @return
	 */
	public static boolean tableHasCheckedItems(Table table) {
		for (int i = 0; i < table.getItems().length; ++i) {
			TableItem tc = table.getItem(i);
			if (tc.getChecked()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Modify the input text based on the format flag. If the flag is
	 * unrecoginzied, the original string is returned.
	 * 
	 * FORMAT_METHOD_STRING - make a string conform to basic method name
	 * conventions.
	 * 
	 * @param text
	 * @param format
	 * @return
	 */
	public static String formatString(String text, int format) {
		switch (format) {
		case FORMAT_METHOD_STRING:
			text = text.replace(' ', '_');
			text = text.replace('.', '_');
			text = text.replace(';', '_');
			return text;
		}

		return text;
	}

	/**
	 * Will truncated the string to 35 characters in length adding an ellipsis
	 * for the last three characters.
	 * 
	 * @param description
	 * @return
	 */
	public static String truncateString(String description, int length) {
		String truncated = "";
		if (description.length() > length) {
			truncated = description.substring(0, length - 3);
			truncated = truncated + "...";
		} else {
			truncated = description;
		}

		return truncated;
	}

	/**
	 * Sets horizontal indent for a widget
	 * 
	 * @param indent
	 * @return data
	 */
	public static GridData horizontalIndent(int indent) {
		GridData data = new GridData();
		data.horizontalIndent = indent;
		return data;
	}

	/**
	 * Logs an error to the Eclipse error log and displays message in dialog
	 * box.
	 * 
	 * @param message
	 * @param e
	 */
	public static void handleVisualError(String message, Exception e) {
		DragonflyActivator.getDefault().getLog().log(new Status(Status.ERROR, DragonflyActivator.PLUGIN_ID, 0, message, e));
		final Display disp = PlatformUI.getWorkbench().getDisplay();
		final String msg = message;

		disp.syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(new Shell(disp), "An error has occurred.", msg + "\n\nPlease consult log file for additional details.");
			}
		});
	}

	/**
	 * Displays information message to the user
	 * 
	 * @param message
	 */
	public static void giveVisualInformation(final String message) {
		final Display disp = PlatformUI.getWorkbench().getDisplay();
		disp.syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(new Shell(disp), "Information", message);
			}
		});
	}

	/**
	 * Displays information message to the user
	 * 
	 * @param message
	 */
	public static void giveNonVisualInformation(final String message) {
		DragonflyActivator.getDefault().getLog().log(new Status(Status.INFO, DragonflyActivator.PLUGIN_ID, message));
	}

	/**
	 * Log an error to the Eclipse log.
	 * 
	 * @param message
	 * @param e1
	 */
	public static void handleNonvisualError(String message, Exception e) {
		DragonflyActivator.getDefault().getLog().log(new Status(Status.ERROR, DragonflyActivator.PLUGIN_ID, 0, message, e));
	}

	/**
	 * Log warning to the Eclipse log.
	 * 
	 * @param message
	 * @param e1
	 */
	public static void handleNonvisualWarning(String message, Exception e) {
		DragonflyActivator da = DragonflyActivator.getDefault();

		if (da == null) {
			System.err.println(message);
			System.err.println(e.toString());
			return;
		}

		ILog log = da.getLog();

		if (log == null) {
			System.err.println(message);
			System.err.println(e.toString());
			return;
		}

		log.log(new Status(Status.WARNING, DragonflyActivator.PLUGIN_ID, 0, message, e));
	}

	/**
	 * A simple abstraction of logging for use in the plug-in
	 * 
	 * @param status
	 */
	public static void log(Status status) {
		DragonflyActivator da = DragonflyActivator.getDefault();
		if (da == null || da.getLog() == null) {
			System.out.println("[Log Message]" + status.toString());
			return;
		}
		da.getLog().log(status);
	}

	synchronized public static void removeLoadingMessageBug(ITreeNode root, IProgressMonitor monitor) {
		Object[] objects = root.getChildren().toArray();

		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof LoadingBugNode) {
				root.getChildren().remove(objects[i]);
				DragonflyActivator.getDefault().fireModelChangeEvent(new PropertyChangeEvent(monitor, "Root", null, root));
			}
		}
	}

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}
}
