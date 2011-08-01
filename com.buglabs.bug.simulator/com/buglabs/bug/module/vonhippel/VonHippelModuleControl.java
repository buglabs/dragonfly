/*******************************************************************************
 * Copyright (c) 2008, 2009 Bug Labs, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package com.buglabs.bug.module.vonhippel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.osgi.service.log.LogService;

import com.buglabs.bug.module.vonhippel.pub.IVonHippelModuleControl;
import com.buglabs.bug.module.vonhippel.pub.IVonHippelSerialPort;
import com.buglabs.bug.dragonfly.module.IModuleLEDController;

public class VonHippelModuleControl implements IVonHippelModuleControl, IModuleLEDController, IVonHippelSerialPort {

	//private CommConnection cc;
	private static final String NULL_STREAM_PATH = "/dev/null";
	private InputStream inputStream;
	private OutputStream outputStream;

	private String streamFilePath;
	private String baudrate;
	private int bitsPerChar;
	private String stopBits;
	private String parity;
	private boolean autoCTS;
	private boolean autoRTS;
	private boolean blocking;
	
	private LogService logService;

	public VonHippelModuleControl(LogService logService, String streamFilePath) {
		this.logService = logService;
		this.streamFilePath = streamFilePath;
		// load defaults for comm port.
		baudrate = "9600";
		bitsPerChar = 8;
		stopBits = "1";
		parity = "none";
		autoCTS = false;
		autoRTS = false;
		blocking = false;
		
	}

	/**
	 * Close input and output streams.  
	 */
	protected void dispose() {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
			
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (IOException e) {
			//Disregard exception
		}
	}

	public int setLEDGreen(boolean state) throws IOException {
		if(state)
			logService.log(LogService.LOG_INFO, "Green LED is on");
		else
			logService.log(LogService.LOG_INFO, "Green LED is off");
		return 0;
	}

	public int setLEDRed(boolean state) throws IOException {
		if(state)
			logService.log(LogService.LOG_INFO, "Red LED is on");
		else
			logService.log(LogService.LOG_INFO, "Red LED is off");
		return 0;
	}

	public void clearGPIO(int pin) throws IOException {
		logService.log(LogService.LOG_DEBUG, "called clearGPIO(" + pin + ")");
	}

	public void clearIOX(int pin) throws IOException {
		logService.log(LogService.LOG_DEBUG, "called clearIOX(" + pin + ")");
	}

	

	public int getRDACResistance() throws IOException {
		logService.log(LogService.LOG_DEBUG, "called getRDACResistance()");
		return 0;
	}

	public int getStatus() throws IOException {
		logService.log(LogService.LOG_DEBUG, "called getStatus()");
		return 0;
	}

	public void makeGPIOIn(int pin) throws IOException {
		logService.log(LogService.LOG_DEBUG, "called makeGPIOIn(" + pin + ")");
		throw new IOException("VonHippelModlet.getRDACResistance() is not yet implemented");
	}

	public void makeGPIOOut(int pin) throws IOException {
		logService.log(LogService.LOG_DEBUG, "called makeGPIOOut(" + pin + ")");
	}

	public void makeIOXIn(int pin) throws IOException {
		logService.log(LogService.LOG_DEBUG, "called makeIOXIn(" + pin + ")");
	}

	public void makeIOXOut(int pin) {
		logService.log(LogService.LOG_DEBUG, "called makeIOXOut(" + pin + ")");
	}
	
	public void writeADC(int control) throws IOException {
		logService.log(LogService.LOG_DEBUG, "called writeADC(int control)");		
	}

	public void writeDAC(int digital) throws IOException {
		logService.log(LogService.LOG_DEBUG, "called writeDAC(int digital)");		
	}

	public int readADC() throws IOException {
		logService.log(LogService.LOG_DEBUG, "called readADC()");
		return 0;
	}

	public int readDAC(int control) throws IOException {
		if (control == IVonHippelModuleControl.VH_DAC_W1_RDA){
			logService.log(LogService.LOG_DEBUG, "called readDAC(CHANNEL A)");
		}
		else if( control == IVonHippelModuleControl.VH_DAC_W1_RDB){
			logService.log(LogService.LOG_DEBUG, "called readDAC(CHANNEL A)");
		}
		else
			throw new IOException("Incorrect call to readDAC.  Param must be either IVonHippelModuleControl.VH_DAC_W1_RDA or"+
					"IVonHippelModuleControl.VH_DAC_W1_RDA");
		return 0;
	}

	public void setGPIO(int pin) {
		logService.log(LogService.LOG_DEBUG, "called setGPIO()");
		
	}

	public void setIOX(int pin) {
		logService.log(LogService.LOG_DEBUG, "called setIOX()");
	}

	public void setRDACResistance(int resistance) throws IOException {
		logService.log(LogService.LOG_DEBUG, "called setRDACResistance(" + resistance + ")");
		throw new IOException("VonHippelModlet.setRDACResistance(int resistance) is not yet implemented");
	}

	public int LEDGreenOff() throws IOException {
		return setLEDGreen(false);
	}

	public int LEDGreenOn() throws IOException {
		return setLEDGreen(true);
	}

	public int LEDRedOff() throws IOException {
		return setLEDRed(false);
	}

	public int LEDRedOn() throws IOException {
		return setLEDRed(true);
	}
	
	public InputStream getRS232InputStream() {
		try {
			inputStream = getSerialInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return inputStream;
	}

	public OutputStream getRS232OutputStream() {
		try {
			outputStream = getSerialOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return outputStream;
	}
	

	public InputStream getSerialInputStream() throws IOException {
		/*
		if (cc == null) {
			cc = (CommConnection) Connector.open(getCommString(), Connector.READ_WRITE, true);
		}
		if (inputStream == null) {
			inputStream = cc.openInputStream();
		}
		return inputStream;
		*/
		if (!isInputStreamOpen()) {
			String istreamPath = getCommString();
			if (istreamPath == null || istreamPath.length() == 0)
				istreamPath = NULL_STREAM_PATH;
			// default to stdin
			if (istreamPath != null && istreamPath.trim().length() > 0) {
				try {
					inputStream = new FileInputStream(istreamPath);
				} catch (FileNotFoundException e) {
					logService.log(LogService.LOG_INFO, 
							"Input Stream source " + istreamPath + " not found.");
				}
			}
		}
		return inputStream;	
	}
	
	public OutputStream getSerialOutputStream() throws IOException {
		/*
		try {
			if (cc == null) {
				cc = (CommConnection) Connector.open(getCommString(), Connector.READ_WRITE, true);
			}
			if (outputStream == null) {
				outputStream = cc.openOutputStream();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputStream;
		*/
		if (!isOutputStreamOpen()) {
			String ostreamPath = getCommString();
			if (ostreamPath == null || ostreamPath.length() == 0)
				ostreamPath = NULL_STREAM_PATH;
			// default to stdin
			if (ostreamPath != null && ostreamPath.trim().length() > 0) {
				try {
					outputStream = new FileOutputStream(ostreamPath);
				} catch (FileNotFoundException e) {
					logService.log(LogService.LOG_INFO, 
							"Output Stream source " + ostreamPath + " not found.");
				}
			}
		}
		return outputStream;		
		
	}

	/**
	 * @return
	 */
	private String getCommString() {
		// return "comm:/dev/ttymxc/" + slotId +
		// ";baudrate=9600;bitsperchar=8;stopbits=1;parity=none;autocts=off;autorts=off;blocking=off";
		//return "comm:/dev/ttymxc/" + slotId + ";baudrate=" + baudrate + ";bitsperchar=" + bitsPerChar + ";stopbits=" + stopBits + ";parity=" + parity + ";autocts=" + boolToStr(autoCTS)
		//		+ ";autorts=" + boolToStr(autoRTS) + ";blocking=" + boolToStr(blocking);
		/*
		return getCommSchemeAndTarget() + ";baudrate=" + baudrate + ";bitsperchar=" + bitsPerChar + ";stopbits=" + stopBits + ";parity=" + parity + ";autocts=" + boolToStr(autoCTS)
				+ ";autorts=" + boolToStr(autoRTS) + ";blocking=" + boolToStr(blocking);
				*/
		return streamFilePath;
	}


	/**
	 * @param val
	 * @return comm string friendly formatting of boolean value
	 */
	private String boolToStr(boolean val) {
		if (val) {
			return "on";
		}
		
		return "off";
	}
	
	private void checkOpen() throws IOException {
		if (isInputStreamOpen() || isOutputStreamOpen())
			throw new IOException("Serial port connection has already been created.  Unable to set parameters.");
	}

	public String getBaudrate() {
		return baudrate;
	}

	public void setBaudrate(String baudrate) throws IOException {
		checkOpen();
		this.baudrate = baudrate;
	}

	public int getBitsPerChar() {
		return bitsPerChar;
	}
	public String getStopBits() {
		return stopBits;
	}

	public void setStopBits(String stopBits) throws IOException {
		checkOpen();
		this.stopBits = stopBits;
	}

	public String getParity() {
		return parity;
	}

	public void setParity(String parity) throws IOException {
		checkOpen();
		this.parity = parity;
	}

	public boolean getAutoCTS() {
		return autoCTS;
	}

	public void setAutoCTS(boolean autoCTS) throws IOException {
		checkOpen();
		this.autoCTS = autoCTS;
	}

	public boolean getAutoRTS() {
		return autoRTS;
	}

	public void setAutoRTS(boolean autoRTS) throws IOException {
		checkOpen();
		this.autoRTS = autoRTS;
	}

	public boolean getBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) throws IOException {
		checkOpen();
		this.blocking = blocking;
	}

	public boolean isInputStreamOpen() {
		return inputStream != null;
	}

	public boolean isOutputStreamOpen() {
		return outputStream != null;
	}

	public void setBitsPerChar(int bitsPerChar) throws IOException {
		checkOpen();
		this.bitsPerChar = bitsPerChar;
	}


}
