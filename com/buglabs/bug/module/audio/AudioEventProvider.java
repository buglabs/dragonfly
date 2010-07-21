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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.buglabs.bug.module.audio.pub.IAudioEventProvider;
import com.buglabs.device.ButtonEvent;
import com.buglabs.device.IButtonEventListener;

public class AudioEventProvider implements IAudioEventProvider, KeyListener {
    
    private List buttonEventListeners;
    
    public AudioEventProvider() {
        buttonEventListeners = new ArrayList();
    }
    
    public synchronized void addListener(IButtonEventListener listener) {
        if (!buttonEventListeners.contains(listener)) {
            buttonEventListeners.add(listener);
        }
    }

    public synchronized void removeListener(IButtonEventListener listener) {
        buttonEventListeners.remove(listener);
    }

    private void fireEvent(ButtonEvent event) {
        Iterator iter = buttonEventListeners.iterator();

        while (iter.hasNext()) {
            IButtonEventListener listener = (IButtonEventListener) iter.next();
            listener.buttonEvent(event);
        }
    }

    /**
     * 
     * Press + for volume up, 
     * Press - for volume down
     * 
     */
    public void keyPressed(KeyEvent event) {
        char value = (char) event.getKeyChar();
        switch (value) {
        case '+':
            fireEvent( new ButtonEvent(
                                ButtonEvent.BUTTON_AUDIO, 
                                0, 
                                ButtonEvent.BUTTON_AUDIO_VOLDOWN_PRESSED_ACTION));
            break;
        case '-':
            fireEvent( new ButtonEvent(
                                ButtonEvent.BUTTON_AUDIO, 
                                0, 
                                ButtonEvent.BUTTON_AUDIO_VOLDOWN_PRESSED_ACTION));
            break;
        default:
            break;
        }
    }

    // unused key listener methods
    public void keyReleased(KeyEvent arg0) {}
    public void keyTyped(KeyEvent arg0) {}
}