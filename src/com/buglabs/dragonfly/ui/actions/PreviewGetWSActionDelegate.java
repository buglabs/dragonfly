package com.buglabs.dragonfly.ui.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.buglabs.dragonfly.model.GetServiceNode;

/**
 * @author kgilmer
 * 
 */
public class PreviewGetWSActionDelegate implements IObjectActionDelegate {

	private GetServiceNode node;

	public PreviewGetWSActionDelegate() {

	}

	public void run(IAction action) {
		LaunchBrowserAction a;
		try {
			System.out.println(node.getUrl());
			a = new LaunchBrowserAction(new URL(node.getUrl()), node.getName() + " Web Service");
			a.run();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		IStructuredSelection s = (IStructuredSelection) selection;

		node = (GetServiceNode) s.getFirstElement();
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}

}
