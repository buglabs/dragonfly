package com.buglabs.dragonfly.ui.views;

import java.util.Iterator;
import java.util.List;



import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.buglabs.dragonfly.model.BUGNetProgramReferenceNode;

/**
 * A JFace Viewer for drawing bug applications.
 * The model is set in setInput, which should take a List<BUGNetProgramReferenceNodes>
 * redraw() will draw the items in the list
 * 
 * @author brian
 *
 */
public class BugnetViewer extends Viewer {

	private Composite composite;
	private ScrolledForm form;
	private FormToolkit toolkit;
	private List<BUGNetProgramReferenceNode> model;
	
	public BugnetViewer(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		// main form
		form = toolkit.createScrolledForm(parent);
		form.setExpandHorizontal(true);
		GridData formGD = new GridData(GridData.FILL_BOTH);
		formGD.grabExcessHorizontalSpace = true;
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;
		form.setLayoutData(formGD);
		form.getBody().setLayout(layout);			
		
		// main child of form that contains all the stuff
		composite = toolkit.createComposite(form.getBody());
		composite.setLayout(new GridLayout(1, false));
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
		composite.setLayoutData(td);
	}
	
	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public Object getInput() {
		return model;
	}

	@Override
	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		// Refreshes this viewer completely with information freshly obtained from this viewer's model.
		redraw();
	}

	@Override
	public void setInput(Object input) {
		model = (List<BUGNetProgramReferenceNode>)input;
		redraw();
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Draw the thing using model
	 */
	private void redraw() {
		Label label;
		if (model==null || model.size() == 0) {
			label = new Label(composite, SWT.LEFT);
			label.setText("No BUG Apps Found.");
			return;
		}
		Iterator<BUGNetProgramReferenceNode> iterator = model.iterator();
		BugnetApplicationItemDrawer appItemDrawer 
			= new BugnetApplicationItemDrawer(composite);
		BUGNetProgramReferenceNode node;
		while (iterator.hasNext()) {
			node = iterator.next();
			appItemDrawer.draw(node);
		}
		appItemDrawer.dispose();
		// refresh the view
		composite.layout();
		form.reflow(true);
	}

}
