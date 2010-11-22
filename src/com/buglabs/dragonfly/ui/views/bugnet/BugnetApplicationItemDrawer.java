package com.buglabs.dragonfly.ui.views.bugnet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
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
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
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
 * Draws a single application item Keeps state to set background color and stuff
 * like that
 * 
 * You must call dispose to dispose fonts and colors when finished with object
 * 
 * @author brian
 * 
 */
public class BugnetApplicationItemDrawer {
	private static final int THUMBNAIL_WIDTH = 52;
	private static final int THUMBNAIL_HEIGHT = 39;
	private static final String SCALED_IMAGE_SUFFIX = "_scaled"; //$NON-NLS-1$
	private static final int LABEL_LENGTH = 16;
	private static final int CHAR_HEIGHT = 8;
	private static final int RATING_WIDTH_HINT = 50;
	private static final int RATING_HEIGHT_HINT = 15;
	private static final int DESCRIPTION_LENGTH = 15;
	private static final int USERNAMES_LENGTH = 21;
	private static final int LINE_WIDTH_HINT = 200;
	private static final int LINE_HEIGHT_HINT = 17;
	private static final int DESCRIPTION_TOOLTIP_MAX_LENGTH = 250;
	private static final int DOWNLOAD_BUTTON_WIDTH = 15;
	private static final int DOWNLOAD_BUTTON_HEIGHT = 15;
	private static final String DISPOSED_ERROR = "The resource has been disposed.";
	private static final String BUGNET_BROWSER_TITLE = "BUGnet";

	private List<Label> itemDescriptionLabels = new ArrayList();
	private List<String> itemDescriptions = new ArrayList();

	private Composite parent;
	private FormToolkit toolkit;
	// colors
	private static ColorRegistry colorRegistry;
	private static final String LIGHTGRAY1COLOR = "LIGHTGRAY1COLOR";
	private static final String LIGHTGRAY2COLOR = "LIGHTGRAY2COLOR";
	private static final String HYPERLINKCOLOR = "HYPERLINKCOLOR";
	private static final String USERNAMECOLOR = "USERNAMECOLOR";
	private static final String DESCRIPTIONCOLOR = "DESCRIPTIONCOLOR";
	private boolean isColor1;

	private ImageRegistry imageRegistry;

	// fonts
	private static FontRegistry fontRegistry;
	private static final String TITLEFONT = "TITLEFONT";
	private static final String DESCRIPTIONFONT = "DESCRIPTIONFONT";
	private static final String DOWNLOADTOSDK_TOOLTIP_TEXT = "Import to Dragonfly SDK";
	private static final String BUGBUTTON_TOOLTIP_TEXT = "Send to BUG";

	/**
	 * initialize fonts and everything
	 * 
	 * @param parent
	 *            should be a composite child of a form
	 */
	public BugnetApplicationItemDrawer(Composite parent) {
		this.parent = parent;
		toolkit = new FormToolkit(parent.getDisplay());
		imageRegistry = new ImageRegistry();
		initializeColors();
		initializeFonts();
		parent.setBackground(colorRegistry.get(LIGHTGRAY2COLOR));
		parent.addControlListener(new ResizeListener());
	}

	/**
	 * initialize all the colors used to draw an application item they are
	 * static so will only be initialized when the first item is created
	 * 
	 * Use a color registry so don't have to handle cleanup
	 */
	private void initializeColors() {
		if (colorRegistry == null)
			colorRegistry = new ColorRegistry(parent.getDisplay());

		if (!colorRegistry.hasValueFor(LIGHTGRAY1COLOR))
			colorRegistry.put(LIGHTGRAY1COLOR, new RGB(247, 247, 247));

		if (!colorRegistry.hasValueFor(LIGHTGRAY2COLOR))
			colorRegistry.put(LIGHTGRAY2COLOR, new RGB(255, 255, 255));

		if (!colorRegistry.hasValueFor(HYPERLINKCOLOR))
			colorRegistry.put(HYPERLINKCOLOR, new RGB(98, 83, 125));

		if (!colorRegistry.hasValueFor(USERNAMECOLOR))
			colorRegistry.put(USERNAMECOLOR, new RGB(123, 129, 138));

		if (!colorRegistry.hasValueFor(DESCRIPTIONCOLOR))
			colorRegistry.put(DESCRIPTIONCOLOR, new RGB(85, 85, 85));
	}

