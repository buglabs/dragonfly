package com.buglabs.bug.simulator.ui;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.knapsack.shell.pub.Netcat;

/**
 * A class to read from the stdin, send to knapsack, and print result.
 * 
 * @author kgilmer
 *
 */
public class ShellIOThread extends Thread {

	private final int port;

	/**
	 * @param port
	 */
	public ShellIOThread(int port) {
		this.port = port;		
	}
	
	@Override
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));		
		OutputStream os = System.out;
		
		String line = null;
		try {
			printPrompt(os);
			while (!Thread.interrupted() && (line = br.readLine()) != null) {
				Netcat.run("localhost", port, new ByteArrayInputStream(line.getBytes()), os);
				os.flush();
				printPrompt(os);
			}
		} catch (IOException e) {
			
		} finally {
			try {
				br.close();
			} catch (IOException e) {				
			}
		}
	}
	
	/**
	 * @param os
	 * @throws IOException
	 */
	private void printPrompt(OutputStream os) throws IOException {
		os.write("\n(: ".getBytes());
	}

	/**
	 * Shutdown the thread
	 */
	public void shutdown() {
		this.interrupt();
		try {
			System.out.write("\n".getBytes());
		} catch (IOException e) {			
		}
	}

}
