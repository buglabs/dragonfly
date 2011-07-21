package com.buglabs.dragonfly.ui.actions;

import org.eclipse.jface.action.IAction;

public class RefreshBugActionObjectDelegate extends BUGConnectionActionDelegate {

	public void run(IAction action) {
		new RefreshBugAction(getBugProjectNode()).run();
	}
}
