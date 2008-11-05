/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.bugnet.net/legal/epl-v10.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui.views;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.IBUGnetAuthenticationListener;
import com.buglabs.dragonfly.bugnet.BugnetWSHelper;
import com.buglabs.dragonfly.exception.BugnetAuthenticationException;
import com.buglabs.dragonfly.model.BUGNetProgramReferenceNode;
import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ILinkableModelNode;
import com.buglabs.dragonfly.model.IModelChangeListener;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.ModelNodeChangeEvent;
import com.buglabs.dragonfly.swt.Rating;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.BugnetAuthenticationHelper;
import com.buglabs.dragonfly.ui.actions.BugNetToIDEActionDelegate;
import com.buglabs.dragonfly.ui.actions.ExportJarToBUGNetAction;
import com.buglabs.dragonfly.ui.actions.ImportFromBUGNetAction;
import com.buglabs.dragonfly.ui.actions.LaunchBrowserAction;
import com.buglabs.dragonfly.ui.actions.RefreshBugNetViewAction;
import com.buglabs.dragonfly.ui.actions.SearchBugNetAction;
import com.buglabs.dragonfly.ui.dnd.BugNETProgramReferenceNodeTransfer;
import com.buglabs.dragonfly.ui.editors.PhysicalEditor;
import com.buglabs.dragonfly.ui.jobs.BUGNetRefreshJob;
import com.buglabs.dragonfly.ui.jobs.PopulateBUGNetViewModelJob;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.dragonfly.util.URLUtils;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;

/**
 * This view displays content from BUGNet in the context of the current editor
 * view.
 * 
 * @author ken
 * 
 */
public class BUGNetView extends ViewPart implements IBUGnetAuthenticationListener, IModelChangeListener {

	public static final String VIEW_ID = "com.buglabs.dragonfly.ui.BUGNetView"; //$NON-NLS-1$

	private FormToolkit toolkit;

	private ScrolledForm form;

	private ITreeNode model;

	private ImageRegistry imgreg;

	private Composite top;

	private Font descriptionFont;

	private Font titleFont;

	private Color lightGrayColor;

	private Color lightGray2Color;

	private Color borderColor;

	private Label descLabel;

	private Collection labelList;

	private ArrayList originalDescriptionList;

	private PopulateBUGNetViewModelJob job;

	private static BUGNetView view;

	private static int CHAR_HEIGHT = 8;

	private static final String APPLICATION_MODULE_SECTION = "Applications by Modules"; //$NON-NLS-1$

	private Map sectionMap; // stores id and state of expansion for each section

	private static final int TRUE = 1;

	private static final int FALSE = 0;

	private File sectionsFileName = null; // file name where bugs are persisted

	private final String STATE_FILE_NAME = "bugnet-sections.xml"; //$NON-NLS-1$

	protected PhysicalEditor lastPhysicalEditor;

	private Map moduleTrackerMap = null;

	public List programsForPackages;

	public BUGNetView() {
		imgreg = new ImageRegistry();
		view = this;
		moduleTrackerMap = new HashMap();
	}

	public void dispose() {
		super.dispose();

		try {
			saveState();
		} catch (IOException e) {
			UIUtils.handleNonvisualError("Unable to save state of BUGnet view", e);
		}
		if (titleFont != null) {
			titleFont.dispose();
		}

		if (descriptionFont != null) {
			descriptionFont.dispose();
		}

		if (lightGrayColor != null) {
			lightGrayColor.dispose();
		}

		if (lightGrayColor != null) {
			lightGray2Color.dispose();
		}

		if(borderColor != null) {
			borderColor.dispose();
		}
	}

	public static BUGNetView getView() {
		return view;
	}

