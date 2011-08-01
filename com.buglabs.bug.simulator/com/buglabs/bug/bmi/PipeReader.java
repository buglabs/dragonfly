/* Copyright (c) 2007, 2008 Bug Labs, Inc.
 * All rights reserved.
 *   
 * This program is free software; you can redistribute it and/or  
 * modify it under the terms of the GNU General Public License version  
 * 2 only, as published by the Free Software Foundation.   
 *   
 * This program is distributed in the hope that it will be useful, but  
 * WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  
 * General Public License version 2 for more details (a copy is  
 * included at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).   
 *   
 * You should have received a copy of the GNU General Public License  
 * version 2 along with this work; if not, write to the Free Software  
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  
 * 02110-1301 USA   
 *
 */
package com.buglabs.bug.bmi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.osgi.service.log.LogService;

import com.buglabs.bug.bmi.pub.Manager;

/**
 * This class listens to a pipe for events from Hotplug.  These events are passed to the BMIManager.
 * @author kgilmer
 *
 */
public class PipeReader extends Thread {
	private static final String POISON_PILL = "exit\n";

	private volatile FileInputStream ifs = null;

	private final String pipeFilename;

	private final Manager manager;

	private final LogService logService;

	public PipeReader(String pipeFilename, Manager manager, LogService logService) {
		this.pipeFilename = pipeFilename;
		this.manager = manager;
		this.logService = logService;
	}

	public void cancel() {

		try {
			FileOutputStream fos = new FileOutputStream(pipeFilename);
			fos.write(POISON_PILL.getBytes());
			fos.close();
		} catch (IOException e) {
		}

	}

	public void run() {



		while(!Thread.currentThread().isInterrupted()) {
			try {

				ifs = new FileInputStream(pipeFilename);

				int c = 0;

				StringBuffer sb = new StringBuffer();

				while((c = ifs.read()) != -1) {
					sb.append((char) c);
					if(c == '\n' || c == '\r') {
						if(sb.toString().equals(POISON_PILL)) {
							return;
						}
						if (logService != null) {
							logService.log(LogService.LOG_DEBUG, "Received message from event pipe: " + sb.toString());
						}

						manager.processMessage(sb.toString());
						break;
					}
				}
			} catch (FileNotFoundException e) {
				logService.log(LogService.LOG_ERROR, e.getMessage());
			} catch (IOException e) {
				logService.log(LogService.LOG_ERROR, e.getMessage());
			} finally {
				try {
					if (ifs != null) {
						ifs.close();
					}
				} catch (IOException e) {
				}
			}
		}
	}
}
