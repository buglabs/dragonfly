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
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.core.runtime.Status;

import com.buglabs.dragonfly.BugConnectionManager;

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
	private ServerSocket serverSocket;
	private Socket socket;

	public SimpleHttpSever(int port) {
		this.port = port;

	}

	public void run() {

		serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e1) {
			handleException(e1);
			return;
		}

		running = true;
		socket = null;
		while (running) {
			try {
				socket = serverSocket.accept();

				// Check to see if we got a poison pill. If so cleanup and exit.
				if (this.isInterrupted()) {
					running = false;
					resetSockets();
					return;
				}

				try {
					OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
					out.write(" "); //$NON-NLS-1$
					out.flush();
					out.close();
					socket.close();
				} catch (IOException e) {
					Activator.getDefault().getLog().log(new Status(Status.INFO, Activator.getDefault().PLUGIN_ID, "Problem completing.", e));
				}
				BugConnectionManager.getInstance().refreshBugConnections();
			} catch (IOException e) {
				handleException(e);
			} catch (NullPointerException e) {
				handleException(e);
			}
		}

		if (!running) {
			resetSockets();
		}
	}

	private void handleException(Exception e) {
		Activator.getDefault().getLog().log(new Status(Status.WARNING, Activator.getDefault().PLUGIN_ID, Messages.SimpleHttpSever_1 + port + Messages.SimpleHttpSever_2, e));
		running = false;
		resetSockets();
	}

	private void resetSockets() {
		try {
			if (serverSocket != null)
				serverSocket.close();
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(Status.INFO, Activator.getDefault().PLUGIN_ID, "Problem cleaning up SimpleHttpServer sockets", e));
		} finally {
			serverSocket = null;
			socket = null;
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