	public void init(IViewSite site) throws PartInitException {
		setSite(site);
		site.getActionBars().getToolBarManager().add(new RefreshBugNetViewAction(this));
		site.getActionBars().getToolBarManager().add(new SearchBugNetAction(this));

		// set description font depending on OS
		if(System.getProperty("os.name").startsWith("Mac")){
			CHAR_HEIGHT = 12;
		}
		else if(System.getProperty("os.name").toLowerCase().startsWith("win")){
			CHAR_HEIGHT = 9;
		}

		initializeFonts();
		initializeColors();

		DragonflyActivator.getDefault().addBUGnetAuthenticationListener(this);
		DragonflyActivator.getDefault().addListener(this);

		// try to create file name to store section properties
		sectionsFileName = Activator.getDefault().getStateLocation().append(STATE_FILE_NAME).toFile();

		if (!sectionsFileName.exists()) {
			try {
				sectionsFileName.createNewFile();
			} catch (IOException e) {
				UIUtils.handleNonvisualError("Unable to create file", e);
			}
		}

		sectionMap = new HashMap();

		try {
			loadBugnetState();
		} catch (WorkbenchException e) {
			UIUtils.handleNonvisualError("Unable to load state", e);
		} catch (IOException e) {
			UIUtils.handleNonvisualError("Unable to load state", e);
		}

	}

	/**
	 * Loads view's sections' state
	 * @throws WorkbenchException
	 * @throws IOException
	 */
	private void loadBugnetState() throws WorkbenchException, IOException {
		FileReader reader = new FileReader(sectionsFileName);

		// start reading from file only if it's available
		if (reader.ready()) {
			IMemento memento = XMLMemento.createReadRoot(reader);
			IMemento[] sections = memento.getChildren("section");

			for(int i = 0; i < sections.length; i++){
				String id = sections[i].getString("id");
				Integer integer = sections[i].getInteger("isExpanded"); //$NON-NLS-1$
				sectionMap.put(id,integer);
			}
		}
	}

	private void initializeColors() {
		// BB
		lightGrayColor = new Color(Display.getCurrent(), new RGB(247, 247, 247));
		lightGray2Color = new Color(Display.getCurrent(), new RGB(255, 255, 255));
		borderColor = new Color(Display.getCurrent(), new RGB(255, 255, 255));
	}

	public FormToolkit getToolKit() {
		return toolkit;
	}

	private void initializeFonts() {
		FontData[] fdTitle = JFaceResources.getDefaultFont().getFontData();

		for (int i = 0; i < fdTitle.length; ++i) {
			fdTitle[i].setStyle(SWT.BOLD);
		}

		FontData[] fdDescription = JFaceResources.getDefaultFont().getFontData();
		for (int i = 0; i < fdDescription.length; ++i) {
			fdDescription[i].setHeight(CHAR_HEIGHT);
		}

		Display display = getSite().getShell().getDisplay();
		titleFont = new Font(display, fdTitle);
		descriptionFont = new Font(display, fdDescription);
	}

	public void refresh() {
		if (!form.isDisposed()) {
			form.getDisplay().syncExec(new Runnable() {
				public void run() {
					// set the last editor that was opened so that during refresh order of sections is preserved
					IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

					IEditorPart activeEditor = activePage.getActiveEditor();

					if(activeEditor instanceof PhysicalEditor)
						lastPhysicalEditor = (PhysicalEditor) activeEditor; // get current active editor

					clearChildren(form.getBody());
					generateDetail();
				}
			});
		}
	}

	private void clearChildren(Composite comp2) {
		Control[] children = comp2.getChildren();

		for (int i = 0; i < children.length; ++i) {
			children[i].dispose();
		}
	}

	public void createPartControl(Composite parent) {

		top = new Composite(parent, SWT.None);
		top.setLayout(new FillLayout());

		toolkit = new FormToolkit(top.getDisplay());

		generateDetail();
	}

