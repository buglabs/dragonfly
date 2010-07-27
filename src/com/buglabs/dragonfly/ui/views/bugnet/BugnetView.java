package com.buglabs.dragonfly.ui.views.bugnet;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

import com.buglabs.dragonfly.BugConnectionManager;
import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.IBugnetAuthenticationListener;
import com.buglabs.dragonfly.bugnet.BugnetApplicationCategoryHelper;
import com.buglabs.dragonfly.bugnet.BugnetResultManager;
import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.IModelChangeListener;
import com.buglabs.dragonfly.model.IModelNode;
import com.buglabs.dragonfly.ui.BugnetAuthenticationHelper;
import com.buglabs.dragonfly.ui.actions.ExportJarToBUGNetAction;
import com.buglabs.dragonfly.ui.actions.RefreshBugNetViewAction;
import com.buglabs.dragonfly.ui.actions.SearchBugNetAction;
import com.buglabs.dragonfly.ui.jobs.BUGNetRefreshJob;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;

/**
 * The BugnetView, not to be confused with the old BUGnetView This one uses the
 * BugnetViewer and an MVC approach to draw a view containing a list of bug
 * applications
 * 
 * @author brian
 * 
 */
public class BugnetView extends ViewPart implements IModelChangeListener, IBugnetAuthenticationListener {
	private static final int DEFAULT_CATEGORY_INDEX = 0;
	public static final String VIEW_ID = "com.buglabs.dragonfly.ui.views.bugnet.BugnetView"; //$NON-NLS-1$

	private static ColorRegistry colorRegistry;
	private static final String BACKGROUNDCOLOR = "BACKGROUNDCOLOR";
	private static final String FOREGROUNDCOLOR = "FOREGROUNDCOLOR";

	private BugnetApplicationCategoryHelper appCategoryHelper;
	private Composite top;
	private Composite loginArea;
	private Composite filterArea;
	private Composite bottomNav;
	private Combo combo;
	private Text searchText;
	private Hyperlink moreLink;
	private Label bottomLabel;
	private BugnetViewer bugnetViewer;
	private BugnetApplicationList applicationList;
	private ScrolledForm form;
	private FormToolkit toolkit;

	/**
	 * called from the framework This is where the view gets drawn
	 */
	public void createPartControl(Composite parent) {
		// initialize composite for drawing
		top = new Composite(parent, SWT.None);
		initializeColors();
		top.setBackground(colorRegistry.get(BACKGROUNDCOLOR));
		GridLayout topLayout = new GridLayout(1, true);
		topLayout.verticalSpacing = topLayout.marginHeight = 0;
		topLayout.marginWidth = 2;
		top.setLayout(topLayout);
		toolkit = new FormToolkit(top.getDisplay());

		// More initial setup
		appCategoryHelper = new BugnetApplicationCategoryHelper();
		applicationList = new BugnetApplicationList();

		// layout the parts of the view
		// login area is where it displays a login link or says "logged in as soandso"
		drawLoginArea();
		// filterArea contains both search box and category combo
		drawFilterArea();
		// initialize the viewer
		createBugnetViewer();
		// Set up this view as a drop target for uploading apps
		setupDropTarget();
		// run job to query and draw
		queryBugnetAndDrawApplications();
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.getActionBars().getToolBarManager().add(new RefreshBugNetViewAction(this));
		site.getActionBars().getToolBarManager().add(new SearchBugNetAction());
		DragonflyActivator.getDefault().addListener(this);
		BugnetAuthenticationHelper.addBugnetAuthenticationListener(this);
	}

	public void setFocus() {
		top.setFocus();
	}

	private void initializeColors() {
		if (colorRegistry == null)
			colorRegistry = new ColorRegistry(top.getDisplay());

		if (!colorRegistry.hasValueFor(BACKGROUNDCOLOR))
			colorRegistry.put(BACKGROUNDCOLOR, new RGB(255, 255, 255));

		if (!colorRegistry.hasValueFor(FOREGROUNDCOLOR))
			colorRegistry.put(FOREGROUNDCOLOR, new RGB(98, 83, 125));
	}

