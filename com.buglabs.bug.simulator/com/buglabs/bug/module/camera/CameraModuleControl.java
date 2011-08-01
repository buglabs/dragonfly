package com.buglabs.bug.module.camera;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.osgi.service.log.LogService;

import com.buglabs.bug.dragonfly.module.IModuleLEDController;
import com.buglabs.bug.module.camera.pub.ICamera2ModuleControl;
import com.buglabs.bug.module.camera.pub.ICameraModuleControl;
import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider2;
import com.buglabs.services.ws.PublicWSProviderWithParams;
import com.buglabs.services.ws.WSResponse;
import com.buglabs.util.xml.XmlNode;

public class CameraModuleControl implements ICameraModuleControl, ICamera2ModuleControl, IModuleLEDController, PublicWSProviderWithParams {
	private int slotId;
	private LogService logService;
	private final CameraModlet cameraModlet;
	
	public CameraModuleControl(int slotId, CameraModlet cameraModlet, LogService logService) {
		this.slotId = slotId;
		this.cameraModlet = cameraModlet;
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
	
	// For WS:
	private String serviceName = "CameraControl";
	public void setPublicName(String name) {
		serviceName = name;
	}

	public PublicWSDefinition discover(int operation) {
		if (operation == PublicWSProvider2.GET) {
			return new PublicWSDefinition() {

				public List getParameters() {
					return null;
				}

				public String getReturnType() {
					return "text/xml";
				}
			};
		}

		return null;
	}

	public IWSResponse execute(int operation, String input) {
		// not executed; we use the one below
		return null;
	}
	
	public IWSResponse execute(int operation, String input, Map get, Map post) {
		if (operation == PublicWSProvider2.GET) {
			// allow setting via http://bugip/service/CameraControl?testPattern=2 etc

			String v = (String) get.get("flash");
			if (v != null) {
				final int i = Integer.parseInt(v);
				System.out.println("setting flash " + ((i == 1) ? "ON" : "OFF"));
				try {
					setLEDFlash((i == 1) ? true : false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			v = (String) get.get("flashIntensity");
			if (v != null) {
				System.out.println("setting flash intensity to " + v);
				try {
					this.setFlashBeamIntensity(Integer.parseInt(v));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (get.containsKey("cameraOpen")) {
				System.out.println("opening camera");
			}

			v = (String) get.get("testPattern");
			if (v != null) {
				System.out.println("Setting test pattern to " + v);
				setTestPattern(Integer.parseInt(v));
			}
			v = (String) get.get("colorEffects");
			if (v != null) {
				System.out.println("Setting color effects to " + v);
				setColorEffects(Integer.parseInt(v));
			}
			v = (String) get.get("verticalFlip");
			if (v != null) {
				System.out.println("Setting vertical flip to " + v);
				setVerticalFlip(Integer.parseInt(v));
			}
			v = (String) get.get("horizontalMirror");
			if (v != null) {
				System.out.println("Setting horizontal mirror to " + v);
				setHorizontalMirror(Integer.parseInt(v));
			}
			v = (String) get.get("exposureLevel");
			if (v != null) {
				System.out.println("Setting exposure level to " + v);
				setExposureLevel(Integer.parseInt(v));
			}
			
			if (get.containsKey("cameraStart")) {
				System.out.println("starting camera");
			}
			
			if (get.containsKey("cameraStop")) {
				System.out.println("stopping camera");
			}
			
			if (get.containsKey("cameraClose")) {
				System.out.println("closing camera");
			}
			
			return new WSResponse(getCameraInfoXml(), "text/xml");
		}
		return null;
	}

	public String getPublicName() {
		return serviceName;
	}

	public String getDescription() {
		return "Returns information about the Camera. " +
			"Allows Camera settings to be changed via GET parameters: " +
			"flash=0/1 - turn flash off/on; " +
			"flashIntensity=0/1 - set flash beam to low/high; " +
			"cameraOpen/Close/Start/Stop - open/close/start/stop the camera; " +
			// turning off test pattern seems to go a bit awry, so let's not
			// mention that we support this
			// "testPattern=0/1 - set test pattern off/on; " +
			"colorEffects=0/1/2/34 - set effect to none/black-and-white/sepia/negative/solarize; " +
			"verticalFlip=0/1 - set vertical flip off/on; " +
			"horizontalMirror=0/1 - set mirroring off/on; " +
			"exposureLevel=0..255 - set exposure level.";
	}

	private String getCameraInfoXml() {
		XmlNode root = new XmlNode("CameraInfo");

		if (cameraModlet.isCameraOpen()) {
			if (cameraModlet.isCameraStarted()) {
				root.addChild(new XmlNode("CameraInfo", "Camera is open and started"));
			} else {
				root.addChild(new XmlNode("CameraInfo", "Camera is open but not yet started"));
			}
		} else {
			root.addChild(new XmlNode("CameraInfo", "Camera is not open."));
		}
		
		root.addChild(new XmlNode("TestPattern", testPatternString(getTestPattern())));
		root.addChild(new XmlNode("ColorEffects", colorEffectsString(getColorEffects())));
		root.addChild(new XmlNode("VerticalFlip", positiveValueOkayString(getVerticalFlip())));
		root.addChild(new XmlNode("HorizontalMirror", positiveValueOkayString(getHorizontalMirror())));
		root.addChild(new XmlNode("ExposureLevel", positiveValueOkayString(getExposureLevel())));
		root.addChild(new XmlNode("FullFramesTaken", "0"));
		root.addChild(new XmlNode("PreviewsTaken", "0"));
		
		return root.toString();
	}
	
	private static String testPatternString(int testPattern) {
		switch (testPattern) {
		case 0: return "Disabled";
		case 1: return "Walking 1s";
		case 2: return "Solid White";
		case 3: return "Grey Ramp";
		case 4: return "Color Bars";
		case 5: return "Black/White Bars";
		case 6: return "PseudoRandom";
		default: return "ERROR: " + testPattern;
		}
	}
	
	private static String colorEffectsString(int colorEffects) {
		switch (colorEffects) {
		case 0: return "Disabled";
		case 1: return "Black and White";
		case 2: return "Sepia";
		case 3: return "Negative";
		case 4: return "Solarize";
		default: return "ERROR: " + colorEffects;
		}
	}
	
	private static String positiveValueOkayString(int value) {
		if (value < 0) {
			return "ERROR: " + value;
		} else {
			return Integer.toString(value);
		}
	}

}
