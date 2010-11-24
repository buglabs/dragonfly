package com.buglabs.dragonfly.ui.editors;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.DelegatingLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RelativeLocator;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BaseTreeNode;
import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.BugProperty;
import com.buglabs.dragonfly.model.IModelChangeListener;
import com.buglabs.dragonfly.model.Module;
import com.buglabs.dragonfly.simulator.controller.Client;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.borders.RoundedLineBorder;
import com.buglabs.dragonfly.ui.draw2d.BugSelectableFigure;
import com.buglabs.dragonfly.ui.draw2d.CenterLocator;
import com.buglabs.dragonfly.ui.draw2d.XYFigureAnchor;
import com.buglabs.dragonfly.ui.views.mybugs.BugLabelProvider;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * 
 * Draws the Bug view
 * 
 * @author Brian
 * 
 */
public class PhysicalEditor extends EditorPart implements IModelChangeListener, ISelectionListener {

	private static final int BUG_SIMULATOR_CONTROLLER_PORT = 8093;

	public static final String ID = "com.buglabs.dragonfly.ui.physicalEditor";

	public static final String LAYER_KEY_GRID_LAYER = "LAYER_KEY_GRID_LAYER";

	public static final String LAYER_KEY_BASE_UNIT = "LAYER_KEY_BASE_UNIT";

	public static final String LAYER_KEY_SCRATCH = "LAYER_KEY_SCRATCH";

	public static final String LAYER_KEY_BOTTOM = "LAYER_KEY_BOTTOM";

	public static final String LAYER_KEY_TOP = "LAYER_KEY_TOP";

	public static final String LAYER_KEY_LABEL = "LAYER_KEY_LABEL";

	public static final String REFRESH_FAMILY = "Refresh";

	RoundedLineBorder roundBorder = new RoundedLineBorder(ColorConstants.darkGray, 2, 4);

	private Composite comp;

	private FigureCanvas fc;

	private List moduleList;

	private Bug bug;

	private Map moduleToFigure = new HashMap();

	ImageRegistry imageRegistry;

	private BugSelectableFigure baseUnitFigure;

	public static final String EDITOR_FOCUS_GAINED = "FOCUS_GAINED";

	public static final String EDITOR_FOCUS_LOST = "FOCUS_LOST";

	public static final String MODULES_CHANGED = "MODULES_CHANGED";

	public static final String REFRESH = "REFRESH";

	/**
	 * Slot ID on BUG20 that is video only.
	 */
	private static final int VIDEO_SLOT = 1;

	private Client controllerClient;

	private List menuActions;

	public PhysicalEditor(Bug bug) {
		this.bug = bug;
	}

	public PhysicalEditor() {
		menuActions = new ArrayList();
	}

	public void dispose() {
		DragonflyActivator.getDefault().removeListener(this);
		super.dispose();
	}

	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		getSite().setSelectionProvider(CanvasSelectionProvider.getDefault());
		getSite().getPage().addPostSelectionListener(this);
		if (input instanceof BugEditorInput) {
			this.bug = ((BugEditorInput) input).getBug();
			this.moduleList = ((BugEditorInput) input).getModules();
		}

		if (bug != null) {
			this.setPartName(bug.getName());
		}

		if (isSimulatedBUG(bug)) {
			try {
				controllerClient = Client.getClient(bug.getUrl().getHost(), BUG_SIMULATOR_CONTROLLER_PORT);
			} catch (IOException e) {
				UIUtils.handleNonvisualError("Unable to initialize BUG Simulator.", e);
			}
		}

