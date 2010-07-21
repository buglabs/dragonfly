package com.buglabs.bug.module.audio.pub;

import java.io.IOException;
import java.io.InputStream;

public interface IModuleAudioPlayer {
	public void play(InputStream is) throws IOException;

	public void pause();
	
	public void resume();
	
}