	private synchronized void generateDetail() {
		if (form == null) {
			form = toolkit.createScrolledForm(top);
			form.setExpandHorizontal(true);
			setupDropTarget();
		}

		GridData formGD = new GridData(GridData.FILL_BOTH);
		formGD.grabExcessHorizontalSpace = true;
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;
		form.setLayoutData(formGD);
		form.getBody().setLayout(layout);
		job = new PopulateBUGNetViewModelJob(this, model, form.getBody(), true);
		Job[] jobs = Job.getJobManager().find(PopulateBUGNetViewModelJob.JOB_TITLE);
		if (jobs.length == 0) {
			job.schedule();
			job.addJobChangeListener(new IJobChangeListener(){

				public void aboutToRun(IJobChangeEvent event) {
					// TODO Auto-generated method stub

				}

				public void awake(IJobChangeEvent event) {
					// TODO Auto-generated method stub

				}

				public void done(IJobChangeEvent event) {
					// during refresh make sure special section gets back into the view, and positioned under other sections
					if(lastPhysicalEditor != null){
						Bug bug = lastPhysicalEditor.getBug();
						Job job = new UpdateModulesAppSectionJob("Updating BUGnet view",(BugConnection)bug);
						job.schedule();
					}
					else{
						IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						if(activeWorkbenchWindow != null){
							IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
							IEditorPart activeEditor = activePage.getActiveEditor(); // get current active editor
							if(activeEditor instanceof PhysicalEditor){
								PhysicalEditor editor = (PhysicalEditor)activeEditor;
								Bug bug = editor.getBug();

								Job job = new UpdateModulesAppSectionJob("Updating BUGnet view",(BugConnection)bug);
								job.schedule();
							}
						}
					}
				}

				public void running(IJobChangeEvent event) {
					// TODO Auto-generated method stub

				}

				public void scheduled(IJobChangeEvent event) {
					// TODO Auto-generated method stub

				}

				public void sleeping(IJobChangeEvent event) {
					// TODO Auto-generated method stub

				}

			});
		}
	}

	private void setupDragSource(Hyperlink h) {
		DragSource ds = new DragSource(h, DND.DROP_MOVE | DND.DROP_COPY);

		Transfer[] transfer = new Transfer[] { PluginTransfer.getInstance() };
		ds.setTransfer(transfer);
		ds.addDragListener(new HyperLinkDragSourceListener(h));
	}

	private void setupDropTarget() {
		DropTarget dt = new DropTarget(form.getBody(), DND.DROP_MOVE | DND.DROP_COPY);

		dt.setTransfer(new Transfer[] { ResourceTransfer.getInstance() });

		dt.addDropListener(new DropTargetAdapter() {

			public void dragEnter(DropTargetEvent event) {
				Object obj = event.data;

				if (ResourceTransfer.getInstance().isSupportedType(event.currentDataType)) {

				} else {
					event.detail = DND.DROP_NONE;
				}

			}

			public void drop(DropTargetEvent event) {
				Object obj = event.data;

				if (obj instanceof IResource[]) {
					if (((IResource[]) obj).length > 0) {
						IResource res = ((IResource[]) obj)[0];

						if (res instanceof IProject) {
							IProject proj = (IProject) res;
							try {
								if(ProjectUtils.existsProblems(proj)){
									IStatus status = new Status(IStatus.ERROR,DragonflyActivator.PLUGIN_ID,"Application '" + proj.getName() + "' contains errors. Please fix errors before uploading.",null);
									throw new CoreException(status);
								}
							} catch (CoreException e) {
								UIUtils.handleVisualError(e.getMessage(), e);
								return;
							}
							ExportJarToBUGNetAction exportAction = new ExportJarToBUGNetAction(proj, new BUGNetRefreshJob());
							exportAction.run();
						}
					} else {
						MessageDialog.openInformation(new Shell(), "Wrong application format", "Unable to upload application to BUGnet. "
								+ "It appears that the application is in the wrong format.");

					}
				}
			}
		});
	}

	/*
	 * 
	 * this gets called to fill the insides of some of the form sections
	 */
	public synchronized void generateDetail(final Composite c, Collection elems) {
		labelList = new ArrayList();
		originalDescriptionList = new ArrayList();
		c.setBackground(borderColor);
		boolean grayBackground = true;
		Color backgroundColor;
		BUGNetProgramReferenceNode prog = null;

		for (Iterator i = elems.iterator(); i.hasNext();) {
			Object detailElem = i.next();

			if (detailElem instanceof BUGNetProgramReferenceNode) {
				prog = (BUGNetProgramReferenceNode) detailElem;

				grayBackground = !grayBackground;
				if (grayBackground) {
					backgroundColor = lightGrayColor;
				} else {
					backgroundColor = lightGray2Color;
				}
				drawAppItem(c, backgroundColor, prog);
			}
		}
	}

