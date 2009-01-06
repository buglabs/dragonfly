package com.buglabs.dragonfly.ui.views.bugnet;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.lang.annotation.Inherited;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.IBUGnetAuthenticationListener;
import com.buglabs.dragonfly.bugnet.BugnetApplicationCategoryHelper;
import com.buglabs.dragonfly.bugnet.BugnetResultManager;
import com.buglabs.dragonfly.exception.BugnetAuthenticationException;
import com.buglabs.dragonfly.exception.BugnetException;
import com.buglabs.dragonfly.model.BUGNetProgramReferenceNode;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.IModelChangeListener;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.BugnetAuthenticationHelper;
import com.buglabs.dragonfly.ui.actions.RefreshBugNetViewAction;
import com.buglabs.dragonfly.ui.actions.SearchBugNetAction;
import com.buglabs.dragonfly.ui.views.mybugs.MyBugsView;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * The BugnetView, not to be confused with the old BUGnetView
 * This one uses the BugnetViewer and an MVC approach to draw a view
 * containing a list of bug applications
 * 
 * @author brian
 *
 */
public class BugnetView extends ViewPart implements IModelChangeListener, IBUGnetAuthenticationListener {
    private static final int DEFAULT_CATEGORY_INDEX = 0;
    private Color backgroundColor;
    private BugnetApplicationCategoryHelper appCategoryHelper;
	private Composite top;
	private Composite loginArea;
	private Composite filterArea;	
	private Combo combo;
    private Text searchText;
	private Hyperlink moreLink;
	private BugnetViewer bugnetViewer;
	private BugnetApplicationList applicationList;
	private ScrolledForm form;
	private FormToolkit toolkit;
	private boolean loggedIn = false;
	