	/**
	 * initialize all the fonts used draw an application item using the
	 * fontRegistry
	 */
	private void initializeFonts() {
		if (fontRegistry == null)
			fontRegistry = new FontRegistry(parent.getDisplay());

		if (!fontRegistry.hasValueFor(TITLEFONT)) {
			FontData[] fdTitle = JFaceResources.getDefaultFont().getFontData();
			for (int i = 0; i < fdTitle.length; ++i) {
				fdTitle[i].setStyle(SWT.BOLD);
			}
			fontRegistry.put(TITLEFONT, fdTitle);
		}

		if (!fontRegistry.hasValueFor(DESCRIPTIONFONT)) {
			FontData[] fdDescription = JFaceResources.getDefaultFont().getFontData();
			for (int i = 0; i < fdDescription.length; ++i) {
				fdDescription[i].setHeight(CHAR_HEIGHT);
			}
			fontRegistry.put(DESCRIPTIONFONT, fdDescription);
		}
	}

	/**
	 * allows us to alternate colors for each item
	 * 
	 * @return the background color for this cycle
	 */
	private Color getBackgroundColor() {
		Color color = colorRegistry.get(LIGHTGRAY1COLOR);
		if (isColor1)
			color = colorRegistry.get(LIGHTGRAY2COLOR);
		isColor1 = !isColor1;
		return color;
	}

