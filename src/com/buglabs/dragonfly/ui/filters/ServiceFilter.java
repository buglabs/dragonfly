package com.buglabs.dragonfly.ui.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * 

 *
 */
public class ServiceFilter extends ViewerFilter {

	String pattern;
	List checkedElements;

	public ServiceFilter() {
		pattern = ".*";
		checkedElements = new ArrayList();
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {

		if (element instanceof String && !checkedElements.contains(element)) {
			if (!((String) element).matches(pattern)) {
				return false;
			}
		}

		return true;
	}

	public void setPattern(String pattern, List checkedElements) {
		this.pattern = pattern;
		this.checkedElements = checkedElements;
	}
}
