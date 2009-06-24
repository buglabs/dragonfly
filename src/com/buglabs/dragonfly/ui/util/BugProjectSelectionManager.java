package com.buglabs.dragonfly.ui.util;

import java.util.Arrays;
import java.util.List;

/**
 * A singleton class to keep track of what project names
 * the user would like to run on the BUG
 * 
 * This helps keep state so that consecutive launches of the virtual
 * BUG remember the previous selections, making life easeir
 * 
 * @author brian
 *
 */
public class BugProjectSelectionManager {
	
	private static BugProjectSelectionManager _instance = null;
	
	private List<String> selected_project_names = null;
	
	protected BugProjectSelectionManager() {}
	
	public synchronized static BugProjectSelectionManager getInstance() {
		if(_instance == null) {
			_instance = new BugProjectSelectionManager();
		}
		return _instance;
	}
	
	public void setSelectedProjectNames(String[] bugProjectNames) {
		selected_project_names = Arrays.asList(bugProjectNames);
	}
	
	public String[] getSelectedProjectNames() {
		return (selected_project_names == null) ? null :
			selected_project_names.toArray(
					new String[selected_project_names.size()]);
	}
	
}
