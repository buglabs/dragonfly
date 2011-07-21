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
package com.buglabs.bug.jni.common;

/**
 * Stub implementation of CharDevice that throws RuntimeExceptions.  For design-time classpath resolution.
 * @author kgilmer
 *
 */
public class CharDevice {

	public int getFileDescriptor() {
		throw new RuntimeException(this.getClass().getName() + " is unimplemented in the BUG Simulator");
	}

	/**
	 * Open a file.
	 * @param file
	 * @param mode Refer to FCNTL_H class for constants to be passed in.
	 * @return negative value on failure.
	 */
	public  int open(String file, int mode) {
		throw new RuntimeException(this.getClass().getName() + " is unimplemented in the BUG Simulator");
	}

	/**
	 * @return a full line from the file.
	 */
	public  String getline() {
		throw new RuntimeException(this.getClass().getName() + " is unimplemented in the BUG Simulator");
	}

	/**
	 * @return a byte as int from the file.
	 */
	public  int read() {
		throw new RuntimeException(this.getClass().getName() + " is unimplemented in the BUG Simulator");
	}

	/**
	 * @param b
	 * @return number of bytes read.
	 */
	public int read(byte[] b) {
		return readBytes(b);
	}

	/**
	 * There's a bug somewhere (gcc, libc, dlopen, or jvm) where overloaded
	 * functions don't map well through jni. Therefore, the function overloading
	 * is at the java layer and renamed the  method to readBytes
	 */
	private  int readBytes(byte[] b) {
		throw new RuntimeException(this.getClass().getName() + " is unimplemented in the BUG Simulator");
	}

	/**
	 * @param offset
	 * @param whence
	 * @return
	 */
	public  long lseek(long offset, int whence) {
		throw new RuntimeException(this.getClass().getName() + " is unimplemented in the BUG Simulator");
	}

	public  long write(byte[] buf, long count) {
		throw new RuntimeException(this.getClass().getName() + " is unimplemented in the BUG Simulator");
	}

	/**
	 * @param request
	 * @return
	 */
	public  int ioctl(int request) {
		throw new RuntimeException(this.getClass().getName() + " is unimplemented in the BUG Simulator");
	}

	/**
	 * @return
	 */
	public  int close() {
		throw new RuntimeException(this.getClass().getName() + " is unimplemented in the BUG Simulator");
	}
}
