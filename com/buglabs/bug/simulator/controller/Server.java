package com.buglabs.bug.simulator.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * Server of module/LED controller.  This server talks with the SDK to load graphical views on the BUG Simulator's "physical" state.
 * 
 * @author kgilmer
 * 
 */
public class Server extends Thread {

	private static final int REQUEST_LOOP_SLEEP_INTERVAL_MILLIS = 100;
	private static final String LOCAL_HOSTNAME = "localhost";
	private static Server ref;
	private ServerSocket serverSocket;
	private final LogService log;
	private int port;
	private final BundleContext context;

	private Server(int port, LogService log, BundleContext context) throws IOException {
		this.log = log;
		this.context = context;
		serverSocket = new ServerSocket(port);
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(REQUEST_LOOP_SLEEP_INTERVAL_MILLIS);
				if (Thread.interrupted()) {
					return;
				}

				Socket clientSocket = serverSocket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String inputLine, outputLine;
				ControllerProtocol protocol = new ControllerProtocol(context);

				while (Thread.interrupted() == false && (inputLine = in.readLine()) != null) {
					try {
						outputLine = protocol.processInput(inputLine);

						if (outputLine.equals(ControllerProtocol.CMD_GOODBYE)) {
							clientSocket.close();
							break;
						}

						out.println(outputLine.trim());
					} catch (Exception e) {
						out.println(ControllerProtocol.CMD_ERROR_RESPONSE);
					}
				}
			} catch (InterruptedException e) {
				log.log(LogService.LOG_INFO, "Controller server has been shutdown.");
				return;
			} catch (IOException e) {
				log.log(LogService.LOG_ERROR, "An IO error occurred in the controller server.", e);
			}
		}
	}

	public static Server getServer(int port, LogService log, BundleContext context) throws IOException {
		if (ref == null) {
			ref = new Server(port, log, context);
			ref.start();
		}

		return ref;
	}

	public void shutdown() {
		ref.interrupt();
		try {
			Socket s = new Socket(LOCAL_HOSTNAME, port);
			s.getOutputStream().write((ControllerProtocol.CMD_GOODBYE + "\n").getBytes());
			s.close();
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
	}
}
