package com.buglabs.bug.module.vonhippel.pub;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IVonHippelSerialPort {
	
	/**
	 * NOTE - in Virtual BUG this returns a fileStream based on the configuration
	 * item - "IO Stream Path" - configuration properties are ignored
	 * 
	 * Gets the input stream associated with the RS232 port on Von Hippel
	 * module. This implementation is based on the javax.microedition.commports
	 * API. The port is set up with the following parameters in
	 * VonHippelModuleControl: baudrate=9600 bitsperchar=8 stopbits=1
	 * parity=none autocts=off autorts=off blocking=off
	 * 
	 * @return stream associated with RS232 input (reading)
	 */
	public InputStream getSerialInputStream() throws IOException;
	
	/**
	 * NOTE - in Virtual BUG this returns a fileStream based on the configuration
	 * item - "IO Stream Path" - configuration properties are ignored
	 * 
	 * Gets the output stream associated with the RS232 port on Von Hippel
	 * module. This implementation is based on the javax.microedition.commports
	 * API. The port is set up with the following parameters in
	 * VonHippelModuleControl: baudrate=9600 bitsperchar=8 stopbits=1
	 * parity=none autocts=off autorts=off blocking=off
	 * 
	 * @return stream associated with RS232 output (writing)
	 */
	public OutputStream getSerialOutputStream() throws IOException;
	
	/**
	 * @return true if a client has already opened the input stream.
	 */
	public boolean isInputStreamOpen();
	
	/**
	 * @return true if a client has already opened the output stream.
	 */
	public boolean isOutputStreamOpen();
	
	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @return baud rate setting for serial port.
	 */
	public String getBaudrate();

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * Set baud rate for serial port. 
	 * @param baudrate
	 * @throws IOException Is thrown if connection is already open.
	 */
	public void setBaudrate(String baudrate) throws IOException;

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @return Bits per char of serial connection.
	 */
	public int getBitsPerChar();

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @param bitsPerChar
	 * @throws IOException Is thrown if connection is already open.
	 */
	public void setBitsPerChar(int bitsPerChar) throws IOException;

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @return get stop bits of serial connection.
	 */
	public String getStopBits();

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @param stopBits
	 * @throws IOException Is thrown if connection is already open.
	 */
	public void setStopBits(String stopBits) throws IOException;

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @return
	 */
	public String getParity();

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @param parity
	 * @throws IOException Is thrown if connection is already open.
	 */
	public void setParity(String parity) throws IOException;

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @return
	 */
	public boolean getAutoCTS();

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @param autoCTS
	 * @throws IOException Is thrown if connection is already open.
	 */
	public void setAutoCTS(boolean autoCTS) throws IOException;

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @return Is thrown if connection is already open.
	 */
	public boolean getAutoRTS() ;

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @param autoRTS
	 * @throws IOException
	 */
	public void setAutoRTS(boolean autoRTS) throws IOException;

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @return
	 */
	public boolean getBlocking() ;

	/**
	 * Does Nothing in Virtual BUG
	 * 
	 * @param blocking
	 * @throws IOException
	 */
	public void setBlocking(boolean blocking) throws IOException;
}