	/**
	 * creates a thumbnail for an app item
	 * 
	 * @param item
	 * @return
	 */
	private Image getThumbnailFor(BUGNetProgramReferenceNode item) {
		Image img = DragonflyActivator.getDefault().getImageRegistry().get(item.getImageKey());
		if (img == null) {
			img = Activator.getDefault().getImageRegistry().get(Activator.IMAGE_KEY_IMAGE_NOT_FOUND);
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
		if (imageRegistry.get(identifier) != null)
			imageRegistry.put(identifier, image);
	}

	/**
	 * Mac specific code to set the hyperlink background
	 * 
	 * @param hyperlink
	 */
	private void setHyperlinkBackgroundForMac(Hyperlink hyperlink) {
		Set s = System.getProperties().keySet();
		String osName = System.getProperty("os.name");

		if (osName != null && !osName.equals("Mac OS X")) {
			hyperlink.addPaintListener(new PaintListener() {

				public void paintControl(PaintEvent e) {
					((Hyperlink) e.widget).setBackground((Color) ((Map) e.widget.getData()).get("backgroundcolor"));
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
	 * This is the main public function. It draws a new BUGnet app in the parent
	 * composite set up in the constructor.
	 * 
	 * @param item
	 */
	public synchronized void draw(BUGNetProgramReferenceNode item) {
		Composite comp = toolkit.createComposite(parent, SWT.NONE);
		Color backgroundColor = getBackgroundColor();
		comp.setBackground(backgroundColor);
		GridLayout compLayout = new GridLayout(3, false);
		compLayout.verticalSpacing = 0;
		comp.setLayout(compLayout);
		GridData gd = GridDataFactory.fillDefaults().create();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		comp.setLayoutData(gd);

		// Get get the thumbnail image
		Image image = getThumbnailFor(item);
		putImageInRegistry(image, item.getImageKey() + SCALED_IMAGE_SUFFIX);

		// Draw thumbnail image
		Label imageLabel = toolkit.createLabel(comp, ""); //$NON-NLS-1$
		imageLabel.setImage(image);
		imageLabel.setBackground(backgroundColor);
		gd = GridDataFactory.fillDefaults().create();
		gd.verticalSpan = 3;
		imageLabel.setLayoutData(gd);

		// Draw the hyperlink title
		Hyperlink hyperlink = toolkit.createHyperlink(comp, UIUtils.truncateString(item.getLabel(), LABEL_LENGTH), SWT.NONE);
		hyperlink.setToolTipText(item.getLabel());
		hyperlink.addHyperlinkListener(new BUGNetHyperlinkListener(item));
		HashMap hyperLinkData = new HashMap();
		hyperLinkData.put("program", item);
		hyperLinkData.put("backgroundcolor", backgroundColor);
		hyperlink.setFont(fontRegistry.get(TITLEFONT));
		hyperlink.setBackground(backgroundColor);
		hyperlink.setForeground(colorRegistry.get(HYPERLINKCOLOR));
		gd = GridDataFactory.fillDefaults().create();
		gd.grabExcessHorizontalSpace = true;
		hyperlink.setLayoutData(gd);
		hyperlink.setBackgroundMode(SWT.INHERIT_NONE);
		hyperlink.setData(hyperLinkData);
		setHyperlinkBackgroundForMac(hyperlink);
		setupDragSource(hyperlink);

		// this has vertical span of 2
		drawDownloadButtons(comp, backgroundColor, item);

		// Draw the username
		gd = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
		Label lblUserName = new Label(comp, SWT.NONE);
		lblUserName.setText(UIUtils.truncateString(item.getUserName(), USERNAMES_LENGTH));
		lblUserName.setLayoutData(gd);
		lblUserName.setBackground(backgroundColor);
		lblUserName.setForeground(colorRegistry.get(USERNAMECOLOR));

		// Draw the download count & version number
		//GridData downloadsGD = GridDataFactory.fillDefaults().create();
		gd = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
		gd.horizontalSpan = 2;
		Label lblDownloads = new Label(comp, SWT.NONE);
		String apiStr = "";
		if (item.getAPIVersion() != null && item.getAPIVersion().length() > 0)
			apiStr = "API " + item.getAPIVersion() + " - ";
		lblDownloads.setText(apiStr + item.getDownload_count() + " downloads");
		lblDownloads.setLayoutData(gd);
		lblDownloads.setFont(fontRegistry.get(DESCRIPTIONFONT));
		lblDownloads.setBackground(backgroundColor);
		lblDownloads.setForeground(colorRegistry.get(USERNAMECOLOR));

		// Draw the rating
		Rating r = new Rating(comp, SWT.NONE);
		r.setBackground(backgroundColor);
		String ratingVal = item.getRating();
		if (ratingVal.length() == 0) {
			ratingVal = "0";
		}
		r.setRating(Double.parseDouble(ratingVal));
		gd = GridDataFactory.fillDefaults().create();
		gd.widthHint = RATING_WIDTH_HINT;
		gd.heightHint = RATING_HEIGHT_HINT;
		r.setLayoutData(gd);

		// Draw Description
		String desc = item.getDescription().trim();
		
		if (!desc.equals("")) { //$NON-NLS-1$
			String shortenedDesc = UIUtils.truncateString(desc, DESCRIPTION_LENGTH);
			Label descLabel = toolkit.createLabel(comp, shortenedDesc, SWT.NONE);
			gd = new GridData(SWT.BEGINNING, SWT.TOP, true, false);
			gd.horizontalSpan = 2;
			gd.widthHint = LINE_WIDTH_HINT;
			gd.heightHint = LINE_HEIGHT_HINT;
			// store the full description and the labels that hold 'em
			// so that they can be resized when view is resized
			itemDescriptionLabels.add(descLabel);
			itemDescriptions.add(desc);
			descLabel.setFont(fontRegistry.get(DESCRIPTIONFONT));
			descLabel.setLayoutData(gd);
			descLabel.setBackground(backgroundColor);
			descLabel.setForeground(colorRegistry.get(DESCRIPTIONCOLOR));
			descLabel.setToolTipText(UIUtils.truncateString(desc, DESCRIPTION_TOOLTIP_MAX_LENGTH ));
		}

		createContextMenu(comp, item);
	}

	private void drawDownloadButtons(Composite appbox, Color backgroundColor, final BUGNetProgramReferenceNode item) {
		Composite comp = toolkit.createComposite(appbox, SWT.NONE);
		comp.setBackground(new Color(appbox.getDisplay(), new RGB(255, 255, 255)));
		GridLayout compLayout = new GridLayout(1, false);
		compLayout.marginWidth = compLayout.marginHeight = 0;
		compLayout.horizontalSpacing = compLayout.verticalSpacing = 0;
		comp.setLayout(compLayout);
		GridData compgd = new GridData(SWT.END, SWT.TOP, false, false);
		compgd.verticalSpan = 2;
		comp.setLayoutData(compgd);

		ImageHyperlink h = toolkit.createImageHyperlink(comp, SWT.NONE);
		h.setImage(Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_DWNLD_SDK));
		h.setToolTipText(DOWNLOADTOSDK_TOOLTIP_TEXT);
		GridData gd = new GridData(SWT.BEGINNING, SWT.NONE, false, false);
		h.setLayoutData(gd);
		h.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				// TODO - this is chunky.  Need to refactor and use a delegate or job
				ImportFromBUGNetAction action = new ImportFromBUGNetAction();
				action.setUserName(item.getUserName());
				action.setProgramName(item.getLabel());
				action.run(); // run actually calls a job thread
			}
		});
	}

	/**
	 * This listener sits on the parent composite of the items we're drawing. It
	 * handles the reformating of the description text
	 */
	private class ResizeListener extends ControlAdapter {
		/**
		 * Shortens width of the description and adds ellipsis to it.
		 * 
		 * @param gc
		 * @param t
		 * @param width
		 * @return
		 * 
		 *         TODO: Need to polish this algorithm a bit, labels are not
		 *         being truncated in a unified manner.
		 */
		private String shortenText(GC gc, String t, int width) {
			int w = gc.textExtent(t).x; // width of the string

			if (t == null)
				return null;

			// shorten the string only if it's less than viewable area
			if (w > width) {
				String substring = ""; //$NON-NLS-1$
				try {
					// magic number is 16
					substring = t.substring(0, width - 16) + "..."; //$NON-NLS-1$
				} catch (StringIndexOutOfBoundsException e) {
					return t;
				}
				return substring;
			}
			return t;
		}

		public void controlResized(ControlEvent event) {
			int newWidth = parent.getSize().x / 6;
			Label descriptionLabel;
			GC gc;
			String shortenedText;
			for (int i = 0; i < itemDescriptionLabels.size(); i++) {
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
	 * This listener goes on the item hyperlink and allows you to drag stuff
	 * from the BugnetView
	 */
	private class HyperLinkDragSourceListener implements DragSourceListener {

		Hyperlink h;

		public HyperLinkDragSourceListener(Hyperlink h) {
			this.h = h;
		}

		// not used
		public void dragFinished(DragSourceEvent event) {
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

				PluginTransferData data = new PluginTransferData(BugNetToIDEActionDelegate.ACTION_ID, BugNETProgramReferenceNodeTransfer.getInstance().toByteArray(
						new BUGNetProgramReferenceNode[] { prog }));

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
				if (url == null)
					url = new URL(n.getUrl());

				LaunchBrowserAction action = new LaunchBrowserAction(url, BUGNET_BROWSER_TITLE);
				action.run();
			} catch (MalformedURLException e1) {
				UIUtils.handleVisualError("Invalid URL: " + n.getUrl(), e1); //$NON-NLS-1$
			}
		}

		// not used
		public void linkEntered(HyperlinkEvent e) {
		}

		public void linkExited(HyperlinkEvent e) {
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
}
