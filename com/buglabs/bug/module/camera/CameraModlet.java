package com.buglabs.bug.module.camera;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

import com.buglabs.bug.module.camera.pub.ICameraButtonEventProvider;
import com.buglabs.bug.module.camera.pub.ICameraDevice;
import com.buglabs.bug.module.camera.pub.ICameraModuleControl;
import com.buglabs.bug.module.pub.IModlet;
import com.buglabs.device.ButtonEvent;
import com.buglabs.device.IButtonEventListener;
import com.buglabs.device.IButtonEventProvider;
import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleLEDController;
import com.buglabs.module.IModuleProperty;
import com.buglabs.module.ModuleProperty;
import com.buglabs.osgi.shell.IShellCommandProvider;
import com.buglabs.osgi.shell.pub.AbstractCommand;
import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.services.ws.PublicWSProvider2;
import com.buglabs.services.ws.WSResponse;
import com.buglabs.util.RemoteOSGiServiceConstants;

/**
 * 
 * @author kgilmer
 * 
 */
public class CameraModlet implements IModlet, ICameraDevice, PublicWSProvider2, PublicWSProvider, IModuleControl, ICameraButtonEventProvider, IShellCommandProvider {
	private ServiceRegistration wsRef;

	private int megapixels;

	private List modProps;

	private final BundleContext context;

	private final int slotId;

	private final String moduleName;

	private ServiceRegistration moduleControl;

	private ServiceRegistration cameraService;

	private ServiceRegistration cameraModuleControl;

	private ServiceRegistration buttonEventProvider;

	private boolean demoMode = false;

	private BufferedReader camera;

	public static final String PROPERTY_CAMERA_SNAPSHOTS = "com.buglabs.bug.emulator.module.camera.snapshots";

	public static final String MODULE_ID = "CAMERA";

	private List listeners;

	private File fprops;

	private String currentImageFile;

	private ServiceRegistration ledControl;

	private LogService logService;

	private String serviceName = "Picture";

	private ServiceRegistration buttonCmdReg;

	public CameraModlet(BundleContext context, int slotId, String moduleName, LogService logService) {
		this.context = context;
		this.slotId = slotId;
		this.moduleName = moduleName;
		this.logService = logService;
		listeners = new ArrayList();
	}

	public String getModuleId() {
		return moduleName;
	}

	public int getSlotId() {
		return slotId;
	}

	public void setup() throws Exception {
		File f = new File("/dev/video0");// + slotId);

		if (!f.exists()) {
			demoMode = true;
		} else {
			// TODO: here go the goods!
		}
	}

	public void start() throws Exception {
		if (demoMode) {
			megapixels = 3;
		}

		fprops = null;

		modProps = new ArrayList();

		List wsProviders = new ArrayList();
		wsProviders.add(this);

		String prop = System.getProperty(PROPERTY_CAMERA_SNAPSHOTS);
		if (prop != null) {
			fprops = new File(prop);
			if (fprops.exists()) {
				camera = new BufferedReader(new FileReader(fprops));
			}
		}

		wsRef = context.registerService(PublicWSProvider.class.getName(), this, null);

		moduleControl = context.registerService(IModuleControl.class.getName(), this, null);
		CameraModuleControl cameraModuleControlObj = new CameraModuleControl(slotId, logService);
		cameraModuleControl = context.registerService(ICameraModuleControl.class.getName(), cameraModuleControlObj, createRemotableProperties(null));
		ledControl = context.registerService(IModuleLEDController.class.getName(), cameraModuleControlObj, createRemotableProperties(null));

		cameraService = context.registerService(ICameraDevice.class.getName(), this, createRemotableProperties(null));

		if (context.getServiceReferences(IButtonEventProvider.class.getName(), "(ButtonsProvided=Camera)") == null) {
			buttonEventProvider = context.registerService(ICameraButtonEventProvider.class.getName(), this, createRemotableProperties(getButtonServiceProperties()));
		}

		buttonCmdReg = context.registerService(IShellCommandProvider.class.getName(), this, null);
	}

	public void stop() throws Exception {
		buttonCmdReg.unregister();
		cameraService.unregister();
		moduleControl.unregister();
		cameraModuleControl.unregister();
		ledControl.unregister();
		if (buttonEventProvider != null)
			buttonEventProvider.unregister();
		wsRef.unregister();
	}

	private Dictionary getButtonServiceProperties() {
		Dictionary props = new Hashtable();

		props.put("ButtonEventProvider", this.getClass().getName());
		props.put("ButtonsProvided", "Camera");
		props.put("Emuluated", "true");

		return props;
	}

	public PublicWSDefinition discover(int operation) {
		if (operation == PublicWSProvider2.GET) {
			return new PublicWSDefinition() {

				public List getParameters() {
					return null;
				}

				public String getReturnType() {
					return "image/jpeg";
				}
			};
		}

		return null;
	}

	public IWSResponse execute(int operation, String input) {
		if (operation == PublicWSProvider2.GET) {
			return new WSResponse(getImageInputStream(), "image/jpg");
		}
		return null;
	}

