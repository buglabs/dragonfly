package com.buglabs.bug.module.camera;

import java.io.IOException;

import org.osgi.service.log.LogService;

import com.buglabs.bug.module.camera.pub.ICamera2ModuleControl;
import com.buglabs.module.IModuleLEDController;

public class CameraModuleControl implements ICamera2ModuleControl, IModuleLEDController {

	private int slotId;
	private LogService logService;
	
	public CameraModuleControl(int slotId, LogService logService) {
		this.slotId = slotId;
		this.logService = logService;
	}
	
	public int getSelectedCamera() throws IOException {
		return slotId;
	}

	public int setFlashBeamIntensity(int intensity) throws IOException {
		logService.log(LogService.LOG_DEBUG, "called setFlashBeamIntensity(" + intensity + ")");
		return intensity;
	}

	public int setLEDFlash(boolean state) throws IOException {
		logService.log(LogService.LOG_INFO, "called setLEDFlash(" + state + ")");
		return 0;
	}

	public int setSelectedCamera(int slot) throws IOException {
		logService.log(LogService.LOG_INFO, "called setSelectedCamera(" + slot + ")");
		return 0;
	}

	public int setLEDGreen(boolean state) throws IOException {
		String message = "Green LED is off";
		if (state) message = "Green LED is on";
		logService.log(LogService.LOG_INFO, message);
		return 0;
	}

	public int setLEDRed(boolean state) throws IOException {
		String message = "Red LED is off";
		if (state) message = "Red LED is on";
		logService.log(LogService.LOG_INFO, message);
		return 0;
	}

	
	public int getTestPattern() {
		logService.log(LogService.LOG_INFO, "called getTestPattern()");
		return 0;
	}

	
	public int setTestPattern(int testPattern) {
		logService.log(LogService.LOG_INFO, "called setTestPattern(" + testPattern + ")");
		return 0;
	}

	
	public int getColorEffects() {
		logService.log(LogService.LOG_INFO, "called getColorEffects()");
		return 0;
	}

	
	public int setColorEffects(int colorEffects) {
		logService.log(LogService.LOG_INFO, "called setColorEffects(" + colorEffects + ")");
		return 0;
	}

	
	public int getVerticalFlip() {
		logService.log(LogService.LOG_INFO, "called getVerticalFlip()");
		return 0;
	}

	
	public int setVerticalFlip(int verticalFlip) {
		logService.log(LogService.LOG_INFO, "called setVerticalFlip(" + verticalFlip + ")");
		return 0;
	}

	
	public int getHorizontalMirror() {
		logService.log(LogService.LOG_INFO, "called getHorizontalMirror()");
		return 0;
	}

	
	public int setHorizontalMirror(int horizontalMirror) {
		logService.log(LogService.LOG_INFO, "called setHorizontalMirror(" + horizontalMirror + ")");
		return 0;
	}

	
	public int getExposureLevel() {
		logService.log(LogService.LOG_INFO, "called getExposureLevel()");
		return 0;
	}

	
	public int setExposureLevel(int exposureLevel) {
		logService.log(LogService.LOG_INFO, "called setExposureLevel(" + exposureLevel + ")");
		return 0;
	}

}
