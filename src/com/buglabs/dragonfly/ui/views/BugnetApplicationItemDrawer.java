package com.buglabs.dragonfly.ui.views;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.bugnet.BugnetWSHelper;
import com.buglabs.dragonfly.model.BUGNetProgramReferenceNode;
import com.buglabs.dragonfly.model.ILinkableModelNode;
import com.buglabs.dragonfly.swt.Rating;
import com.buglabs.dragonfly.ui.Activator;
import com.buglabs.dragonfly.ui.actions.BugNetToIDEActionDelegate;
import com.buglabs.dragonfly.ui.actions.ImportFromBUGNetAction;
import com.buglabs.dragonfly.ui.actions.LaunchBrowserAction;
import com.buglabs.dragonfly.ui.dnd.BugNETProgramReferenceNodeTransfer;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.dragonfly.util.URLUtils;

/**
 * Draws a single application item
 * Keeps state to set background color and stuff like that
 * 
 * @author brian
 *
 */
public class BugnetApplicationItemDrawer {
	private static final int 	THUMBNAIL_WIDTH 	= 52;
	private static final int 	THUMBNAIL_HEIGHT 	= 39;
	private static final String SCALED_IMAGE_SUFFIX = "_scaled"; //$NON-NLS-1$
	private static final int 	LABEL_LENGTH 		= 15;
	private static final int 	CHAR_HEIGHT 		= 8;
	private static final int 	RATING_WIDTH_HINT 	= 50;
	private static final int 	RATING_HEIGHT_HINT 	= 15;
	private static final int 	USERNAME_WIDTH_HINT = 150;
	private static final int 	USERNAME_HEIGHT_HINT= 17;

	private List<Label> itemDescriptionLabels	= new ArrayList();
	private List<String> itemDescriptions		= new ArrayList();
	
	private Composite parent;
	private FormToolkit toolkit;
	// colors
	private Color lightGray1Color;
	private Color lightGray2Color;
	private Color borderColor;
	private Color hyperlinkColor;
	private Color usernameColor;
	private Color descriptionColor;
	private ImageRegistry imageRegistry;
	private boolean isColor1;
	// fonts
	private Font titleFont;
	private Font descriptionFont;
	
	/**
	 * initialize fonts and everyting
	 * 
	 * @param parent should be a composite child of a form
	 */
	public BugnetApplicationItemDrawer(Composite parent) {
		this.parent = parent;
		toolkit = new FormToolkit(parent.getDisplay());
		imageRegistry = new ImageRegistry();
		initializeColors();
		initializeFonts();
		parent.setBackground(lightGray2Color);
		parent.addControlListener(new ResizeListener());
	}
	
	/**
	 * Clean up colors and fonts
	 */
	public void dispose() {
		disposeColors();
		disposeFonts();
	}
	
	
	/**
	 *  initialize all the colors used to draw an application item
	 */
	private void initializeColors() {
		lightGray1Color = new Color(Display.getCurrent(), new RGB(247, 247, 247));
		lightGray2Color = new Color(Display.getCurrent(), new RGB(255, 255, 255));
		borderColor 	= new Color(Display.getCurrent(), new RGB(255, 255, 255));
		hyperlinkColor 	= new Color(Display.getCurrent(), new RGB(98,83,125));
		usernameColor	= new Color(Display.getCurrent(), new RGB(123,129,138));
		descriptionColor= new Color(Display.getCurrent(), new RGB(85,85,85));
	}
	
	
	/**
	 * initialize all the fonts used draw an application item
	 */
	private void initializeFonts() {
		FontData[] fdTitle = JFaceResources.getDefaultFont().getFontData();

		for (int i = 0; i < fdTitle.length; ++i) {
			fdTitle[i].setStyle(SWT.BOLD);
		}

		FontData[] fdDescription = JFaceResources.getDefaultFont().getFontData();
		for (int i = 0; i < fdDescription.length; ++i) {
			fdDescription[i].setHeight(CHAR_HEIGHT);
		}

		Display display = parent.getDisplay();
		titleFont = new Font(display, fdTitle);
		descriptionFont = new Font(display, fdDescription);
	}	
	
	/**
	 * dispose all the colors created in initializeColors
	 */
	private void disposeColors() {
		if (lightGray1Color != null) 	lightGray1Color.dispose();
		if (lightGray2Color != null) 	lightGray2Color.dispose();
		if (borderColor != null) 		borderColor.dispose();
		if (hyperlinkColor != null) 	hyperlinkColor.dispose();
		if (usernameColor != null) 		usernameColor.dispose();
		if (descriptionColor != null) 	descriptionColor.dispose();
	}
	
