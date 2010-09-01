package com.buglabs.bug.simulator.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

import com.buglabs.bug.base.pub.IBUG20BaseControl;
import com.buglabs.bug.sysfs.LEDDevice;

/**
 * Server of module/LED controller. This server talks with the SDK to load
 * graphical views on the BUG Simulator's "physical" state.
 * 
 * @author kgilmer
 * 
 */
public class Server extends Thread implements IBUG20BaseControl {

	private static final int REQUEST_LOOP_SLEEP_INTERVAL_MILLIS = 100;
	private static final String LOCAL_HOSTNAME = "localhost";
	private static Server ref;
	private ServerSocket serverSocket;
	private final LogService log;
	private final int port;
	private final BundleContext context;
	private LEDDevice batteryLED;
	private LEDDevice wlanLED;
	private LEDDevice powerLED;
	private LEDDevice btLED;

	/**
	 * Private constructor for Server.
	 * 
	 * @param port
	 * @param log
	 * @param context
	 * @throws IOException
	 */
	private Server(int port, LogService log, BundleContext context) throws IOException {
		this.port = port;
		this.log = log;
		this.context = context;

		batteryLED = new LEDDevice(null, "battery");
		wlanLED = new LEDDevice(null, "wifi");
		powerLED = new LEDDevice(null, "power", "blue");
		btLED = new LEDDevice(null, "bt", "blue");

		serverSocket = new ServerSocket(port);
	}

	@Override
	public void run() {
		boolean exit = false;
		try {
			while (!exit) {

				Thread.sleep(REQUEST_LOOP_SLEEP_INTERVAL_MILLIS);
				if (Thread.interrupted()) {
					System.out.println("+++ Server was interrupted, exiting.");
					exit = true;
					return;
				}

				Socket clientSocket = serverSocket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String inputLine, outputLine;
				ControllerProtocol protocol = new ControllerProtocol(context);

				inputLine = in.readLine();

				try {
					outputLine = protocol.processInput(inputLine);

					if (outputLine.equals(ControllerProtocol.CMD_GOODBYE)) {
						clientSocket.close();
						break;
					}

					out.println(outputLine.trim());

					out.close();
					clientSocket.close();
				} catch (Exception e) {
					out.println(ControllerProtocol.CMD_ERROR_RESPONSE);
				}
			}
		} catch (InterruptedException e) {
			log.log(LogService.LOG_INFO, "Controller server has been shutdown.");
		} catch (IOException e) {
			log.log(LogService.LOG_ERROR, "An IO error occurred in the controller server.", e);
		} finally {
			try {
				exit = true;
				serverSocket.close();
				serverSocket = null;
				log.log(LogService.LOG_INFO, "Shutdown of " + this.getClass().getName() + " complete.");
				return;
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Start the socket server that serves to give external clients the ability
	 * to configure a BUG simulator instance.
	 * 
	 * @param port
	 * @param log
	 * @param context
	 * @return
	 * @throws IOException
	 */
	public static Server startServer(int port, LogService log, BundleContext context) throws IOException {
		if (ref == null) {
			ref = new Server(port, log, context);
			ref.start();
		}

		return ref;
	}

	/**
	 * Shutdown the socket server.
	 */
	public void shutdown() {
		log.log(LogService.LOG_INFO, "Shutdown of " + this.getClass().getName() + " started.");
		ref.interrupt();
		try {
			Socket s = new Socket(LOCAL_HOSTNAME, port);
			s.getOutputStream().write((ControllerProtocol.CMD_GOODBYE + "\n").getBytes());
			s.close();
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * @param index
	 * @return the output stream for a given LED
	 * @throws IOException
	 */
	private LEDDevice getLEDDevice(int index) throws IOException {
		switch (index) {
		case 0:
			return batteryLED;
		case 1:
			return powerLED;
		case 2:
			return wlanLED;
		case 3:
			return btLED;
		default:
			throw new IOException("LED index out of bounds: " + index);
		}
	}

	public void setLEDColor(int led, int color, boolean on) throws IOException {
		getLEDDevice(led).setBrightness(color, 100);
	}

	public int getLEDBrightness(int led, int color) throws IOException {
		return getLEDDevice(led).getBrightness(color);
	}

	public void setLEDTrigger(int led, int color, String trigger) throws IOException {
		getLEDDevice(led).setTrigger(color, trigger);
	}

	public String getLEDTrigger(int led, int color) throws IOException {
		return getLEDDevice(led).getLEDTrigger(color);
	}

	public String[] getLEDTriggers(int led, int color) throws IOException {
		return getLEDDevice(led).getLEDTriggers(color);
	}

	public void setLEDBrightness(int led, int brightness) throws IOException {
		LEDDevice device = getLEDDevice(led);

		if (device.getType() == LEDDevice.TYPE_MONO_COLOR) {
			device.setBrightness(LEDDevice.COLOR_MONO, brightness);
		} else {
			device.setBrightness(LEDDevice.COLOR_BLUE, brightness);
			device.setBrightness(LEDDevice.COLOR_GREEN, brightness);
			device.setBrightness(LEDDevice.COLOR_RED, brightness);
		}
	}

	public List getLEDDevices() {
		return Arrays.asList(new LEDDevice[] { batteryLED, wlanLED, btLED, powerLED });
	}
}