	/**
	 * This is a hack so I can call drawAppItem from
	 * a helper class.  Need to call this before calling the next method
	 * 
	 */
	public synchronized void setupForDrawingAppItems() {
		labelList = new ArrayList();
		originalDescriptionList = new ArrayList();		
	}
	
	/**
	 * 
	 * Draws a single app item in a Composite
	 * 
	 * @param parent
	 * @param backgroundColor
	 * @param prog
	 */
	public synchronized void drawAppItem(final Composite parent, 
			Color backgroundColor, BUGNetProgramReferenceNode prog) {
		
		// make sure we set up these array lists
		if (labelList == null) labelList = new ArrayList();
		if (originalDescriptionList == null) originalDescriptionList = new ArrayList();
		
		Composite comp = toolkit.createComposite(parent, SWT.NONE);
		comp.setBackground(backgroundColor);
		GridData compgd = GridDataFactory.fillDefaults().create();
		compgd.grabExcessHorizontalSpace = true;
		compgd.grabExcessVerticalSpace = true;

		comp.setLayoutData(compgd);
		comp.setLayout(new GridLayout(3, false));

		// Get get the image and scale it.
		Image img = DragonflyActivator.getDefault().getImageRegistry().get(prog.getImageKey());
		if (img == null) {
			img = Activator.getDefault().getImageRegistry().get(Activator.IMAGE_KEY_IMAGE_NOT_FOUND);
		}

		Image scaledimg = new Image(null, img.getImageData().scaledTo(52, 39));
		if (imgreg.get(prog.getImageKey() + "_scaled") != null) { //$NON-NLS-1$
			imgreg.put(prog.getImageKey() + "_scaled", scaledimg); //$NON-NLS-1$
		}

		// Label <-- for the image
		Label imgLabel = toolkit.createLabel(comp, ""); //$NON-NLS-1$
		imgLabel.setImage(scaledimg);
		imgLabel.setBackground(backgroundColor);
		GridData imageGD = GridDataFactory.fillDefaults().create();
		imageGD.verticalSpan = 3;
		imgLabel.setLayoutData(imageGD);

		Hyperlink hyperlink = toolkit.createHyperlink(comp, UIUtils.truncateString(prog.getLabel(), 15), SWT.NONE);
		hyperlink.setToolTipText(prog.getLabel());
		hyperlink.addHyperlinkListener(new BUGNetHyperlinkListener(prog));
		HashMap hyperLinkData = new HashMap();
		hyperLinkData.put("program", prog);
		hyperLinkData.put("backgroundcolor", backgroundColor);
		hyperlink.setFont(titleFont);
		hyperlink.setBackground(backgroundColor);
		hyperlink.setForeground(new Color(Display.getCurrent(), new RGB(98,83,125)));
		GridData hGD = GridDataFactory.fillDefaults().create();
		hGD.grabExcessHorizontalSpace = true;
		hyperlink.setLayoutData(hGD);
		hyperlink.setBackgroundMode(SWT.INHERIT_NONE);
		hyperlink.setData(hyperLinkData);

		Set s = System.getProperties().keySet();
		String osName = System.getProperty("os.name");

		if (osName != null && !osName.equals("Mac OS X")) {
			hyperlink.addPaintListener(new PaintListener() {

				public void paintControl(PaintEvent e) {
					((Hyperlink) e.widget).setBackground((Color) ((Map) e.widget.getData()).get("backgroundcolor"));
				}
			});
		}
		setupDragSource(hyperlink);

		double rating = 3.5;
		Rating r = new Rating(comp, SWT.NONE);
		r.setBackground(backgroundColor);
		String ratingVal = prog.getRating();
		if (ratingVal.length() == 0) {
			ratingVal = "0";
		}
		r.setRating(Double.parseDouble(ratingVal));
		GridData ratingGD = GridDataFactory.fillDefaults().create();
		ratingGD.widthHint = 50;
		ratingGD.heightHint = 15;
		r.setLayoutData(ratingGD);


		GridData gd = GridDataFactory.fillDefaults().create();
		gd.verticalAlignment = SWT.TOP;
		gd.widthHint = 150;
		gd.heightHint = 17;
		gd.horizontalSpan = 2;

		Label lblUserNameAndDownloads = new Label(comp, SWT.NONE);
		lblUserNameAndDownloads.setText(prog.getUserName() + "  " + prog.getDownload_count() + " downloads");
		lblUserNameAndDownloads.setLayoutData(gd);
		lblUserNameAndDownloads.setBackground(backgroundColor);
		// BB 
		lblUserNameAndDownloads.setForeground(new Color(Display.getCurrent(), new RGB(123,129,138)));
		String desc = prog.getDescription().trim();

		if (!desc.equals("")) { //$NON-NLS-1$
			descLabel = toolkit.createLabel(comp, desc, SWT.NONE);
			labelList.add(descLabel);
			originalDescriptionList.add(desc);
			descLabel.setFont(descriptionFont);
			descLabel.setLayoutData(gd);
			descLabel.setBackground(backgroundColor);
			// BB
			descLabel.setForeground(new Color(Display.getCurrent(), new RGB(85,85,85)));

			descLabel.setToolTipText(desc);
		}
		final Object[] labelArray = labelList.toArray();
		final Object[] originalDescriptionArray = originalDescriptionList.toArray();


		parent.addListener(SWT.Resize, new Listener(){
			
			public void handleEvent(Event event) {				
				if(descLabel != null && !descLabel.isDisposed()){
					GC gc = new GC(descLabel);

					int width = parent.getSize().x / 6;

					for(int i = 0; i < labelArray.length; i++){
						String shortenText = shortenText(gc, ((String)originalDescriptionArray[i]), width);
						((Label)labelArray[i]).setText(shortenText);
						((Label)labelArray[i]).pack(true);
					}
				}
			}
		});
		createContextMenu(comp, prog);
	}
	
