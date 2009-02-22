package com.buglabs.dragonfly.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class BUGPostTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BUGPostTest pt = new BUGPostTest();
		
		try {
			pt.run();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void run() throws MalformedURLException, InterruptedException {
		File jarFile = new File("/home/kgilmer/dev/workspaces/runtime-New_configuration/.metadata/.plugins/com.buglabs.dragonfly.ui/BlueBack.jar");
		//File jarFile = new File("/tmp/infile.txt");
		if (!jarFile.exists()) {
			throw new RuntimeException("Boom");
		}
		
		URL url = new URL("http://127.0.0.1:8082/program/BlueBack");
		int failCount = 0;
		int runs = 100;
		for (int i = 0; i < runs; ++i) {
			try {
				BugWSHelper.upsertBundle(jarFile, url);
			} catch (IOException e) {
				failCount++;
				System.out.println("FAIL COUNT: " + failCount);
				e.printStackTrace();
			}
		}
		
		System.out.println("Total runs: " + runs + " Total failures: " + failCount);
	}

}