	public String getPublicName() {
		return serviceName;
	}

	public List getModuleProperties() {
		modProps.clear();

		modProps.add(new ModuleProperty("MP", "" + megapixels, "Number", false));
		modProps.add(new ModuleProperty("Slot", "" + slotId));

		return modProps;
	}

	public String getModuleName() {
		return moduleName;
	}

	public boolean setModuleProperty(IModuleProperty property) {
		if (property.getName().equals("MP")) {
			megapixels = Integer.parseInt((String) property.getValue());
			return true;
		}

		return false;
	}

	public byte[] getImage() {
		InputStream pic = getImageInputStream();

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		int read = 0;
		byte[] buff = new byte[4096];

		try {
			while ((read = pic.read(buff)) > 0) {
				os.write(buff, 0, read);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return os.toByteArray();
	}

	public InputStream getImageInputStream() {
		InputStream pic = null;

		if (camera == null) {
			pic = CameraModlet.class.getResourceAsStream("snapshot.jpg");
		} else {
			try {
				String line = camera.readLine();
				if (fprops != null) {
					if (line != null && line.length() > 0) {
						currentImageFile = line;
					}

					File file = new File(fprops.getParentFile(), currentImageFile);
					if (file.exists()) {
						pic = new FileInputStream(file);
					}
				}
			} catch (IOException e) {
				// Purposely do nothing.
			}
		}

		// camera wasn't working and or reading picture file failed.
		if (pic == null) {
			pic = CameraModlet.class.getResourceAsStream("snapshot.jpg");
		}
		return pic;
	}

	public String getFormat() {

		return "image/jpg";
	}

	public String getDescription() {
		return "Retrieves image from camera module.";
	}

	public synchronized void addListener(IButtonEventListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public synchronized void removeListener(IButtonEventListener listener) {
		listeners.remove(listener);
	}

	private void fireEvent(ButtonEvent event) {
		Iterator iter = listeners.iterator();

		while (iter.hasNext()) {
			IButtonEventListener listener = (IButtonEventListener) iter.next();
			listener.buttonEvent(event);
		}
	}

	public void keyPressed(KeyEvent event) {
		char value = (char) event.getKeyChar();
		switch (value) {
		case 'w':
			fireEvent(new ButtonEvent(ButtonEvent.BUTTON_CAMERA_ZOOM_OUT));
			break;
		case 't':
			fireEvent(new ButtonEvent(ButtonEvent.BUTTON_CAMERA_ZOOM_IN));
			break;
		case 'o':
			fireEvent(new ButtonEvent(ButtonEvent.BUTTON_CAMERA_SHUTTER));
			break;
		default:
			break;
		}
	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * @return A dictionary with R-OSGi enable property.
	 */
	private Dictionary createRemotableProperties(Dictionary ht) {
		if (ht == null) {
			ht = new Hashtable();
		}

		ht.put(RemoteOSGiServiceConstants.R_OSGi_REGISTRATION, "true");

		return ht;
	}

	public byte[] getImage(int sizeX, int sizeY, int format, boolean highQuality) {
		logService.log(LogService.LOG_INFO, "calling getImage(), parameters ignored in Virtual BUG");
		return getImage();
	}

	public boolean initOverlay(Rectangle pbounds) {
		logService.log(LogService.LOG_INFO, "Called initOverlay(pbounds).  This method not implemented in Virtual BUG");
		return false;
	}

	public boolean startOverlay() {
		logService.log(LogService.LOG_INFO, "Called startOverlay().  This method not implemented in Virtual BUG");
		return false;
	}

	public boolean stopOverlay() {
		logService.log(LogService.LOG_INFO, "Called stopOverlay().  This method not implemented in Virtual BUG");
		return false;
	}

	public void setPublicName(String name) {
		serviceName = name;
	}

	public int resume() throws IOException {
		throw new IOException("CameraModlet resume call is not implemented for Virtual BUG");
	}

	public int suspend() throws IOException {
		throw new IOException("CameraModlet suspend call is not implemented for Virtual BUG");
	}

	@Override
	public List getCommands() {
		List l = new ArrayList();

		l.add(new CameraButtonCommand());

		return l;
	}

	private class CameraButtonCommand extends AbstractCommand {

		@Override
		public void execute() throws Exception {
			String b = (String) arguments.get(0);

			if (b.equals("out")) {
				fireEvent(new ButtonEvent(ButtonEvent.BUTTON_CAMERA_ZOOM_OUT));
			} else if (b.equals("in")) {
				fireEvent(new ButtonEvent(ButtonEvent.BUTTON_CAMERA_ZOOM_IN));
			} else if (b.equals("shutter")) {
				fireEvent(new ButtonEvent(ButtonEvent.BUTTON_CAMERA_SHUTTER));
			}
		}

		@Override
		public String getName() {
			return "button.camera";
		}

		@Override
		public boolean isValid() {
			return this.arguments.size() == 1;
		}

		@Override
		public String getUsage() {
			return "[shutter | in | out]";
		}

		@Override
		public String getDescription() {
			return "Simulates button presses on camera module.";
		}

	}
}