		DragonflyActivator.getDefault().addListener(this);
		imageRegistry = Activator.getDefault().getImageRegistry();

	}

	public boolean isDirty() {
		//Read-only editor.
		return false;
	}

	public boolean isSaveAsAllowed() {
		//Read-only editor.
		return false;
	}

	public void createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new FillLayout());

		fc = new FigureCanvas(top, SWT.NONE);
		fc.setBackground(ColorConstants.white);
		fc.setLayout(new FillLayout());
		hookContextMenu(top);
		fc.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				drawBugControl();
			}
		});
	}

	private void hookContextMenu(Composite parent) {
		MenuManager menuMgr = new MenuManager();
		Separator additions = new Separator(IWorkbenchActionConstants.MB_ADDITIONS);
		menuMgr.removeAll();
		menuMgr.add(additions);

		if (isSimulatedBUG(bug)) {
			final MenuManager subMenu = new MenuManager("BUG Simulator");
			try {
				List availableModules = controllerClient.getAvailableModuleNames();

				for (int i = 0; i < 4; ++i) {
					MenuManager mm = new MenuManager("Slot " + i);
					createModuleMenu(mm, i, availableModules);
					subMenu.add(mm);
				}

				menuMgr.add(subMenu);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Menu menu = menuMgr.createContextMenu(parent);

		fc.setMenu(menu);

		getSite().registerContextMenu(menuMgr, CanvasSelectionProvider.getDefault());
	}

	private void createModuleMenu(MenuManager mm, int slot, List availableModules) {

		availableModules = filterAvailableModules(availableModules, slot);

		for (Iterator i = availableModules.iterator(); i.hasNext();) {
			ModuleMenuAction mma = new ModuleMenuAction((String) i.next(), slot);
			mm.add(mma);
			menuActions.add(mma);
		}
		mm.add(new EmptyModuleMenuAction(slot));
	}

	/**
	 * Filter modules available for a given slot. On BUG20 device Video, LCD
	 * only on slot 1, and all others on 0, 2-3 slots.
	 * 
	 * @param availableModules
	 * @param slot
	 * @return List of modules available for a give slot.
	 */
	private List filterAvailableModules(List availableModules, int slot) {

		List nl = new ArrayList();

		for (Iterator i = availableModules.iterator(); i.hasNext();) {
			String modName = (String) i.next();

			if (slot == VIDEO_SLOT) {
				if (isVideoModule(modName)) {
					nl.add(modName);
				}
			} else {
				if (!isVideoModule(modName)) {
					nl.add(modName);
				}
			}
		}

		return nl;
	}

	/**
	 * @param modName
	 * @return true if a given module id (name) is for the video module slot.
	 */
	private boolean isVideoModule(String modName) {
		return modName.toUpperCase().equals("LCD") || modName.toUpperCase().equals("VIDEO");
	}

	/**
	 * @param b
	 * @return true if this editor is connected to a BUG Simulator
	 */
	private boolean isSimulatedBUG(Bug b) {
		if (b == null) {
			return false;
		}

		return b.getUrl().getHost().equalsIgnoreCase("localhost") || b.getUrl().getHost().contains("127.0.0.1");
	}

	/**
	 * Draws the bug on the screen called in creation and model change events
	 * 
	 */
	private void drawBugControl() {
		BugLabelProvider labelprovider = new BugLabelProvider();

		/*
		 * ------------------------- ------------------------- | MODULE 1 | |
		 * MODULE 0 | ------------------------- -------------------------
		 * --------------------------------------------------- | | | BUG BASE | | |
		 * ---------------------------------------------------
		 * ------------------------- ------------------------- | MODULE 3 | |
		 * MODULE 2 | ------------------------- -------------------------
		 * 
		 */

		// Populate model
		Module[] modules = new Module[4];
		Module m;
		BugProperty slot;
		int n;
		for (Iterator i = menuActions.iterator(); i.hasNext();) {
			((ModuleMenuAction) i.next()).setState(false);
		}

		for (Iterator i = moduleList.iterator(); i.hasNext();) {
			m = (Module) i.next();

			if (m != null) {
				Map properties = m.getProperties();
				if (properties != null) {
					slot = (BugProperty) properties.get("Slot"); //$NON-NLS-1$
					if (slot != null) {
						n = Integer.parseInt(slot.getValue());

						if (n >= 0 && n < 4) {
							modules[n] = m;
							setSlotUsed(n);
						}
					}
				}
			}
		}

		// Draw the content
		ScalableFreeformLayeredPane pane = new ScalableFreeformLayeredPane();
		pane.setLayoutManager(new StackLayout());
		GridLayer gridLayer = new GridLayer();

		gridLayer.setForegroundColor(ColorConstants.lightGray);
		Layer scratchLayer = createLayer();
		Layer bottomLayer = createLayer();
		Layer baseUnitLayer = createLayer();
		Layer topLayer = createLayer();
		Layer labelLayer = createLayer();

		pane.add(scratchLayer, 0);
		pane.add(gridLayer, LAYER_KEY_GRID_LAYER, 1);
		pane.add(bottomLayer, LAYER_KEY_BOTTOM, 2);
		pane.add(baseUnitLayer, LAYER_KEY_BASE_UNIT, 3);
		pane.add(topLayer, LAYER_KEY_TOP, 4);
		pane.add(labelLayer, LAYER_KEY_LABEL, 5);

		Figure centerFigure = new Figure();

		baseUnitFigure = new BugSelectableFigure("Base");
		if (isBUGConnected()) {
			baseUnitFigure.setImage(imageRegistry.get(Activator.IMAGE_KEY_BASE_UNIT));
			baseUnitFigure.setSelectedImage(imageRegistry.get(Activator.IMAGE_KEY_BASE_UNIT_SELECTED));
		} else {
			baseUnitFigure.setImage(imageRegistry.get(Activator.IMAGE_KEY_BASE_UNIT_DISCONNECTED));
			baseUnitFigure.setSelectedImage(imageRegistry.get(Activator.IMAGE_KEY_BASE_UNIT_DISCONNECTED));
		}
		baseUnitFigure.setModel(bug);
		centerFigure.setSize(baseUnitFigure.getPreferredSize());
		scratchLayer.add(centerFigure, new CenterLocator(scratchLayer));
		baseUnitLayer.add(baseUnitFigure, new CenterLocator(centerFigure));

		moduleToFigure.put(bug, baseUnitFigure);

		if (modules[0] != null) {
			final BugSelectableFigure moduleSlot0Figure = new BugSelectableFigure("Slot0");
			moduleSlot0Figure.setImage(imageRegistry.get(Activator.IMAGE_KEY_MODULE_SLOT_0));
			moduleSlot0Figure.setSelectedImage(imageRegistry.get(Activator.IMAGE_KEY_MODULE_SLOT_0_SELECTED));
			moduleSlot0Figure.setModel(modules[0]);
			topLayer.add(moduleSlot0Figure, new RelativeLocator(baseUnitFigure, 0.326, 0.190));
			moduleToFigure.put(modules[0], moduleSlot0Figure);
			moduleSlot0Figure.setName("Slot 0");
			Label lblFigure = new Label(labelprovider.getText(modules[0]));

			lblFigure.setBorder(roundBorder);
			lblFigure.setBackgroundColor(ColorConstants.white);
			lblFigure.setOpaque(true);
			lblFigure.setIcon(labelprovider.getImage(modules[0]));
			lblFigure.addMouseListener(new MouseListener() {
				public void mousePressed(MouseEvent me) {
					if (me.button == 1) {
						CanvasSelectionProvider.getDefault().setSelection(new StructuredSelection(moduleSlot0Figure));
					}
				}

				public void mouseReleased(MouseEvent me) {
				}

				public void mouseDoubleClicked(MouseEvent me) {
				}
			});
			lblFigure.setIconTextGap(5);
			labelLayer.add(lblFigure, new RelativeLocator(moduleSlot0Figure, 0.1, -0.3));

			PolylineConnection connection = new PolylineConnection();
			connection.setLineWidth(2);
			connection.setLineStyle(Graphics.LINE_SOLID);
			connection.setTargetDecoration(new PolygonDecoration());
			connection.setConnectionRouter(new ManhattanConnectionRouter());

			connection.setForegroundColor(ColorConstants.darkGray);
			connection.setSourceAnchor(new BorderAnchor(lblFigure, PositionConstants.EAST));
			connection.setTargetAnchor(new XYFigureAnchor(moduleSlot0Figure, new Point(0, -20)));
			labelLayer.add(connection);
		}

		if (modules[1] != null) {
			final BugSelectableFigure moduleSlot1Figure = new BugSelectableFigure("Slot1");
			moduleSlot1Figure.setImage(imageRegistry.get(Activator.IMAGE_KEY_MODULE_SLOT_1));
			moduleSlot1Figure.setSelectedImage(imageRegistry.get(Activator.IMAGE_KEY_MODULE_SLOT_1_SELECTED));
			moduleSlot1Figure.setModel(modules[1]);

			topLayer.add(moduleSlot1Figure, new RelativeLocator(baseUnitFigure, 0.670, 0.444));
			moduleToFigure.put(modules[1], moduleSlot1Figure);

			Label lblFigure = new Label(labelprovider.getText(modules[1]));
			labelLayer.add(lblFigure, new RelativeLocator(moduleSlot1Figure, .9, -0.3));

			lblFigure.setBorder(roundBorder);
			lblFigure.setBackgroundColor(ColorConstants.white);
			lblFigure.setOpaque(true);
			lblFigure.setIcon(labelprovider.getImage(modules[1]));
			lblFigure.addMouseListener(new MouseListener() {
				public void mousePressed(MouseEvent me) {
					if (me.button == 1) {
						CanvasSelectionProvider.getDefault().setSelection(new StructuredSelection(moduleSlot1Figure));
					}
				}

				public void mouseReleased(MouseEvent me) {
				}

				public void mouseDoubleClicked(MouseEvent me) {
				}
			});

			PolylineConnection connection = new PolylineConnection();
			connection.setLineWidth(2);
			connection.setLineStyle(Graphics.LINE_SOLID);
			connection.setTargetDecoration(new PolygonDecoration());
			connection.setConnectionRouter(new ManhattanConnectionRouter());

			connection.setForegroundColor(ColorConstants.darkGray);
			connection.setSourceAnchor(new BorderAnchor(lblFigure, PositionConstants.WEST));
			connection.setTargetAnchor(new XYFigureAnchor(moduleSlot1Figure, new Point(0, -20)));
			topLayer.add(connection);
		}

		if (modules[2] != null) {
			final BugSelectableFigure moduleSlot2Figure = new BugSelectableFigure("Slot2");
			moduleSlot2Figure.setImage(imageRegistry.get(Activator.IMAGE_KEY_MODULE_SLOT_2));
			moduleSlot2Figure.setSelectedImage(imageRegistry.get(Activator.IMAGE_KEY_MODULE_SLOT_2_SELECTED));
			moduleSlot2Figure.setModel(modules[2]);
			bottomLayer.add(moduleSlot2Figure, new RelativeLocator(centerFigure, 0.329, .59));
			moduleToFigure.put(modules[2], moduleSlot2Figure);
			Label lblFigure = new Label(labelprovider.getText(modules[2]));
			labelLayer.add(lblFigure, new RelativeLocator(moduleSlot2Figure, 0.1, 1.3));
			lblFigure.setBorder(roundBorder);
			lblFigure.setBackgroundColor(ColorConstants.white);
			lblFigure.setOpaque(true);
			lblFigure.setIcon(labelprovider.getImage(modules[2]));
			lblFigure.addMouseListener(new MouseListener() {
				public void mousePressed(MouseEvent me) {
					if (me.button == 1) {
						CanvasSelectionProvider.getDefault().setSelection(new StructuredSelection(moduleSlot2Figure));
					}
				}

				public void mouseReleased(MouseEvent me) {
				}

				public void mouseDoubleClicked(MouseEvent me) {
				}
			});

			PolylineConnection connection = new PolylineConnection();
			connection.setLineWidth(2);
			connection.setLineStyle(Graphics.LINE_SOLID);
			connection.setTargetDecoration(new PolygonDecoration());
			connection.setConnectionRouter(new ManhattanConnectionRouter());

			connection.setForegroundColor(ColorConstants.darkGray);
			connection.setSourceAnchor(new BorderAnchor(lblFigure, PositionConstants.EAST));
			connection.setTargetAnchor(new XYFigureAnchor(moduleSlot2Figure, new Point(-80, 35)));
			topLayer.add(connection);
		}

		if (modules[3] != null) {
			final BugSelectableFigure moduleSlot3Figure = new BugSelectableFigure("Slot3");
			moduleSlot3Figure.setImage(imageRegistry.get(Activator.IMAGE_KEY_MODULE_SLOT_3));
			moduleSlot3Figure.setSelectedImage(imageRegistry.get(Activator.IMAGE_KEY_MODULE_SLOT_3_SELECTED));
			moduleSlot3Figure.setModel(modules[3]);
			bottomLayer.add(moduleSlot3Figure, new RelativeLocator(centerFigure, 0.670, 0.849));
			moduleToFigure.put(modules[3], moduleSlot3Figure);

			Label lblFigure = new Label(labelprovider.getText(modules[3]));
			labelLayer.add(lblFigure, new RelativeLocator(moduleSlot3Figure, .9, 1.2));
			lblFigure.setBorder(roundBorder);
			lblFigure.setBackgroundColor(ColorConstants.white);
			lblFigure.setOpaque(true);
			lblFigure.setIcon(labelprovider.getImage(modules[3]));
			lblFigure.addMouseListener(new MouseListener() {
				public void mousePressed(MouseEvent me) {
					if (me.button == 1) {
						CanvasSelectionProvider.getDefault().setSelection(new StructuredSelection(moduleSlot3Figure));
					}
				}

				public void mouseReleased(MouseEvent me) {
				}

				public void mouseDoubleClicked(MouseEvent me) {
				}
			});

			PolylineConnection connection = new PolylineConnection();
			connection.setLineWidth(2);
			connection.setLineStyle(Graphics.LINE_SOLID);
			connection.setTargetDecoration(new PolygonDecoration());
			connection.setConnectionRouter(new ManhattanConnectionRouter());

			connection.setForegroundColor(ColorConstants.darkGray);
			connection.setSourceAnchor(new BorderAnchor(lblFigure, PositionConstants.WEST));
			connection.setTargetAnchor(new XYFigureAnchor(moduleSlot3Figure, new Point(-25, 60)));
			topLayer.add(connection);
		}

		fc.setContents(pane);
		pane.validate();
	}

	/**
	 * For the actions that work against the virtual bug, set a given slot as
	 * having a module attached.
	 * 
	 * @param n
	 */
	private void setSlotUsed(int n) {
		for (Iterator i = menuActions.iterator(); i.hasNext();) {
			ModuleMenuAction mma = ((ModuleMenuAction) i.next());
			if (mma.getSlot() == n) {
				mma.setState(true);
			}
		}
	}

	private Layer createLayer() {
		Layer layer = new FreeformLayer();
		layer.setLayoutManager(new DelegatingLayout());
		layer.setBackgroundColor(ColorConstants.white);
		return layer;
	}

	public void setFocus() {
		fc.setFocus();
	}

	public void propertyChange(PropertyChangeEvent event) {
		URL editorBug = null, eventBug = null;
		if (event != null) {
			String property = event.getPropertyName();
			if (property != null) {
				if (property.equals(BugConnectionManager.REMOVE_BUG)) {
					editorBug = bug.getUrl();
					eventBug = ((BugConnection) event.getNewValue()).getUrl();
					// update physical editor of the BUG that was disconnected
					if (editorBug.toString().equals(eventBug.toString())) {
						bug.setConnected(false);
						moduleList.clear();
						redrawEditor();
					}
				} else if (property.equals(BugConnectionManager.ADD_BUG)) {
					editorBug = bug.getUrl();
					eventBug = ((BugConnection) event.getNewValue()).getUrl();
					if (editorBug.toString().equals(eventBug.toString())) {
						GetModulesJob job = new GetModulesJob("Connecting to " + bug.getUrl());
						job.schedule();
					}
				} else if (property.equals(BugConnectionManager.REFRESH_BUG)) {
					if (bug != null) {
						editorBug = bug.getUrl();
						eventBug = ((BugConnection) event.getNewValue()).getUrl();
						if (editorBug.toString().equals(eventBug.toString())) {
							if (bug.isConnected()) {
								refreshModules();
								if (isSimulatedBUG(bug)) {
									try {
										controllerClient = Client.getClient(bug.getUrl().getHost(), BUG_SIMULATOR_CONTROLLER_PORT);
									} catch (IOException e) {
										UIUtils.handleNonvisualError("Unable to initialize BUG Simulator.", e);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void refreshModules() {
		moduleList.clear();
		try {
			moduleList.addAll(BugWSHelper.getModuleList(null, bug.getModuleURL()));
			bug.setConnected(true);
		} catch (Exception e) {
			bug.setConnected(false);
			UIUtils.handleNonvisualWarning("Unable to connect to " + bug.getUrl(), e, true);
		} finally {
			redrawEditor();
		}
	}

	/**
	 * A job that calls refreshModules()
	 * 
	 * @author akravets
	 * 
	 */
	private class GetModulesJob extends Job {

		public GetModulesJob(String name) {
			super(name);
		}

		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Connecting to BUG", IProgressMonitor.UNKNOWN);
			refreshModules();
			monitor.done();
			return Status.OK_STATUS;
		}
	}

	private void redrawEditor() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				drawBugControl();
			}
		});
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();

			if (obj instanceof BaseTreeNode) {
				BaseTreeNode mod = (BaseTreeNode) obj;
				if (moduleToFigure != null) {
					clearAllFigureSelections();
					BugSelectableFigure fig = (BugSelectableFigure) moduleToFigure.get(mod);
					if (fig != null) {
						fig.setFocus(true);
						fig.repaint();
					}
				}
			}
		}
	}

	private void clearAllFigureSelections() {
		Iterator keys = moduleToFigure.keySet().iterator();
		while (keys.hasNext()) {
			BugSelectableFigure fig = (BugSelectableFigure) moduleToFigure.get(keys.next());
			if (fig != null) {
				fig.setFocus(false);
				fig.repaint();
			}
		}
	}

	public void refresh() {
		try {
			BugWSHelper.subscribeToBug(bug);
			refreshModules();
		} catch (Exception e) {
			UIUtils.handleVisualError("Unable to refresh " + bug.getName() + "(" + bug.getUrl() + "). Please check error log for further information", e);
		}
	}

	/**
	 * @return Returns <code>true</code> if {@link BugConnection} associated
	 *         with the editor is connected, <code>false</code> otherwise.
	 */
	public boolean isBUGConnected() {
		return bug.isConnected();
	}

	/**
	 * @return Returns {@link Bug} associated with this editor
	 */
	public Bug getBug() {
		return bug;
	}

	private class ModuleMenuAction extends Action {
		private final int slot;
		private boolean moduleSet;

		public ModuleMenuAction(String name, int slot) {
			super(name);
			this.moduleSet = false;
			this.slot = slot;
		}

		public void setState(boolean value) {
			moduleSet = value;
		}

		public int getSlot() {
			return slot;
		}

		@Override
		public void run() {
			Job j = new Job("Set Slot") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						if (ModuleMenuAction.this.moduleSet) {
							if (!controllerClient.RemoveModuleFromSlot(slot)) {
								ModuleMenuAction.this.moduleSet = false;
								UIUtils.handleVisualError("Problem occured while connecting to BUG Simulator.", new Exception("RemoveModuleFromSlot() returned false."));
								return Status.CANCEL_STATUS;
							}
							moduleSet = false;
						} else {
							if (!controllerClient.AttachModule(getText(), slot)) {
								ModuleMenuAction.this.moduleSet = true;
								UIUtils.handleVisualError("Problem occured while connecting to BUG Simulator.", new Exception("AttachModule() returned false."));
								return Status.CANCEL_STATUS;
							}
							moduleSet = true;
						}
					} catch (IOException e) {
						e.printStackTrace();
						return Status.CANCEL_STATUS;
					}
					
					return Status.OK_STATUS;
				}
				
			};
			j.schedule();
		}
	}

	private class EmptyModuleMenuAction extends Action {
		private final int slot;

		public EmptyModuleMenuAction(int slot) {
			super("<Empty>");
			this.slot = slot;
		}

		@Override
		public void run() {
			Job j = new Job("Clear Module") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						controllerClient.RemoveModuleFromSlot(slot);
					} catch (IOException e) {
						e.printStackTrace();
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}

			};
			j.schedule();
		}
	}
}
