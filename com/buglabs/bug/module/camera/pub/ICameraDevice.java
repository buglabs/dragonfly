package com.buglabs.bug.module.camera.pub;

import java.awt.Rectangle;
import java.io.InputStream;

/**
 * An interface for a device that can return images.
 * 
 * @author kgilmer
 * 
 */
public interface ICameraDevice {
	public byte[] getImage();
	public byte[] getImage(int sizeX, int sizeY, int format, boolean highQuality);
	public boolean initOverlay(Rectangle pbounds);
	public boolean startOverlay();
	public boolean stopOverlay();
	public InputStream getImageInputStream();
	public String getFormat();	
}
