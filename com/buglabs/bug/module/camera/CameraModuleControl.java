package com.buglabs.bug.module.camera;

import java.io.IOException;

import org.osgi.service.log.LogService;

import com.buglabs.bug.module.camera.pub.ICameraModuleControl;
import com.buglabs.module.IModuleLEDController;

public class CameraModuleControl implements ICameraModuleControl, IModuleLEDController {

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

}
