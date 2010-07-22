package com.buglabs.bug.module.audio.pub;

import java.io.IOException;

import com.buglabs.module.IModuleLEDController;

public interface IAudioModuleControl extends IModuleLEDController  {
	public int setLEDGreen(boolean state) throws IOException;

	public int setLEDRed(boolean state) throws IOException;

	public int setSpeaker(boolean state) throws IOException;

	public int speakerOff() throws IOException;

	public int speakerOn() throws IOException;

	public int getStatus() throws IOException;

	public int setActive(boolean state) throws IOException;

	public int activate() throws IOException;

	public int deactivate() throws IOException;
}