	/**
	 * dispose all the fonts created in initializeFonts
	 */
	private void disposeFonts() {
		if (titleFont != null) 			titleFont.dispose();
		if (descriptionFont != null) 	descriptionFont.dispose();
	}
	
	/**
	 * allows us to alternate colors for each item
	 * 
	 * @return the background color for this cycle
	 */
	private Color getBackgroundColor() {
		Color color=lightGray1Color;
		if (isColor1) color = lightGray2Color;
		isColor1=!isColor1;
		return color;		
	}
	
	/**
	 * creates a thumbnail for an app item
	 * 
	 * @param item
	 * @return
	 */
	private Image getThumbnailFor(BUGNetProgramReferenceNode item) {
		Image img = DragonflyActivator.getDefault()
			.getImageRegistry().get(item.getImageKey());
		if (img == null) {
			img = Activator.getDefault()
				.getImageRegistry().get(Activator.IMAGE_KEY_IMAGE_NOT_FOUND);
		}
		return new Image(null, img.getImageData().scaledTo(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));
	}
	
	/**
	 * stores image in the local registry
	 * 
	 * @param image
	 * @param identifier
	 */
	private void putImageInRegistry(Image image, String identifier) {
		if (imageRegistry.get(identifier) != null) imageRegistry.put(identifier, image);
	}
	
	/**
	 * Mac specific code to set the hyperlink background
	 * 
	 * @param hyperlink
	 */
	private void setHyperlinkBackgroundForMac(Hyperlink hyperlink){
		Set s = System.getProperties().keySet();
		String osName = System.getProperty("os.name");

		if (osName != null && !osName.equals("Mac OS X")) {
			hyperlink.addPaintListener(new PaintListener() {

				public void paintControl(PaintEvent e) {
					((Hyperlink) e.widget).setBackground(
							(Color) ((Map) e.widget.getData()).get("backgroundcolor"));
				}
			});
		}		
	}
	
