package com.buglabs.dragonfly.ui.views.mybugs;

import java.io.IOException;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.ui.editor.actions.CollapseAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.IBUGnetAuthenticationListener;
import com.buglabs.dragonfly.model.BaseTreeNode;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.actions.BugAddConnectionAction;
import com.buglabs.dragonfly.ui.actions.BugDeleteConnectionAction;
import com.buglabs.dragonfly.ui.actions.MyBugRefreshAction;
import com.buglabs.dragonfly.ui.dnd.MyBugsViewProgramNodeTransfer;
import com.buglabs.dragonfly.ui.dnd.ProgramToViewDropAdapter;
import com.buglabs.dragonfly.ui.editors.PhysicalEditor;
import com.buglabs.dragonfly.ui.filters.ApplicationFilter;
import com.buglabs.dragonfly.ui.jobs.LaunchPhysicalEditorJob;
import com.buglabs.dragonfly.ui.providers.BugLabelProvider;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * View that contains bug connections
 * 
 * @author akravets
 * 
 */
public class MyBugsView extends ViewPart implements ISelectionProvider, IBUGnetAuthenticationListener {

	public static final String ID = "com.buglabs.dragonfly.ui.MyBugsView"; //$NON-NLS-1$

	private static TreeViewer viewer;

	private BugAddConnectionAction addConectionAction;

	private BugDeleteConnectionAction deleteConnectionAction;

	private static MyBugRefreshAction refreshBugAction;

	private static BaseTreeNode root; // root node that contains
										// BugProjectNode nodes

	private ProgramToViewDropAdapter dropAdapter;

	public static final String BUGS_TYPE = "bugs"; //$NON-NLS-1$

	public static final String BUG_TYPE = "bug"; //$NON-NLS-1$

	public static final String BUG_NAME = "name"; //$NON-NLS-1$

	public static final String BUG_URL = "url"; //$NON-NLS-1$

	/**
	 * Initializes this view with the given view site. A memento is passed to
	 * the view which contains a snapshot of the views state from a previous
	 * session.
	 */
	public void init() {
		root = (BaseTreeNode) Activator.getDefault().getBugsViewRoot();		
	}

	public void init(IViewSite site) throws PartInitException {
		setSite(site);
		root = (BaseTreeNode) Activator.getDefault().getBugsViewRoot();
		DragonflyActivator.getDefault().addBUGnetAuthenticationListener(this);
	}

	public void dispose() {
		super.dispose();

		try {
			Activator.getDefault().saveBugs();
			viewer = null;
		} catch (IOException e) {
			UIUtils.handleNonvisualError("Unable to save BUGs", e);
		}
	}

	public void createPartControl(Composite parent) {
		// init();
		viewer = new TreeViewer(parent, SWT.MULTI);
		viewer.setContentProvider(new MyBugsViewContentProvider());
		viewer.setLabelProvider(new BugLabelProvider());
		viewer.addFilter(new ApplicationFilter());
		viewer.setComparator(new MyBugsViewComparator());		

		addDropSupport();
		addDragSupport();

		createActions();
		createContextMenu();
		createToolBar();

		getSite().setSelectionProvider(viewer);

		// open com.buglabs.dragonfly.ui.physicalEditor
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(final DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				final Object selectedNode = ((IStructuredSelection) selection).getFirstElement();
				if (selectedNode instanceof BugConnection) {
					final BugConnection bug = (BugConnection) selectedNode;
					if (bug.isConnected()) {
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
							public void run() {
								Job job = new LaunchPhysicalEditorJob(bug);
								job.setPriority(Job.SHORT);
								job.schedule();
							}
						});
					}
				}
			}

		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(final SelectionChangedEvent selection) {
				deleteConnectionAction.setEnabled(false);
				ISelection selected = selection.getSelection();
				if (selected instanceof IStructuredSelection) {
					Object firstElement = ((IStructuredSelection) selected).getFirstElement();
					if (firstElement instanceof StaticBugConnection) {
						deleteConnectionAction.setEnabled(true);
					} else {
						deleteConnectionAction.setEnabled(false);
					}
				}
			}

		});

		// attached key listener to viewer
		viewer.getTree().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				char ch = e.character;
				// on delete execute delete action
				if (ch == SWT.DEL) {
					Object selection = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
					if (selection instanceof StaticBugConnection) {
						deleteConnectionAction.run();
					}
				}
			}

		});

		viewer.setInput(root);
	}

	/*
	 * defines drop support into bugs view
	 */
	private void addDropSupport() {
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MyBugsViewProgramNodeTransfer.getInstance(), ResourceTransfer.getInstance() };
		dropAdapter = new ProgramToViewDropAdapter(viewer);
		viewer.addDropSupport(operations, transfers, dropAdapter);
	}

	/*
	 * defines drag support from bugs view
	 */
	private void addDragSupport() {
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { MyBugsViewProgramNodeTransfer.getInstance(), PluginTransfer.getInstance() };
		viewer.addDragSupport(ops, transfers, new MyBugsDragSourceListener(viewer));
	}

	private void createActions() {
		addConectionAction = new BugAddConnectionAction();

		deleteConnectionAction = new BugDeleteConnectionAction(viewer);
		deleteConnectionAction.setEnabled(false);

		refreshBugAction = new MyBugRefreshAction(viewer);
		refreshBugAction.setEnabled(false);
	}

	private void createToolBar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
		manager.add(addConectionAction);
		manager.add(new CollapseAction(viewer,"Collapse All")); // collapses all nodes in the view
	}

	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		getSite().registerContextMenu(menuManager, viewer);

	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(addConectionAction);
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(deleteConnectionAction);

		IEditorPart activeEditor = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		refreshBugAction.setEnabled(false);

		// enable refresh only if editor PhysicalEditor is present
		if (activeEditor != null && activeEditor instanceof PhysicalEditor) {
			refreshBugAction.setEnabled(true);
		}
		manager.add(refreshBugAction);
		manager.add(new Separator());

	}

	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		// TODO Auto-generated method stub

	}

	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		// TODO Auto-generated method stub

	}

	public void setSelection(ISelection selection) {
		// TODO Auto-generated method stub

	}

	public static ITreeNode getRoot() {
		return root;
	}

	public static TreeViewer getViewer() {
		return viewer;
	}

	public void listen() {
		// TODO Auto-generated method stub

	}
}
