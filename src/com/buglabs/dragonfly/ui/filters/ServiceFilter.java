package com.buglabs.dragonfly.ui.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ServiceFilter extends ViewerFilter {

	String pattern;

	public ServiceFilter() {
		pattern = ".*";
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {

		if (element instanceof String) {
			if (!((String) element).matches(pattern)) {
				return false;
			}
		}

		return true;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}