	/**
	 * called from the framework
	 * This is where the view gets drawn
	 */
	public void createPartControl(Composite parent) {
		// initialize composite for drawing
	    backgroundColor = new Color(parent.getDisplay(), new RGB(255,255,255));
	    top = new Composite(parent, SWT.None);
	    top.setBackground(backgroundColor);
	    GridLayout topLayout = new GridLayout(1,true);
	    topLayout.marginWidth = 2;
	    top.setLayout(topLayout);
		toolkit = new FormToolkit(top.getDisplay());
		
        // More initial setup
		appCategoryHelper = new BugnetApplicationCategoryHelper();
        applicationList   = new BugnetApplicationList();
		
        // layout the parts of the view
        drawLoginArea();
        // filterArea contains both search box and category combo
        drawFilterArea();
        // initialize the viewer
        createBugnetViewer();
		// run job to query and draw
		queryBugnetAndDrawApplications();
	}

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        site.getActionBars().getToolBarManager().add(new RefreshBugNetViewAction(this));
        site.getActionBars().getToolBarManager().add(new SearchBugNetAction());
        DragonflyActivator.getDefault().addListener(this);
        DragonflyActivator.getDefault().addBUGnetAuthenticationListener(this);
    }

    public void setFocus() {
		top.setFocus();
	}

    /**
     * Called when a bug is added or removed so the
     * category dropdown combo can be updated
     * {@link IModelChangeListener}
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (combo != null && !combo.isDisposed()) {
            combo.getDisplay().syncExec(new Runnable() {
                public void run() {
                    // TODO - get the existing bug connections and store them locally
                    // TODO - resetCombo (as below) but make it use the local bug connections
                    resetCombo();
                }
            });
        }
    }
    
    /**
     * called when a user logs in
     * {@link IBUGnetAuthenticationListener}
     */
    public void listen() {
        // update login area
        checkLoginAndDrawInfo();
        // reset the apps view
        refreshApplications();
    }   

    public void refreshApplications() {
        searchBugnet();
    }
    
	/**
	 * initializes the application list part of the view
	 * that uses the BugnetViewer to draw
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
		// query bugnet, when done, draw viewer
		QueryBugnetJob queryBugnetJob = new QueryBugnetJob();
		queryBugnetJob.addJobChangeListener(new QueryBugnetJobChangeListener());
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
        filterArea.setLayout(new GridLayout(3,false));
        filterArea.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        filterArea.setBackground(backgroundColor);
        drawSearchBar();
        drawCategoryChooser();	    
	}
	
	
	/**
	 * Draws logged in as or login link
	 */
	private void drawLoginArea() {
	    loginArea = toolkit.createComposite(top);
	    loginArea.setLayout(new GridLayout(1, false));
        
	    GridData gd = new GridData(SWT.END, SWT.CENTER, true, false);
	    loginArea.setLayoutData(gd);	    
	    
	    // get login data and refresh the login area
	    checkLoginAndDrawInfo();
	}
	
	/**
	 * Draws the area that lets you choose the category
	 */
    private void drawCategoryChooser() {
        Label label = toolkit.createLabel(filterArea, "in");
        GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        label.setLayoutData(gd);

        combo = new Combo(filterArea, SWT.READ_ONLY);
        combo.setBackground(backgroundColor);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        combo.setLayoutData(gd);
        resetCombo();
        
        // submit button
        Button button = toolkit.createButton(filterArea, "Go", SWT.NONE);
        gd = new GridData(SWT.END, SWT.NONE, false, false);
        button.setLayoutData(gd);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                searchBugnet();
            }
        });        
            
    }	
	
    
    /**
     * Draws the search bar at the top of the page
     */
    private void drawSearchBar() {
        Label label = toolkit.createLabel(filterArea, "Search");
        GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        label.setLayoutData(gd);
        
        // Search Box
        searchText = toolkit.createText(filterArea, "", SWT.BORDER | SWT.SEARCH); //$NON-NLS-1$
        searchText.setBackground(backgroundColor);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        searchText.setLayoutData(gd);
        // prefill search textbox if it's there
        String search = BugnetResultManager.getInstance().getSearch();
        if (search != null) searchText.setText(search);
        searchText.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                searchBugnet();
            }
        });
                
    }
    
    
    /**
     * Draws the more link if there's something to display
     */
    private void drawMoreLink(){
        if (moreLink != null) moreLink.dispose();
        if (BugnetResultManager.getInstance().getApplications().size() <
                BugnetResultManager.getInstance().getCount()) {
            return;
        }
        moreLink = toolkit.createHyperlink(top, "more...", SWT.NONE);

        moreLink.setToolTipText("more...");
        moreLink.addHyperlinkListener(new HyperlinkAdapter(){
            public void linkActivated(HyperlinkEvent e) {
                // set to the next page
                BugnetResultManager.getInstance().setPage(
                        BugnetResultManager.getInstance().getPage()+1);
                queryBugnetAndDrawApplications();
            }
        });
        moreLink.setForeground(new Color(Display.getCurrent(), new RGB(98,83,125)));
        GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        moreLink.setLayoutData(gd);             
    }        
    
    
    /**
     * resets the data in the combo box using the applicationCategories
     * it takes the default categories and adds the bug connections
     */
    private void resetCombo() {
        appCategoryHelper.resetCategories();
        appCategoryHelper.addCategories(
                getBugConnectionCategories(BugConnectionHelper.getBugConnections()));
        combo.setItems(appCategoryHelper.getCategories());
        combo.select(appCategoryHelper.getCategoryIndex(
                BugnetResultManager.getInstance().getCategory()));
    }
    

    /**
     * refreshes the login area w/ login info or login link
     */
    private void refreshLoginArea() {
        if (loginArea == null) return;
        
        // clear loginArea by disposing children
        Control[] loginInfo = loginArea.getChildren();
        int len = loginInfo.length;
        for (int i=0; i<len; i++) {
            if (loginInfo[i] != null) {
                loginInfo[i].dispose();
            }
        }
        
        // draw new info
        GridData gd = new GridData(SWT.END, SWT.CENTER, false, false);
        if (!loggedIn) {
            Hyperlink loginLink = toolkit.createHyperlink(loginArea, "login to BUGnet", SWT.NONE);
            loginLink.addHyperlinkListener(new LoginLinkListener());
            loginLink.setLayoutData(gd);
        } else {
            Label label = toolkit.createLabel(loginArea, 
                    "logged in as " + 
                    DragonflyActivator.getDefault().getAuthenticationData().getUsername());
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
    private List<String> getBugConnectionCategories(List<BugConnection> connections) {
        List<String> connectionNames = new ArrayList<String>();
        Iterator<BugConnection> iterator = connections.iterator();
        while (iterator.hasNext()) {
            connectionNames.add(BugnetApplicationCategoryHelper.
                    BUG_CONNECTION_CATEGORY_PREFIX + " " + iterator.next().getName());
        }
        return connectionNames;
    }

    
    /**
     * 
     * @param selectedCategory
     */
    private void verifyLoggedIn(String selectedCategory) {
        if (selectedCategory == BugnetApplicationCategoryHelper.MY_APPLICATIONS) {
            try {
                if (!BugnetAuthenticationHelper.login()) {
                    combo.select(appCategoryHelper.getCategoryIndex(
                            BugnetResultManager.getInstance().getCategory()));
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    
    /**
     * Called on search bar events to search Bugnet and return apps
     */
    private void searchBugnet(){
        // reset model - will repopulate w/ results
        applicationList.initApplicationList();
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
            // make sure we're logged in if we need to be
            verifyLoggedIn(selectedCategory);
            // set category
            BugnetResultManager.getInstance().setCategory(selectedCategory);
            // if it's a bug connection, need to get packages out
            if (bugConnectionCategorySelected(selectedCategory)) {
                BugConnection connection = getConnectionForSelection(selectedCategory);
                if (connection != null) {
                    BugnetResultManager.getInstance().setPackages(
                            BugConnectionHelper.getPackagesForBugConnection(connection));
                }
            }
        }
        queryBugnetAndDrawApplications();
    }
	
    
    /**
     * check if selected category is a bug connection
     * 
     * @param selectedCategory
     * @return
     */
    private boolean bugConnectionCategorySelected(String selectedCategory) {
        return selectedCategory.startsWith(
                BugnetApplicationCategoryHelper.BUG_CONNECTION_CATEGORY_PREFIX);
    }

    /**
     * 
     * @param selection
     * @return
     */
    private BugConnection getConnectionForSelection(String selection) {
        String connectionName = selection.substring(
                BugnetApplicationCategoryHelper.BUG_CONNECTION_CATEGORY_PREFIX.length()).trim();
        if (connectionName == null || connectionName.length() < 1) return null;
        return BugConnectionHelper.getBugConnectionByName(connectionName);
    }


    /**
     * Job for checking web server for logged in status
     * used asynchronously
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
            loggedIn = BugnetAuthenticationHelper.isLoggedIn();
            return Status.OK_STATUS;
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
	    // Don't need these methods
	    public void aboutToRun(IJobChangeEvent event) {}
	    public void awake(IJobChangeEvent event) {}
	    public void running(IJobChangeEvent event) {}
	    public void scheduled(IJobChangeEvent event) {}
	    public void sleeping(IJobChangeEvent event) {}
	    
	    /**
	     *  Just need to know when we're done querying BUGnet
	     */
	    public void done(IJobChangeEvent event) {
	        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
	            public void run() {
	                if (bugnetViewer == null) return;
	                applicationList.addApplications(
	                        BugnetResultManager.getInstance().getApplications());
	                // Set the model on the viewer - this will cause the viewer to draw
	                bugnetViewer.refresh();
	                drawMoreLink();
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
        public void aboutToRun(IJobChangeEvent event) {}
        public void awake(IJobChangeEvent event) {}
        public void running(IJobChangeEvent event) {}
        public void scheduled(IJobChangeEvent event) {}
        public void sleeping(IJobChangeEvent event) {}
        
        /**
         *  Just need to know when we're done querying BUGnet
         */
        public void done(IJobChangeEvent event) {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                public void run() {
                    refreshLoginArea();
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
        public void linkEntered(HyperlinkEvent e) {}
        public void linkExited(HyperlinkEvent e) {}        
        
        /**
         * launch login dialog box
         */
        public void linkActivated(HyperlinkEvent event) {
            try {
                BugnetAuthenticationHelper.login();
            } catch (IOException e) {
                UIUtils.handleVisualError(
                        "There was a problem connecting to BUGnet.  Please check your BUGnet preferences.", e);
            }
        }

    }
	
}
