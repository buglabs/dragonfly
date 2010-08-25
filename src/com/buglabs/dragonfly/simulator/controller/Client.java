package com.buglabs.dragonfly.simulator.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client controller for BUG Simulator.  Allows for the Modules and LEDs to be remote controlled from the SDK.
 * @author kgilmer
 *
 */
public class Client {
	
	public static final String CMD_ERROR_RESPONSE = "ER";
	public static final String CMD_SUCCESS_RESPONSE = "OK";
	
	public static final String CMD_CONFIGURE_MODULE = "CM";
	public static final String CMD_MODULE_LIST = "ML";
	public static final String CMD_GOODBYE = "GB";
	
	/*private Socket socket;
	private PrintWriter out;
	private BufferedReader in;*/
	private final String host;
	private final int port;

	private Client(String host, int port) throws UnknownHostException, IOException {
		this.host = host;
		this.port = port;
	}
	
	/**
	 * @return A list of all modules a given Simulated BUG can use.
	 * @throws IOException
	 */
	public List getAvailableModuleNames() throws IOException {
		List l = new ArrayList();
		
		String resp = getResponse(CMD_MODULE_LIST);
		
		String elems[] = resp.split(",");
		
		for (int i = 0; i < elems.length; ++i) {
			l.add(elems[i]);
		}
		
		return l;
	}
	
	/**
	 * Add specifid module to slot.
	 * @param module
	 * @param slot
	 * @return
	 * @throws IOException
	 */
	public boolean AttachModule(String module, int slot) throws IOException {
		String resp = getResponse(CMD_CONFIGURE_MODULE + " " + slot + " " + module.trim());
		
		return resp != null && resp.equals("OK");
	}
	
	/**
	 * Remove any module attached to the specified slot #.
	 * @param slot
	 * @return
	 * @throws IOException
	 */
	public boolean RemoveModuleFromSlot(int slot) throws IOException {
		String resp = getResponse(CMD_CONFIGURE_MODULE + " " + slot);
		
		return resp != null && resp.equals("OK");
	}
	
	private String getResponse(String cmd) throws IOException {
		Socket socket = new Socket(host, port);
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
		                            socket.getInputStream()));
		
		out.println(cmd);
		String response = in.readLine();
		socket.close();
		
		return response;
	}

	/**
	 * Terminate connection to server.
	 */
	public void dispose() {
		
	}
	
	/**
	 * Initiates a connection with BUG simulator controller server.  Throws exception on failure.
	 * @param host
	 * @param port
	 * @return
	 */
	public static Client getClient(String host, int port) throws IOException {
		return new Client(host, port);
	}
}
