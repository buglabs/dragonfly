package com.buglabs.bug.simulator.ui;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A class to read from the stdin, send to knapsack, and print result.
 * 
 * @author kgilmer
 * 
 */
public class ShellIOThread extends Thread {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private static final long START_DELAY = 1000;
	private static final String LINE_FEED = "\n";
	private final int port;

	/**
	 * @param port
	 */
	public ShellIOThread(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		BufferedReader br = null;
		try {
			Thread.sleep(START_DELAY);
			br = new BufferedReader(new InputStreamReader(System.in));
			OutputStream os = System.out;
	
			String line = null;
		
			printPrompt(os);
			while (!Thread.interrupted() && (line = br.readLine()) != null) {
				line = line + LINE_FEED;
				run("localhost", port, new ByteArrayInputStream(line.getBytes()), os);
				os.flush();
				printPrompt(os);
			}
		} catch (IOException e) {

		} catch (InterruptedException e) {			
		} finally {
			try {
				if (br != null)
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

	public static void run(String host, int port, InputStream input, OutputStream output) throws UnknownHostException, IOException {
		// Create and open socket
		Socket socket = new Socket(host, port);
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();

		// Push, pull, close.
		copy(input, out);
		copy(in, output);
		socket.close();
	}

	public static int copy(InputStream input, OutputStream output) throws IOException {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	public static long copyLarge(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