	/**
	 * Convenience method to get the color settings set in this view
	 * Should go away after a full-scale cleanup of this code
	 * 
	 * @return
	 */
	public Color[] getColors() {
		Color[] colors = {borderColor, lightGrayColor, lightGray2Color}; 
		return colors;
	}
	
	/**
	 * Shortens width of the description and adds ellipsis to it.
	 * @param gc
	 * @param t
	 * @param width
	 * @return
	 * 
	 * TODO: Need to polish this algorithm a bit, labels are not being truncated in a unified manner.
	 */
	private String shortenText(GC gc, String t, int width) {
		int w = gc.textExtent(t).x; // width of the string

		if (t == null) return null;

		// shorten the string only if it's less than viewable area
		if(w > width){
			String substring = ""; //$NON-NLS-1$
			try{
				// magic number is 16
				substring = t.substring(0, width-16) + "..."; //$NON-NLS-1$
			}
			catch(StringIndexOutOfBoundsException e){
				return t;
			}
			return substring;
		}
		return t;
	}


	/**
	 * Adds context menu for each BUGNet program
	 * 
	 * @param comp
	 * @param prog
	 */
	private void createContextMenu(final Composite comp, final BUGNetProgramReferenceNode prog) {
		MenuManager menuManager = new MenuManager();

		final ImportFromBUGNetAction action = new ImportFromBUGNetAction();
		menuManager.add(action);

		// create menu and add to composite
		final Menu menu = menuManager.createContextMenu(comp);
		comp.addListener(SWT.MenuDetect, new MenuListener(comp, action, prog, menu));

		// add menu to each child of the composite
		final Control[] children = comp.getChildren();
		for (int j = 0; j < children.length; j++) {
			final Control control = children[j];
			control.addListener(SWT.MenuDetect, new MenuListener(control, action, prog, menu));
		}
	}

	/**
	 * Class responsible for setting up action for execution
	 * 
	 * @author akravets
	 * 
	 */
	private class MenuListener implements Listener {
		private Control control;

		private ImportFromBUGNetAction action;

		private BUGNetProgramReferenceNode prog;

		private Menu menu;

		MenuListener(Control control, ImportFromBUGNetAction action, BUGNetProgramReferenceNode prog, Menu menu) {
			this.control = control;
			this.action = action;
			this.prog = prog;
			this.menu = menu;
		}

