package com.buglabs.bug.module.audio;

import java.io.IOException;
import java.io.InputStream;

import org.osgi.service.log.LogService;

import com.buglabs.bug.module.audio.pub.IModuleAudioPlayer;


/**
 * This is a stub audio player with no functionality.
 * @author kgilmer
 *
 */
public class NonworkingAudioPlayer implements IModuleAudioPlayer {
    
    LogService logService;
    
	public void endOfMediaReached() {
	    log(LogService.LOG_INFO, "endOfMediaReached()");	
	}

	public void pause() {
	    log(LogService.LOG_INFO, "paused");
	}

	public void play(InputStream is) throws IOException {
	    log(LogService.LOG_INFO, "play");
	}

	public void resume() {
	    log(LogService.LOG_INFO, "resume");
	}

	
   private void log(int level, String message) {
        if (logService == null) logService = AudioModlet.getLogService();
        if (logService == null) return;
        logService.log(level, this.getClass().getName() + ": " + message);
    }
}