	/**
	 * Sets up the hyperlink for an item as somethin you can drag
	 * 
	 * @param h
	 */
	private void setupDragSource(Hyperlink h) {
		DragSource ds = new DragSource(h, DND.DROP_MOVE | DND.DROP_COPY);

		Transfer[] transfer = new Transfer[] { PluginTransfer.getInstance() };
		ds.setTransfer(transfer);
		ds.addDragListener(new HyperLinkDragSourceListener(h));
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
	 * This is the main public function.  It draws a new BUGnet app in
	 * the parent composite set up in the constructor.
	 * 
	 * @param item
	 */
	public synchronized void draw(BUGNetProgramReferenceNode item) {
		// Setup the main composite
		Composite comp = toolkit.createComposite(parent, SWT.NONE);
		Color backgroundColor = getBackgroundColor();
		comp.setBackground(backgroundColor);
		GridData compgd = GridDataFactory.fillDefaults().create();
		compgd.grabExcessHorizontalSpace = true;
		compgd.grabExcessVerticalSpace = true;
		comp.setLayoutData(compgd);
		comp.setLayout(new GridLayout(3, false));

		// Get get the thumbnail image
		Image image = getThumbnailFor(item);
		putImageInRegistry(image, item.getImageKey() + SCALED_IMAGE_SUFFIX);
		
		// Draw thumbnail image
		Label imageLabel = toolkit.createLabel(comp, ""); //$NON-NLS-1$
		imageLabel.setImage(image);
		imageLabel.setBackground(backgroundColor);
		GridData imageGD = GridDataFactory.fillDefaults().create();
		imageGD.verticalSpan = 3;
		imageLabel.setLayoutData(imageGD);
		
		// Draw the hyperlink title
		Hyperlink hyperlink = toolkit.createHyperlink(
				comp, UIUtils.truncateString(item.getLabel(), LABEL_LENGTH), SWT.NONE);
		hyperlink.setToolTipText(item.getLabel());
		hyperlink.addHyperlinkListener(new BUGNetHyperlinkListener(item));
		HashMap hyperLinkData = new HashMap();
		hyperLinkData.put("program", item);
		hyperLinkData.put("backgroundcolor", backgroundColor);
		hyperlink.setFont(titleFont);
		hyperlink.setBackground(backgroundColor);
		hyperlink.setForeground(hyperlinkColor);
		GridData hGD = GridDataFactory.fillDefaults().create();
		hGD.grabExcessHorizontalSpace = true;
		hyperlink.setLayoutData(hGD);
		hyperlink.setBackgroundMode(SWT.INHERIT_NONE);
		hyperlink.setData(hyperLinkData);
		setHyperlinkBackgroundForMac(hyperlink);
		setupDragSource(hyperlink);
		
		// Draw the rating
		double rating = 3.5;
		Rating r = new Rating(comp, SWT.NONE);
		r.setBackground(backgroundColor);
		String ratingVal = item.getRating();
		if (ratingVal.length() == 0) {
			ratingVal = "0";
		}
		r.setRating(Double.parseDouble(ratingVal));
		GridData ratingGD = GridDataFactory.fillDefaults().create();
		ratingGD.widthHint = RATING_WIDTH_HINT;
		ratingGD.heightHint = RATING_HEIGHT_HINT;
		r.setLayoutData(ratingGD);
		
		// Draw the username and download count
		GridData usernameGD = GridDataFactory.fillDefaults().create();
		usernameGD.verticalAlignment = SWT.TOP;
		usernameGD.widthHint = USERNAME_WIDTH_HINT;
		usernameGD.heightHint = USERNAME_HEIGHT_HINT;
		usernameGD.horizontalSpan = 2;
		Label lblUserNameAndDownloads = new Label(comp, SWT.NONE);
		lblUserNameAndDownloads.setText(
				item.getUserName() + "  " + item.getDownload_count() + " downloads"); //$NON-NLS-1$ //$NON-NLS-2$
		lblUserNameAndDownloads.setLayoutData(usernameGD);
		lblUserNameAndDownloads.setBackground(backgroundColor);
		lblUserNameAndDownloads.setForeground(usernameColor);
		String desc = item.getDescription().trim();

		if (!desc.equals("")) { //$NON-NLS-1$
			Label descLabel = toolkit.createLabel(comp, desc, SWT.NONE);
			itemDescriptionLabels.add(descLabel);
			itemDescriptions.add(desc);
			descLabel.setFont(descriptionFont);
			descLabel.setLayoutData(usernameGD);
			descLabel.setBackground(backgroundColor);
			descLabel.setForeground(descriptionColor);
			descLabel.setToolTipText(desc);
		}
		
		createContextMenu(comp, item);		
	}
	
	
	
	/************************** LISTENERS BELOW **********************************/
	
	/**
	 * This listener sits on the parent composite of the items we're
	 * drawing.  It handles the reformating of the description text
	 */
	private class ResizeListener extends ControlAdapter {
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
		
		public void controlResized(ControlEvent event) {
			System.out.println("resize");			
			int newWidth = parent.getSize().x / 6;
			Label descriptionLabel;
			GC gc;
			String shortenedText;
			for (int i = 0; i<itemDescriptionLabels.size(); i++) {
				descriptionLabel = itemDescriptionLabels.get(i);
				if (descriptionLabel != null && !descriptionLabel.isDisposed()) {
					gc = new GC(descriptionLabel);
					shortenedText = shortenText(gc, itemDescriptions.get(i), newWidth);
					itemDescriptionLabels.get(i).setText(shortenedText);
					itemDescriptionLabels.get(i).pack(true);
				}
			}
		}
	}

	
	/**
	 * This listener goes on the item hyperlink and allows you to drag stuff from the
	 * BugnetView
	 */
	private class HyperLinkDragSourceListener implements DragSourceListener {

		Hyperlink h;

		public HyperLinkDragSourceListener(Hyperlink h) {
			this.h = h;
		}

		// not used
		public void dragFinished(DragSourceEvent event) {}

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

				PluginTransferData data 
					= new PluginTransferData(BugNetToIDEActionDelegate.ACTION_ID, BugNETProgramReferenceNodeTransfer
						.getInstance().toByteArray(new BUGNetProgramReferenceNode[] { prog }));

				event.data = data;
			}
		}

		public void dragStart(DragSourceEvent event) {
			event.doit = true;
		}
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
				if (url == null) url = new URL(n.getUrl());
				
				LaunchBrowserAction action = new LaunchBrowserAction(url, n.getLabel());
				action.run();
			} catch (MalformedURLException e1) {
				UIUtils.handleVisualError("Invalid URL: " + n.getUrl(), e1); //$NON-NLS-1$
			}
		}
		
		// not used
		public void linkEntered(HyperlinkEvent e) {}
		public void linkExited(HyperlinkEvent e) {}
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
}
