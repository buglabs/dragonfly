package com.buglabs.dragonfly.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.buglabs.dragonfly.ui.messages"; //$NON-NLS-1$

	public static String SimpleHttpSever_1;

	public static String SimpleHttpSever_2;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
