package com.buglabs.dragonfly.util;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.model.LDBugConnection;

/**
 * Local discovery type listener
 * 
 * @author akravets
 * 
 */
public class LDBugListener extends BugListener implements Runnable {

	private Socket socket;

	private String httpPort = DragonflyActivator.getDefault().getHttpPort();

	public LDBugListener(ITreeNode root, List model) {
		super(root, model);
	}

	protected List getBugs() {
		List bugs = Collections.synchronizedList(new ArrayList());
		try {
			System.out.println("PORT = " + httpPort);
			socket = new Socket("localhost", Integer.parseInt(httpPort));
			BugConnection bug = new LDBugConnection("localhost", new URL("http://localhost:" + httpPort));
			bugs.add(bug);
		} catch (UnknownHostException e) {
			return bugs;
		} catch (IOException e) {
			return bugs;
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				UIUtils.handleNonvisualError("Unable to close connection", e);
			}
		}
		return bugs;
	}

	protected boolean isValidType(Object o) {
		return o instanceof LDBugConnection;
	}
}
