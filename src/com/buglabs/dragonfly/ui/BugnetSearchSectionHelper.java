package com.buglabs.dragonfly.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import com.buglabs.dragonfly.bugnet.BugnetResultManager;
import com.buglabs.dragonfly.exception.BugnetAuthenticationException;
import com.buglabs.dragonfly.exception.BugnetException;
import com.buglabs.dragonfly.model.BUGNetProgramReferenceNode;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.ui.providers.Messages;
import com.buglabs.dragonfly.ui.views.BUGNetView;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * 
 * Deals with drawing the BUGnet view middle section
 * Handles the search event, does the search and draws the results
 *  
 * 
 * 
 * @author brian
 *
 */
public class BugnetSearchSectionHelper {

	private Composite 	sectionClient;
	private Composite 	parent;
	private Composite 	toolbar; // part that holds search box and button
	private Composite 	data;	 // part that holds the results
	private Text 		searchText;
	private FormToolkit toolkit;
	private BugnetResultManager resultManager;
	private BUGNetView	bugnetView;
	
	public BugnetSearchSectionHelper() {}
	
	public void init(BUGNetView bugnetView, 
			Composite sectionClient, BugnetResultManager resultManager) {
		this.sectionClient 	= sectionClient;
		this.resultManager	= resultManager;
		this.bugnetView		= bugnetView;
		parent				= sectionClient.getParent();
		toolkit  			= new FormToolkit(parent.getDisplay());
		drawToolbar();
		drawData();		
	}

	private void drawToolbar() {
		toolbar = toolkit.createComposite(sectionClient);
		toolbar.setLayout(new GridLayout(2,false));
		
		// Search Box
		searchText = toolkit.createText(toolbar, "", SWT.BORDER | SWT.SEARCH);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		searchText.setLayoutData(gd);
		// prefill search textbox if it's there
		String search = resultManager.getSearch();
		if (search != null) searchText.setText(search);
		searchText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				BugnetSearchJob searchJob = new BugnetSearchJob();
				searchJob.schedule();				
			}
		});
		
		// submit button
		Button button = toolkit.createButton(toolbar, "Search", SWT.PUSH);
		gd = new GridData(SWT.END, SWT.CENTER, false, false);
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				BugnetSearchJob searchJob = new BugnetSearchJob();
				searchJob.schedule();
			}
		});
		
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		toolbar.setLayoutData(gd);
	}
	
	private void redrawData() {
		if (data != null) data.dispose();
		drawData();
		data.getParent().layout();
		bugnetView.getForm().reflow(true);		
	}	
	
	private void drawData() {
		data = toolkit.createComposite(sectionClient);
		data.setLayout(new GridLayout());
		addResultsToData();
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.setLayoutData(gd);		
	}	
	
	private void addResultsToData() {
		List applications = resultManager.getApplications();		
		GridData gd;
		// If no apps, display no apps message
		if (applications == null || applications.size() == 0) {
			Label cLabel = toolkit.createLabel(data, "No Applications Found");
		    gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
		    cLabel.setLayoutData(gd);			
			return;
		}

		// loop through apps, displaying them
		BUGNetProgramReferenceNode node = null;
		// hack to set up stome stuff in bugnetView
		bugnetView.setupForDrawingAppItems();
		// 'nother hack to get stuff from bugnetView
		Color[] viewColors = bugnetView.getColors();
		data.setBackground(viewColors[0]);
		boolean grayBackground = true;
		Color backgroundColor;		
		for (Iterator i = applications.iterator(); i.hasNext();) {
			node = (BUGNetProgramReferenceNode)i.next();
			if (node != null) {
				
				grayBackground = !grayBackground;
				if (grayBackground) backgroundColor = viewColors[1];
				else 				backgroundColor = viewColors[2];			
				
				bugnetView.drawAppItem(data, backgroundColor, node);
			}
		}
		
		// if number of apps is less than page size, just return
		if (applications.size() < resultManager.getCount()) return;
		
		// otherwise, we should display the more link, so a user can get more
		Hyperlink moreLink = toolkit.createHyperlink(data, "more...", SWT.NONE);
	    moreLink.setToolTipText("More");
	    moreLink.addHyperlinkListener(new HyperlinkAdapter(){
			public void linkActivated(HyperlinkEvent e) {
				BugnetMoreResultsJob job = new BugnetMoreResultsJob((Hyperlink) e.getSource());
				job.schedule();
			}
	    });
	    moreLink.setForeground(new Color(Display.getCurrent(), new RGB(98,83,125)));
	    gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
	    moreLink.setLayoutData(gd);		
	}
	
	/**
	 * Do the BUGnet Search
	 * 
	 * @author brian
	 *
	 */
	private class BugnetSearchJob extends Job {

		private IStatus status = Status.OK_STATUS;
		private DoQueryJob	doQueryJob;
		
		public BugnetSearchJob() {
			super("Bugnet Search");
			doQueryJob = new DoQueryJob();
		}

		protected IStatus run(IProgressMonitor monitor) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					resultManager = BugnetResultManager.getInstance();
					resultManager.reset();
					String s = searchText.getText();
					if (s != null && s.length() > 0) {
						resultManager.setSearch(searchText.getText());
					}
					// Schedule query with query job below
					doQueryJob.schedule();
					
					doQueryJob.addJobChangeListener(new IJobChangeListener(){
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
									redrawData();
								}
							});
						}
						
					});	
				}
			});			
			return status;
		}
	}
	
	
	/**
	 * Do the work to get more items on the list
	 * 
	 * @author brian
	 *
	 */
	private class BugnetMoreResultsJob extends Job {

		private IStatus status = Status.OK_STATUS;
		private Hyperlink sourceLink;
		private DoQueryJob	doQueryJob;
		
		public BugnetMoreResultsJob(Hyperlink source) {
			super("Getting more results from BUGnet");
			sourceLink = source;
			doQueryJob = new DoQueryJob();
		}

		protected IStatus run(final IProgressMonitor monitor) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					// make this hyperlink go away
					sourceLink.dispose();
					
					// Set up result Manager, getting ready for query
					String s = searchText.getText();
					if (s != null && s.length() > 0) {
						resultManager.setSearch(searchText.getText());
					}
					resultManager.setPage(resultManager.getPage()+1);

					// Schedule query with query job below
					doQueryJob.schedule();
					
					doQueryJob.addJobChangeListener(new IJobChangeListener(){
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
									addResultsToData();
									bugnetView.getForm().reflow(true);
								}
							});
						}
						
					});
				}
			});			
			return status;
		}
	}	
	
	
	/**
	 * 
	 * A Job just for querying BUGnet
	 * 
	 * @author brian
	 *
	 */
	private class DoQueryJob extends Job {

		public DoQueryJob() {
			super("Retrieving BUGnet applications...");
		}

		protected IStatus run(IProgressMonitor monitor) {
			IStatus status = Status.OK_STATUS;
			try {
				resultManager.doQuery();
			} catch (IOException e) {
				UIUtils.handleNonvisualError("There was an error connecting to BUGnet.", e);
				status = handleException(e);
			}
			return status;
		}
		
	}
	
	
	
	/**
	 * helps handle a Job error
	 * 
	 * @param e
	 * @return
	 */
	private IStatus handleException(Exception e) {
		return new Status(IStatus.ERROR, com.buglabs.dragonfly.ui.Activator.PLUGIN_ID, IStatus.ERROR, 
				"There was an error connecting to BUGnet: " + e.getMessage(), e);
	}

}
