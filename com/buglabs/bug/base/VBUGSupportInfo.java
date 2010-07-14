package com.buglabs.bug.base;

import java.util.Dictionary;

import org.osgi.framework.BundleContext;

import com.buglabs.support.SupportInfo;

public class VBUGSupportInfo extends SupportInfo {

	private static final String VERSION_OUT_KEY = "Version: "; //expected by client to parse version
	private static final String VERSION_MANIFEST_KEY = "BUG-API-Version";

	Dictionary headers = null;

	public VBUGSupportInfo(BundleContext context) {
		super(context);
		headers = context.getBundle().getHeaders();
	}

	protected String getKernelVersion() {
		return "Virtual BUG (No Kernel)";
	}

	protected String getRootfsVersion() {
		String val = (String) headers.get(VERSION_MANIFEST_KEY);
		if (val != null)
			return VERSION_OUT_KEY + val;
		return "Virtual BUG (No RootFS Version)";
	}

}
