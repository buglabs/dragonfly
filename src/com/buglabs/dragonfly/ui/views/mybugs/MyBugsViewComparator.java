package com.buglabs.dragonfly.ui.views.mybugs;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.buglabs.dragonfly.model.Module;

/**
 * Custom Comparator for elements of {@link MyBugsView}
 * @author akravets
 *
 */
public class MyBugsViewComparator extends ViewerComparator {

	public int compare(Viewer viewer, Object e1, Object e2) {
		// if we are dealing with modules compare modules' indexes
		if((e1 instanceof Module) && (e2 instanceof Module)){	
			Module m1 = (Module)e1;
			Module m2 = (Module)e2;
			
			return m1.getIndex() - m2.getIndex();
		}
		return super.compare(viewer, e1, e2);
	}
	
}