		public void handleEvent(Event event) {
			action.setUserName(prog.getUserName());
			action.setProgramName(prog.getLabel());
			control.setMenu(menu);
		}

	}

	/**
	 * Generate a section.
	 * 
	 * @param parent
	 * @param name
	 * @return
	 */
	public Composite createSection(Composite parent, final String name) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.TREE_NODE | Section.EXPANDED | Section.TWISTIE| Section.COMPACT);
		section.setText(name);
		section.setLayout(new GridLayout(1, false));
		section.setData("id", name); //$NON-NLS-1$

		if(sectionMap.size() != 0){
			if(sectionMap.containsKey(name)){
				int state = ((Integer) sectionMap.get(name)).intValue();
				if(state == 0){
					section.setExpanded(false);
				}
			}
		}

		section.addExpansionListener(new IExpansionListener(){

			public void expansionStateChanged(ExpansionEvent e) {
				if(sectionMap.containsKey(name)){
					sectionMap.remove(name);
					if(e.getState())
						sectionMap.put(name, new Integer(TRUE));
					else
						sectionMap.put(name, new Integer(FALSE));
				}
				else{
					int state = 0;
					if(e.getState())
						state = 1;
					sectionMap.put(name, new Integer(state));
				}
			}

			public void expansionStateChanging(ExpansionEvent e) {
				// TODO Auto-generated method stub

			}

		});

		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(td);

		Composite c = toolkit.createComposite(section, SWT.NONE);
		c.setLayout(new GridLayout());
		section.setClient(c);
		section.setBackground(lightGray2Color);

		return c;
	}


	/**
	 * Saves state of the view's section, more specifically whether it's expanded or not
	 * @throws IOException
	 */
	private void saveState() throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot("sections");
		Iterator iterator = sectionMap.keySet().iterator();
		while(iterator.hasNext()){
			String id = (String)iterator.next();
			int state = ((Integer) sectionMap.get(id)).intValue();

			IMemento section = xmlMemento.createChild("section");
			section.putString("id", id);
			section.putInteger("isExpanded", state);
		}

		Writer writer = new FileWriter(sectionsFileName);
		xmlMemento.save(writer);
	}

	public void setFocus() {
		form.setFocus();
	}

	/**
	 * This code fires when user clicks on a hyperlink in the BUGNet view.
	 * 
	 * @author ken
	 * 
	 */
	private class BUGNetHyperlinkListener implements IHyperlinkListener {

		private final ILinkableModelNode n;

		public BUGNetHyperlinkListener(ILinkableModelNode n) {
			this.n = n;
		}

		public void linkActivated(HyperlinkEvent e) {
			try {
				URL url = null;
				try { // to get the token out
					String token = BugnetWSHelper.getToken();
					if (token != null && token.length() > 0)
						url = URLUtils.addPropertyToURL(n.getUrl().toString(), "token", token);
				} catch (Exception e1) {
					// catch all errors (if we can't get a token, we'll carry on without it)
					e1.printStackTrace();
				}
				
				// couldn't get a url w/ token, just used naked url
				if (url == null)
					url = new URL(n.getUrl());
				
				LaunchBrowserAction action = new LaunchBrowserAction(url, n.getLabel());
				action.run();
			} catch (MalformedURLException e1) {
				UIUtils.handleVisualError("Invalid URL: " + n.getUrl(), e1); //$NON-NLS-1$
			}
		}

		public void linkEntered(HyperlinkEvent e) {
		}

		public void linkExited(HyperlinkEvent e) {
		}
	}

	private class HyperLinkDragSourceListener implements DragSourceListener {

		Hyperlink h;

		public HyperLinkDragSourceListener(Hyperlink h) {
			this.h = h;
		}

		public void dragFinished(DragSourceEvent event) {
			// TODO Auto-generated method stub

		}

		public void dragSetData(DragSourceEvent event) {

			BUGNetProgramReferenceNode prog = null;

			if (PluginTransfer.getInstance().isSupportedType(event.dataType)) {
				if (h.getData() instanceof Map) {
					Map map = (Map) h.getData();
					prog = (BUGNetProgramReferenceNode) map.get("program");
				}

				String name = ""; //$NON-NLS-1$

				if (prog != null) {
					name = prog.getLabel();
				}

				PluginTransferData data = new PluginTransferData(BugNetToIDEActionDelegate.ACTION_ID, BugNETProgramReferenceNodeTransfer
						.getInstance().toByteArray(new BUGNetProgramReferenceNode[] { prog }));

				event.data = data;
			}
		}

		public void dragStart(DragSourceEvent event) {
			event.doit = true;
		}
	}

	private class LoginLinkListener implements IHyperlinkListener {

		public void linkActivated(HyperlinkEvent e) {
			try {
				//was this line: 
				// AuthenticationData data = BUGNetAuthenticationHelper.getAuthenticationData(true);
				// now it's this line:
				BugnetAuthenticationHelper.login();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		public void linkEntered(HyperlinkEvent e) {
			// TODO Auto-generated method stub
		}

		public void linkExited(HyperlinkEvent e) {
			// TODO Auto-generated method stub
		}

	}

	public ScrolledForm getForm() {
		return this.form;
	}

	public void generateLogin(Composite cMyApps) {
		Hyperlink login = toolkit.createHyperlink(cMyApps, Messages.getString("BUGNetView.7"), SWT.NONE); //$NON-NLS-1$
		login.addHyperlinkListener(new LoginLinkListener());
	}

	public void generateNoUserApps(Composite cMyApps) {
		int preferenceNumOfApps = Integer.parseInt(DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_BUGNET_NUM_OF_APPS));
		if(preferenceNumOfApps == 0){
			toolkit.createLabel(cMyApps, "Number of applications in preferences is set to 0");
		}
		else{
		toolkit.createLabel(cMyApps, Messages.getString("BUGNetView.6") + //$NON-NLS-1$
				Messages.getString("BUGNetView.5")); //$NON-NLS-1$
		}
	}

	public void generateNoApps(Composite composite){
		int preferenceNumOfApps = Integer.parseInt(DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_BUGNET_NUM_OF_APPS));
		String msg = "There are no applications on " + DragonflyActivator.getDefault().getPluginPreferences().getString(DragonflyActivator.PREF_SERVER_NAME);
		if(preferenceNumOfApps == 0){
			msg = "Number of applications in preferences is set to 0";
		}
		toolkit.createLabel(composite, msg);
	}

	public void listen() {
		refresh();
	}

	public void propertyChange(final PropertyChangeEvent event) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			public void run() {
				if (event instanceof ModelNodeChangeEvent) {
					ModelNodeChangeEvent changeEvent = (ModelNodeChangeEvent) event;

					if(changeEvent != null){
						Object object = event.getNewValue();
						/*
						 * if focus is gained update the BUGnet view's application
						 * by modules section, if focus is lost, remove the section.
						 */
						if (changeEvent.getPropertyName() != null && changeEvent.getPropertyName().equals(
								PhysicalEditor.EDITOR_FOCUS_GAINED)) {
							if (object instanceof BugConnection) {
								IWorkbenchPage activePage = PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage();
								IEditorPart activeEditor = activePage
								.getActiveEditor(); // get current active
								// editor

								if (activeEditor instanceof PhysicalEditor) {
									if (lastPhysicalEditor == null) {
										update(object);
									} else if (!lastPhysicalEditor.getBug()
											.getUrl().equals(
													((PhysicalEditor) activeEditor)
													.getBug().getUrl())) {
										update(object);
									} else if (!dataExistsForModuleSet((BugConnection) object)) {
										update(object);
									}
									lastPhysicalEditor = (PhysicalEditor) activeEditor; // get
									// current
									// active
									// editor
								}
							}
						} else if (changeEvent.getPropertyName() != null && changeEvent.getPropertyName().equals(
								PhysicalEditor.EDITOR_FOCUS_LOST)) {
							IWorkbenchPage activePage = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
							IEditorPart activeEditor = activePage.getActiveEditor(); // get
							// current
							// active
							// editor
							if (!(activeEditor instanceof PhysicalEditor)) {
								removeModuleSection(APPLICATION_MODULE_SECTION);
								form.reflow(true);
								form.redraw();
							}
						}
						else if ( changeEvent.getPropertyName() != null && changeEvent.getPropertyName().equals(PhysicalEditor.REFRESH)){
							update(object);
						}
					}
				}
			}

			private boolean dataExistsForModuleSet(BugConnection bugConnection) {
				try {
					URL url = bugConnection.getUrl();
					List currentModules = BugWSHelper
					.getRawModules(bugConnection.getModuleURL());
					List savedModules = (List) moduleTrackerMap.get(url);

					// there are not saved modules or size of new and saved
					// module lists differs, perform update
					if (savedModules == null || currentModules.size() != savedModules.size()) {
						return false;
					}

					if (!compareLists(currentModules, savedModules)) {
						return false;
					}
				} catch (IOException e) {
					return false;
				}
				return true;
			}

			private boolean compareLists(List currentModules, List savedModules) {
				for (Iterator it = currentModules.iterator(); it.hasNext();) {
					if (!savedModules.contains(it.next())) {
						return false;
					}

				}
				return true;
			}

			private synchronized void update(Object object) {
				Job job = new UpdateModulesAppSectionJob("Updating BUGnet view",(BugConnection)object);
				job.schedule();
			}

		});
	}

	private class UpdateModulesAppSectionJob extends Job{
		private BugConnection bugConnection;
		public UpdateModulesAppSectionJob(String name, BugConnection bugConnection) {
			super(name);
			this.bugConnection = bugConnection;
		}

		protected IStatus run(IProgressMonitor monitor) {
			Job getData = new GetDataJob("Updating Applications by Modules", bugConnection);
			getData.schedule();

			getData.addJobChangeListener(new IJobChangeListener(){

				public void aboutToRun(
						IJobChangeEvent event) {
					// TODO Auto-generated method stub

				}

				public void awake(IJobChangeEvent event) {
					// TODO Auto-generated method stub

				}

				public void done(IJobChangeEvent event) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){

						public void run() {
							removeModuleSection(APPLICATION_MODULE_SECTION);
							URL url = bugConnection.getUrl();

							List modules;
							try {
								modules = BugWSHelper.getRawModules(bugConnection.getModuleURL());
								moduleTrackerMap.put(url, modules);

								Composite comp = createSection(form.getBody(), APPLICATION_MODULE_SECTION + " (" + programsForPackages.size() + ")"); //$NON-NLS-1$
								generateDetail(comp, programsForPackages);
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							finally{
								form.reflow(true);
								form.redraw();
							}
						}

					});
				}

				public void running(
						IJobChangeEvent event) {
					// TODO Auto-generated method stub

				}

				public void scheduled(
						IJobChangeEvent event) {
					// TODO Auto-generated method stub

				}

				public void sleeping(
						IJobChangeEvent event) {
					// TODO Auto-generated method stub

				}

			});
			return Status.OK_STATUS;
		}

	}

	private class GetDataJob extends Job{

		private BugConnection bugConnection;

		public GetDataJob(String name, BugConnection bugConnection) {
			super(name);
			this.bugConnection = bugConnection;
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				List packageList = BugWSHelper.getPackages(bugConnection.getPackageURL());
				programsForPackages = BugnetWSHelper.getProgramsForPackages(packageList);
			} catch (BugnetAuthenticationException e) {
				UIUtils.handleNonvisualWarning("Unable to authenticate user", e);
			}  catch (IOException e) {
				UIUtils.handleNonvisualWarning("Unable to retrieve packages", e);
			}
			return Status.OK_STATUS;
		}

	}

	/**
	 * Removes section from the view
	 * @param sectionName Name of section to be removed
	 */
	public void removeModuleSection(String sectionName) {
		Control[] sections = getForm().getBody().getChildren();
		for(int i = 0; i < sections.length; i++){
			String id = (String) sections[i].getData("id"); //$NON-NLS-1$
			if(id.indexOf(sectionName) != -1){
				sections[i].dispose();
			}
		}
	}
}
