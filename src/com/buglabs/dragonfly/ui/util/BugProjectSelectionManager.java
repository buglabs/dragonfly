package com.buglabs.dragonfly.ui.util;

/**
 * A singleton class to keep track of what project names
 * the user would like to run on the BUG
 * 
 * @author brian
 *
 */
public class BugProjectSelectionManager {
	
	private static BugProjectSelectionManager _instance = null;
	
	private String[] selected_project_names = null;
	
	protected BugProjectSelectionManager() {}
	
	public synchronized static BugProjectSelectionManager getInstance() {
		if(_instance == null) {
			_instance = new BugProjectSelectionManager();
		}
		return _instance;
	}
	
	public void setSelectedProjectNames(String[] bugProjectNames) {
		selected_project_names = bugProjectNames;
	}
	
	public String[] getSelectedProjectNames() {
		return selected_project_names;
	}
	
}
