/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.ui;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ModelNodeChangeEvent;
import com.buglabs.dragonfly.ui.actions.RefreshBugAction;
import com.buglabs.dragonfly.ui.views.mybugs.MyBugsView;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * A runnable that listens on a port and generates a ModelNodeChangeEvent when a
 * client connects. Allows local bug to notify IDE when a state change occurs.
 * 
 * @author ken
 * 
 */
public class SimpleHttpSever extends Thread {

	private final int port;

	private boolean running;
	
	private static final Object lock = new Object();
	
	public SimpleHttpSever(int port) {
		this.port = port;

	}

	public void run() {

		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e1) {
			handleException(e1);
		}
		
		running = true;
		Socket socket = null;
		while (running) {
			try {
				socket = serverSocket.accept();

				// Check to see if we got a poison pill. If so cleanup and exit.
				if (this.isInterrupted()) {
					running = false;
					continue;
				}

				try {
					OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
					out.write(" "); //$NON-NLS-1$
					out.flush();
					out.close();
					socket.close();
				} catch (IOException e) {
					Activator.getDefault().getLog().log(
							new Status(Status.INFO, Activator.getDefault().PLUGIN_ID, "Problem completing.", e));
				}
				refreshBugs();	
			} catch (IOException e) {
				handleException(e);
			} catch (CoreException e) {
				handleException(e);
			} catch(NullPointerException e) {
				handleException(e);
			}
		}

		if (!running) {
			try {
				if (serverSocket != null) serverSocket.close();
				if (socket != null) socket.close();
			} catch (IOException e) {
				handleException(e);
			}
		}
	}

	private void handleException(Exception e) {
		running = false;
		/*
		 *  may have to use non bundle-dependent logging as below
		 *  
		Activator.getDefault().getLog().log(
				new Status(Status.INFO, Activator.getDefault().PLUGIN_ID, "Problem completing.", e));
		*/
		UIUtils.handleNonvisualError(Messages.SimpleHttpSever_1 + port + Messages.SimpleHttpSever_2, e);
	}

	/**
	 * Iterate through all the Bugs in the model and run a refresh action.
	 * 
	 * @throws CoreException
	 * @throws MalformedURLException
	 */
	private void refreshBugs() throws CoreException, MalformedURLException {
		List children = (List) MyBugsView.getRoot().getChildren();
		Iterator iterator = children.iterator();

		while (iterator.hasNext()) {
			synchronized (lock) {
				BugConnection bug = ((BugConnection) iterator.next());
				ModelNodeChangeEvent event = new ModelNodeChangeEvent(this.getClass(), RefreshBugAction.REFRESH_BUG, bug);
				DragonflyActivator.getDefault().fireModelChangeEvent(event);
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