	/**
	 * Called when a bug is added or removed so the category dropdown combo can
	 * be updated {@link IModelChangeListener}
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (combo != null && !combo.isDisposed()) {
			combo.getDisplay().syncExec(new Runnable() {
				public void run() {
					refreshCombo();
					// if combo was changed to default in above
					// and it doesn't match the previous selected
					// do a search w/ the new selection
					// this is for the case, for example, when a bug connection was selected
					// but the connection was removed, kicking off this event
					if (combo.getSelectionIndex() == BugnetApplicationCategoryHelper.DEFAULT_CATEGORY_INDEX
							&& BugnetResultManager.getInstance().getCategory() != BugnetApplicationCategoryHelper.DEFAULT_CATEGORY) {
						searchBugnet();
					}
				}
			});
		}
	}

	/**
	 * called when a user logs in {@link IBugnetAuthenticationListener}
	 */
	public void loggedInEvent() {
		// accessing UI elements - protect against invalid thread access
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				refreshLoginArea(true);
				// redo the bugnet search
				searchBugnet();
			}
		});
	}

	/**
	 * called when a user logs out {@link IBugnetAuthenticationListener}
	 */
	public void loggedOutEvent() {
		// accessing UI elements - protect against invalid thread access
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				refreshLoginArea(false);
				// make sure "My Applications" isn't set in combo
				defaultComboAfterLogout();
				// redo the bugnet search
				searchBugnet();
			}
		});
	}

	/*
	 * Gets data from BUGnet and refreshes the whole view
	 */
	public void refresh() {
		// accessing UI elements - protect against invalid thread access
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				// call bugnet to check login and redraw login area
				checkLoginAndDrawInfo();
				refreshCombo();
				// redo the bugnet search
				searchBugnet();
			}
		});
	}

	/**
	 * Helps set the combo to a default after logging out
	 */
	private void defaultComboAfterLogout() {
		if (appCategoryHelper.getCategories()[combo.getSelectionIndex()] == appCategoryHelper.MY_APPLICATIONS) {
			combo.select(appCategoryHelper.DEFAULT_CATEGORY_INDEX);
		}
	}

	/**
	 * initializes the application list part of the view that uses the
	 * BugnetViewer to draw
	 */
	private void createBugnetViewer() {
		form = toolkit.createScrolledForm(top);
		bugnetViewer = new BugnetViewer(form);
		// setInput just sets the model, does not cause a redraw
		bugnetViewer.setInput(applicationList);
	}

	/**
	 * Get data from bugnet and display it using the QueryBugnetJob
	 */
	private void queryBugnetAndDrawApplications() {
		queryBugnetAndDrawApplications(false);
	}

	/**
	 * Get data from bugnet & display using QueryBugnetJob this makes sure that
	 * we append the results to the existing model
	 * 
	 * @param appendResults
	 */
	private void queryBugnetAndDrawApplications(boolean appendResults) {
		// query bugnet, when done, draw viewer
		QueryBugnetJob queryBugnetJob = new QueryBugnetJob();
		queryBugnetJob.addJobChangeListener(new QueryBugnetJobChangeListener(appendResults));
		queryBugnetJob.schedule();
	}

	/**
	 * Check login w/ bugnet and refresh the area
	 */
	private void checkLoginAndDrawInfo() {
		// checking if logged in requires a web service request
		// so draw this part asynchronously
		CheckLoginJob checkLoginJob = new CheckLoginJob();
		checkLoginJob.addJobChangeListener(new CheckLoginJobChangeListener());
		checkLoginJob.schedule();
	}

	/**
	 * Draw search bar and category chooser section
	 */
	private void drawFilterArea() {
		filterArea = toolkit.createComposite(top);
		filterArea.setLayout(new GridLayout(3, false));
		filterArea.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		filterArea.setBackground(colorRegistry.get(BACKGROUNDCOLOR));
		drawCategoryChooser();
		drawSearchBar();
	}

	/**
	 * Draws logged in as or login link
	 */
	private void drawLoginArea() {
		loginArea = toolkit.createComposite(top);
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = layout.marginBottom = 0;
		layout.verticalSpacing = 0;
		loginArea.setLayout(layout);
		GridData gd = new GridData(SWT.END, SWT.CENTER, false, false);
		loginArea.setLayoutData(gd);

		// get login data and refresh the login area
		checkLoginAndDrawInfo();
	}

	/**
	 * Draws the area that lets you choose the category
	 */
	private void drawCategoryChooser() {
		combo = new Combo(filterArea, SWT.READ_ONLY);
		combo.setBackground(colorRegistry.get(BACKGROUNDCOLOR));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		combo.setLayoutData(gd);
		combo.addSelectionListener(new FilterSelectionAdapter());
		refreshCombo();
	}

	/**
	 * Draws the search bar at the top of the page
	 */
	private void drawSearchBar() {
		// Search Box
		searchText = toolkit.createText(filterArea, "", SWT.BORDER | SWT.SEARCH); //$NON-NLS-1$
		searchText.setBackground(colorRegistry.get(BACKGROUNDCOLOR));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		searchText.setLayoutData(gd);
		// prefill search textbox if it's there
		String search = BugnetResultManager.getInstance().getSearch();
		if (search != null)
			searchText.setText(search);
		searchText.addSelectionListener(new FilterSelectionAdapter());

		// clear or 'X' button - clears searchbox text
		Button xbutton = toolkit.createButton(filterArea, "X", SWT.NONE);
		gd = new GridData(SWT.END, SWT.NONE, false, false);
		xbutton.setLayoutData(gd);
		xbutton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchText.setText("");
				searchText.setFocus();
			}
		});

		// submit button
		Button button = toolkit.createButton(filterArea, "Search", SWT.NONE);
		gd = new GridData(SWT.END, SWT.NONE, false, false);
		button.setLayoutData(gd);
		button.addSelectionListener(new FilterSelectionAdapter());
	}

	/**
	 * Draws the more link if there's something to display
	 */
	private void drawBottomNav() {
		if (bottomNav != null)
			bottomNav.dispose();
		bottomNav = toolkit.createComposite(top);
		bottomNav.setLayout(new GridLayout(2, false));
		bottomNav.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		bottomNav.setBackground(colorRegistry.get(BACKGROUNDCOLOR));

		if (bottomLabel != null)
			bottomLabel.dispose();
		String labelText = "Viewing " + applicationList.size() + " Applications. ";
		bottomLabel = toolkit.createLabel(bottomNav, labelText);
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		bottomLabel.setLayoutData(gd);

		if (moreLink != null)
			moreLink.dispose();
		if (BugnetResultManager.getInstance().getApplications() == null
				|| BugnetResultManager.getInstance().getApplications().size() < BugnetResultManager.getInstance().getCount()) {
			return;
		}
		moreLink = toolkit.createHyperlink(bottomNav, "View more...", SWT.NONE);

		moreLink.setToolTipText("View more...");
		moreLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				// set to the next page
				BugnetResultManager.getInstance().setPage(BugnetResultManager.getInstance().getPage() + 1);
				// append results to existing results
				queryBugnetAndDrawApplications(true);
			}
		});
		moreLink.setForeground(colorRegistry.get(FOREGROUNDCOLOR));
		gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
		moreLink.setLayoutData(gd);
	}

	/**
	 * 
	 * Set up this view as a drop target for dragging apps to BUGnet
	 * 
	 */
	private void setupDropTarget() {
		DropTarget dt = new DropTarget(top, DND.DROP_MOVE | DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { ResourceTransfer.getInstance() });

		dt.addDropListener(new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent event) {
				if (!ResourceTransfer.getInstance().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_NONE;
				}
			}

			public void drop(DropTargetEvent event) {

				// First make sure BUGnet is activated
				if (!checkBugnetEnabledAndNotify())
					return;

				Object obj = event.data;
				// guard against dropping of the wrong type of thing
				if (!(obj instanceof IResource[]))
					return;

				// now make sure the package is all legit
				if (((IResource[]) obj).length > 0) {
					IResource res = ((IResource[]) obj)[0];

					if (res instanceof IProject) {
						IProject proj = (IProject) res;
						try {
							if (ProjectUtils.existsProblems(proj)) {
								IStatus status = new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Application '" + proj.getName()
										+ "' contains errors. Please fix errors before uploading.", null);
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
		});
	}

	/*
	 * Check if I have bugnet enabled and show a dialog if I don't
	 * return weather or not bugnet is enabled
	 */
	private boolean checkBugnetEnabledAndNotify() {
		// First make sure BUGnet is activated
		if (!DragonflyActivator.getDefault().getPluginPreferences().getBoolean(DragonflyActivator.PREF_BUGNET_ENABLED)) {
			// Show a Dialog with the message
			MessageDialog.openInformation(new Shell(), "BUGnet not enabled.", "Unable to contact BUGnet because BUGnet is disabled in preferences.  "
					+ "Please enable BUGnet in preferences and try again.");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * resets the data in the combo box using the applicationCategories it takes
	 * the default categories and adds the bug connections
	 */
	private void refreshCombo() {
		appCategoryHelper.resetCategories();
		appCategoryHelper.addCategories(getBugConnectionCategories(BugConnectionManager.getInstance().getBugConnections()));
		combo.setItems(appCategoryHelper.getCategories());
		combo.select(appCategoryHelper.getCategoryIndex(BugnetResultManager.getInstance().getCategory()));
	}

	/**
	 * refreshes the login area w/ login info or login link
	 */
	private void refreshLoginArea(boolean isLoggedIn) {
		if (loginArea == null || loginArea.isDisposed()) {
			return;
		}

		// clear loginArea by disposing children
		Control[] loginInfo = loginArea.getChildren();
		int len = loginInfo.length;
		for (int i = 0; i < len; i++) {
			if (loginInfo[i] != null && !loginInfo[i].isDisposed()) {
				loginInfo[i].dispose();
			}
		}
		// draw new info
		GridData gd = new GridData(SWT.END, SWT.CENTER, false, false);
		if (!isLoggedIn) {
			Hyperlink loginLink = toolkit.createHyperlink(loginArea, "login to BUGnet", SWT.NONE);
			loginLink.addHyperlinkListener(new LoginLinkListener());
			loginLink.setLayoutData(gd);
		} else {
			Label label = toolkit.createLabel(loginArea, "logged in as " + DragonflyActivator.getDefault().getAuthenticationData().getUsername());
			label.setLayoutData(gd);
		}

		loginArea.layout();
		top.layout();
	}

	/**
	 * Gets the existing bug connections for listing as a category
	 * 
	 * @return
	 */
	private List<String> getBugConnectionCategories(Collection<IModelNode> bugs) {
		List<String> connectionNames = new ArrayList<String>();
		// add the separator
		if (bugs.size() > 0) {
			connectionNames.add(BugnetApplicationCategoryHelper.BUG_CONNECTION_CATEGORY_SEPARATOR);
		}
		// add the connections
		Iterator<IModelNode> iterator = bugs.iterator();
		while (iterator.hasNext()) {
			connectionNames.add(BugnetApplicationCategoryHelper.BUG_CONNECTION_CATEGORY_PREFIX + " " + iterator.next().getName());
		}
		return connectionNames;
	}

	/**
	 * Called on search bar events to search Bugnet and return apps The guy
	 * doesn't care if we're logged in or not and doesn't deal with the login
	 */
	private synchronized void searchBugnet() {
		// reset found apps - these will be the new results
		BugnetResultManager.getInstance().reset();

		String search = searchText.getText();
		if (search != null && search.length() > 0) {
			BugnetResultManager.getInstance().setSearch(search);
		}

		// Deal with the combo selection
		int selected = combo.getSelectionIndex();
		if (selected >= 0 && selected < appCategoryHelper.getCategories().length) {
			String selectedCategory = appCategoryHelper.getCategories()[selected];
			// set category
			BugnetResultManager.getInstance().setCategory(selectedCategory);
			// if it's a bug connection, need to get packages out
			if (bugConnectionCategorySelected(selectedCategory)) {
				Bug connection = getConnectionForSelection(selectedCategory);
				if (connection != null) {
					BugnetResultManager.getInstance().setPackages(getPackagesForBugConnection(connection));
				}
			}
		}
		// want to draw applications w/o appending the results
		queryBugnetAndDrawApplications();
	}

	/**
	 * This gets out the
	 * 
	 * @param connection
	 * @return
	 */
	private List<String> getPackagesForBugConnection(Bug connection) {
		List<String> packages = new ArrayList<String>();
		try {
			packages = BugWSHelper.getPackages(connection.getPackageURL());
		} catch (IOException e) {
			UIUtils.handleNonvisualError("Unable to connect to BUG to get packages", e);
		}
		return packages;
	}

	/**
	 * check if selected category is a bug connection
	 * 
	 * @param selectedCategory
	 * @return
	 */
	private boolean bugConnectionCategorySelected(String selectedCategory) {
		return selectedCategory.startsWith(BugnetApplicationCategoryHelper.BUG_CONNECTION_CATEGORY_PREFIX);
	}

	/**
	 * 
	 * @param selection
	 * @return
	 */
	private Bug getConnectionForSelection(String selection) {
		String connectionName = selection.substring(BugnetApplicationCategoryHelper.BUG_CONNECTION_CATEGORY_PREFIX.length()).trim();
		if (connectionName == null || connectionName.length() < 1)
			return null;
		return BugConnectionManager.getInstance().getBugConnection(selection);
	}

	/**
	 * Job for checking web server for logged in status used asynchronously
	 * 
	 * @author brian
	 * 
	 */
	private class CheckLoginJob extends Job {

		public CheckLoginJob() {
			super("Checking login"); //$NON-NLS-1$
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IStatus status = Status.OK_STATUS;
			if (!BugnetAuthenticationHelper.isLoggedIn())
				status = Status.CANCEL_STATUS;
			return status;
		}

	}

	/**
	 * When we're done getting BUGNet data, call setInput on the bugnetViewer
	 * which will display the results
	 * 
	 * @author brian
	 * 
	 */
	private class QueryBugnetJobChangeListener implements IJobChangeListener {
		private boolean appendResults = false;

		public QueryBugnetJobChangeListener(boolean appendResults) {
			this.appendResults = appendResults;
		}

		// Don't need these methods
		public void aboutToRun(IJobChangeEvent event) {
		}

		public void awake(IJobChangeEvent event) {
		}

		public void running(IJobChangeEvent event) {
		}

		public void scheduled(IJobChangeEvent event) {
		}

		public void sleeping(IJobChangeEvent event) {
		}

		/**
		 * Just need to know when we're done querying BUGnet
		 */
		public void done(final IJobChangeEvent event) {
			if (PlatformUI.getWorkbench().getDisplay().isDisposed()) {
				return;
			}
			
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					if (bugnetViewer == null)
						return;
					if (!appendResults)
						applicationList.initApplicationList();
					if (event.getResult().isOK()) {
						applicationList.addApplications(BugnetResultManager.getInstance().getApplications());
					} else {
						applicationList.setNoAppsMessage(event.getResult().getMessage());
					}
					bugnetViewer.refresh();
					drawBottomNav();
					top.layout();
				}
			});
		}
	}

	/**
	 * When done checking bugnet for login, refresh that area
	 * 
	 * @author brian
	 * 
	 */
	private class CheckLoginJobChangeListener implements IJobChangeListener {
		// Don't need these methods
		public void aboutToRun(IJobChangeEvent event) {
		}

		public void awake(IJobChangeEvent event) {
		}

		public void running(IJobChangeEvent event) {
		}

		public void scheduled(IJobChangeEvent event) {
		}

		public void sleeping(IJobChangeEvent event) {
		}

		/**
		 * Just need to know when we're done querying BUGnet
		 */
		public void done(final IJobChangeEvent event) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					refreshLoginArea(event.getResult().isOK());
				}
			});
		}
	}

	/**
	 * Handles click of login link
	 * 
	 * @author brian
	 * 
	 */
	private class LoginLinkListener implements IHyperlinkListener {
		// Don't need these
		public void linkEntered(HyperlinkEvent e) {
		}

		public void linkExited(HyperlinkEvent e) {
		}

		/**
		 * launch login dialog box
		 */
		public void linkActivated(HyperlinkEvent event) {
			// First make sure BUGnet is activated
			if (!checkBugnetEnabledAndNotify())
				return;

			try {
				BugnetAuthenticationHelper.processLogin();
			} catch (IOException e) {
				UIUtils.handleVisualError("There was a problem connecting to BUGnet.  Please check your BUGnet preferences.", e);
			}
		}

	}

	/**
	 * Handles the selection of a filter element checks to see if we need to
	 * login to bugnet Logs in if we need to and then does the query
	 * 
	 * @author brian
	 * 
	 */
	private class FilterSelectionAdapter extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			checkLoginAndSearchBugnet();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			checkLoginAndSearchBugnet();
		}

		/**
		 * checks to see if we need to login if we do and we're not, login and
		 * do search via callback otherwise just do the search
		 */
		private void checkLoginAndSearchBugnet() {
			if (!checkBugnetEnabledAndNotify())
				return;

			// if we're the separator, just select the previous selection
			// and return
			if (isSeparator()) {
				combo.select(appCategoryHelper.getCategoryIndex(BugnetResultManager.getInstance().getCategory()));
				return;
			}

			if (loginRequired() && !BugnetAuthenticationHelper.isLoggedIn()) {
				try {
					// This'll call the loggedInEvent() in this class once it's done;
					// which calls searchBugnet();        	
					if (!BugnetAuthenticationHelper.login()) {
						combo.select(appCategoryHelper.getCategoryIndex(BugnetResultManager.getInstance().getCategory()));
					}
				} catch (IOException e) {
					UIUtils.handleNonvisualError("Unable to login to BUGnet", e);
					searchBugnet();
				}
			} else {
				searchBugnet();
			}
		}

		/*
		 * Check to see if we've selected the separator
		 */
		private boolean isSeparator() {
			int selected = combo.getSelectionIndex();
			if (selected < 0 || selected >= appCategoryHelper.getCategories().length)
				return false;
			if (appCategoryHelper.getCategories()[selected] == BugnetApplicationCategoryHelper.BUG_CONNECTION_CATEGORY_SEPARATOR)
				return true;
			return false;
		}

		/*
		 *  Check to see if the query requires a login
		 */
		private boolean loginRequired() {
			int selected = combo.getSelectionIndex();
			// guard against some weird selection
			if (selected < 0 || selected >= appCategoryHelper.getCategories().length)
				return false;
			// if it's MY APPLICATIONS, then login required
			if (appCategoryHelper.getCategories()[selected] == BugnetApplicationCategoryHelper.MY_APPLICATIONS)
				return true;
			return false;
		}
	}

}
