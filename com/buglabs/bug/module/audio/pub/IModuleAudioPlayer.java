package com.buglabs.bug.module.audio.pub;

import java.io.IOException;
import java.io.InputStream;

/**
 * @deprecated This module is not supported in BUG 2.0 *
 */
public interface IModuleAudioPlayer {
	public void play(InputStream is) throws IOException;

	public void pause();
	
	public void resume();
	
}
