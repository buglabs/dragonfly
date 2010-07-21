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
package com.buglabs.bug.module.audio;

import java.io.IOException;
import org.osgi.service.log.LogService;
import com.buglabs.bug.module.audio.pub.IAudioModuleControl;
import com.buglabs.module.IModuleLEDController;

/**
 * Current implementation of AudioModuleControl just prints log information
 * 
 * @author brian
 *
 */
public class AudioModuleControl implements IAudioModuleControl, IModuleLEDController {

    
	public int LEDGreenOff() throws IOException {
		log(LogService.LOG_INFO, "Green LED is off");
		return 0;
	}

	public int LEDGreenOn() throws IOException {
	    log(LogService.LOG_INFO, "Green LED is on");
		return 0;
	}

	public int LEDRedOff() throws IOException {
	    log(LogService.LOG_INFO, "Red LED is off");
		return 0;
	}

	public int LEDRedOn() throws IOException {
	    log(LogService.LOG_INFO, "Red LED is on");
		return 0;
	}

	public int activate() throws IOException {
	    log(LogService.LOG_INFO, "activate() called");
		return 0;
	}

	public int deactivate() throws IOException {
	    log(LogService.LOG_INFO, "deactivate() called");
		return 0;
	}

	public int getStatus() throws IOException {
	    log(LogService.LOG_DEBUG, "getStatus() called");
		return 0;
	}

	public int setActive(boolean state) throws IOException {
		if (state)
		    activate();
		else
		    deactivate();
		return 0;
	}

	public int setLEDGreen(boolean state) throws IOException {
        if(state)
            LEDGreenOn();
        else
            LEDGreenOff();
		return 0;
	}

	public int setLEDRed(boolean state) throws IOException {
        if(state)
            LEDRedOff();
        else
            LEDRedOn();
        return 0;
	}

	public int setSpeaker(boolean state) throws IOException {
	    if (state)
	        speakerOn();
	    else
	        speakerOff();
		return 0;
	}

	public int speakerOff() throws IOException {
	    log(LogService.LOG_INFO, "Speaker is off");
		return 0;
	}

	public int speakerOn() throws IOException {
	    log(LogService.LOG_INFO, "Speaker is on");
	    return 0;
	}

	private void log(int level, String message) {
	    if (AudioModlet.getLogService() == null) return;
	    AudioModlet.getLogService()
	        .log(level, this.getClass().getName() + ": " + message);
	}

	
}
