package com.buglabs.dragonfly.ui.views.bugnet;

import java.util.Iterator;
import java.util.List;



import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
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
    private static final String NO_APPLICATIONS_TEXT = "No applications found."; //$NON-NLS-1$
	private Composite composite;
	private ScrolledForm form;
	private FormToolkit toolkit;
	private BugnetApplicationList model;
	
	
	public BugnetViewer(ScrolledForm form) {
        form.setExpandHorizontal(true);
        GridData formGD = new GridData(GridData.FILL_BOTH);
        formGD.grabExcessHorizontalSpace = true;
        TableWrapLayout layout = new TableWrapLayout();
        layout.leftMargin = layout.rightMargin = 0;
        layout.numColumns = 1;
        form.setLayoutData(formGD);
        form.getBody().setLayout(layout);
        
        toolkit = new FormToolkit(form.getDisplay());
        this.form = form;
        
	    createControl();
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
		model = (BugnetApplicationList)input;
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * create main child of form that contains all the stuff
	 */
	private void createControl() {
        // main child of form that contains all the stuff
        composite = toolkit.createComposite(form.getBody());
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        composite.setLayout(new GridLayout(1, false));
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        composite.setLayoutData(td);	    
	}
	
	private void drawApplications() {
        if (model == null) { drawNoApplications(); return; }
        if (model.getApplications().size() == 0) { drawNoApplications(); return; }
        
        List<BUGNetProgramReferenceNode> applications = model.getApplications();
        Iterator<BUGNetProgramReferenceNode> iterator = applications.iterator();
        BugnetApplicationItemDrawer appItemDrawer 
            = new BugnetApplicationItemDrawer(composite);
        BUGNetProgramReferenceNode node;
        while (iterator.hasNext()) {
            node = iterator.next();
            appItemDrawer.draw(node);
        }
	}
	
	private void drawNoApplications() {
	    Label label = new Label(composite, SWT.LEFT);
	    label.setText(model.getNoAppsMessage());
	}
	
	/**
	 * Draw the thing using model
	 */
	private void redraw() {
	    if (form == null) return;
	    if (composite != null) composite.dispose();
	    createControl();        
		drawApplications();
		// refresh the view
	    form.reflow(true);
	}

}
