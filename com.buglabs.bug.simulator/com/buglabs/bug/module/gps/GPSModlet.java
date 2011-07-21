package com.buglabs.bug.module.gps;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;
import org.osgi.util.measurement.Measurement;
import org.osgi.util.measurement.Unit;
import org.osgi.util.position.Position;

import com.buglabs.bug.module.gps.pub.IGPSModuleControl;
import com.buglabs.bug.module.gps.pub.INMEARawFeed;
import com.buglabs.bug.module.gps.pub.INMEASentenceProvider;
import com.buglabs.bug.module.gps.pub.INMEASentenceSubscriber;
import com.buglabs.bug.module.gps.pub.IPositionProvider;
import com.buglabs.bug.module.gps.pub.IPositionSubscriber;
import com.buglabs.bug.module.gps.pub.LatLon;
import com.buglabs.bug.module.pub.IModlet;
import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleLEDController;
import com.buglabs.module.IModuleProperty;
import com.buglabs.module.ModuleProperty;
import com.buglabs.nmea.sentences.RMC;
import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.services.ws.PublicWSProvider2;
import com.buglabs.services.ws.WSResponse;
import com.buglabs.util.ConfigAdminUtil;
import com.buglabs.util.LogServiceUtil;
import com.buglabs.util.RemoteOSGiServiceConstants;
import com.buglabs.util.SelfReferenceException;
import com.buglabs.util.XmlNode;

public class GPSModlet implements IModlet, IModuleControl, IPositionProvider, IGPSModuleControl, PublicWSProvider, PublicWSProvider2, INMEARawFeed, IModuleLEDController {
	private BundleContext context;

	private boolean deviceOn = true;

	private int slotId;

	private final String moduleName;

	private ServiceRegistration moduleRef;

	private ServiceRegistration positionRef;

	private ServiceRegistration nmeaSentenceProviderRef;

	private ServiceRegistration nmeaRawFeedRef;

	protected static final String PROPERTY_MODULE_NAME = "moduleName";

	public static final String MODULE_ID = "GPS";

	public static final String PROPERTY_GPS_LOG = "com.buglabs.bug.emulator.module.gps.log";

	public static final String PROPERTY_GPS_SAMPLING_RATE = "com.buglabs.bug.emulator.module.gps.samplingrate";

	private NMEASentenceProvider gpsd;

	private LogService logService;

	private InputStream gpsInputStream;

	private ServiceRegistration lcdRef;

	private int antennaStatus;

	private ServiceRegistration gpsModuleControlRef;

	private String serviceName = "Location";

	private ServiceRegistration pwspRef;

	public GPSModlet(BundleContext context, int slotId, String moduleName, LogService logService) {
		this.context = context;
		this.slotId = slotId;
		this.moduleName = moduleName;
		this.logService = logService;
	}

	public void setup() throws Exception {
		getReadDelay();
	}

	private long getReadDelay() {
		logService.log(LogService.LOG_DEBUG, "GPSModlet getReadDelay enter");
		ServiceReference sr = context.getServiceReference(ConfigurationAdmin.class.getName());

		long read_delay = 100;

		if (sr != null) {
			ConfigurationAdmin ca = (ConfigurationAdmin) context.getService(sr);
			logService.log(LogService.LOG_DEBUG, "GPSModlet getReadDelay obtained cm");
			if (ca != null) {
				Configuration c;
				try {
					c = ca.getConfiguration(getModuleName());				
					Dictionary properties = ConfigAdminUtil.getPropertiesSafely(c);
					
					logService.log(LogService.LOG_DEBUG, "GPSModlet getReadDelay: got configuration");
					String key = "ReadDelay";					
					
					Object obj = properties.get(key);
					if (obj != null) {
						read_delay = Long.parseLong((String) obj);
						logService.log(logService.LOG_DEBUG, "GPSModlet getReadDelay: got delay: " + read_delay);
					} else {
						properties.put(key, Long.toString(read_delay));
						c.update(properties);
						logService.log(logService.LOG_DEBUG, "GPSModlet getReadDelay: wrote delay into cm");
					}
				} catch (IOException e) {
					logService.log(logService.LOG_ERROR, "Problem retrieving data from cm:", e);
				}
			}
		}
		logService.log(logService.LOG_DEBUG, "GPSModlet getReadDelay leave");
		return read_delay;
	}

	public void start() throws Exception {

		logService = LogServiceUtil.getLogService(context);

		String prop = System.getProperty(PROPERTY_GPS_LOG);
		String samplingRateProp = System.getProperty(PROPERTY_GPS_SAMPLING_RATE);
		if (samplingRateProp == null || samplingRateProp.length() <= 0) {
			samplingRateProp = "1000";
		}

		int samplingRate = Integer.parseInt(samplingRateProp);

		gpsInputStream = null;

		if (prop == null || prop.length() == 0) {
			gpsInputStream = GPSModlet.class.getResourceAsStream("gps.log");
		} else {
			gpsInputStream = new FileInputStream(prop);
		}

		gpsd = new NMEASentenceProvider(samplingRate, gpsInputStream, context);
		gpsd.start();

		positionRef = context.registerService(IPositionProvider.class.getName(), this, createBasicServiceProperties());
		moduleRef = context.registerService(IModuleControl.class.getName(), this, createBasicServiceProperties());
		lcdRef = context.registerService(IModuleLEDController.class.getName(), this, createBasicServiceProperties());
		nmeaSentenceProviderRef = context.registerService(INMEASentenceProvider.class.getName(), gpsd, createBasicServiceProperties());
		nmeaRawFeedRef = context.registerService(INMEARawFeed.class.getName(), this, createBasicServiceProperties());
		gpsModuleControlRef = context.registerService(IGPSModuleControl.class.getName(), this, createBasicServiceProperties());

		pwspRef = context.registerService(PublicWSProvider2.class.getName(), this, createBasicServiceProperties());
		

		context.addServiceListener(gpsd, "(|(" + Constants.OBJECTCLASS + "=" + INMEASentenceSubscriber.class.getName() + ") (" + Constants.OBJECTCLASS + "="
				+ IPositionSubscriber.class.getName() + "))");
	}

	public void stop() throws Exception {		
		pwspRef.unregister();
		context.removeServiceListener(gpsd);
		positionRef.unregister();
		moduleRef.unregister();
		nmeaSentenceProviderRef.unregister();
		lcdRef.unregister();
		nmeaRawFeedRef.unregister();
		gpsModuleControlRef.unregister();
		gpsd.interrupt();
	}

	public Position getPosition() {
		RMC rmc = gpsd.getRMC();
		if (rmc != null) {
			Position pos = new Position(new Measurement(rmc.getLatitudeAsDMS().toDecimalDegrees() * Math.PI / 180.0, Unit.rad), new Measurement(rmc.getLongitudeAsDMS()
					.toDecimalDegrees() * Math.PI / 180.0, Unit.rad), new Measurement(0.0d, Unit.m), null, null);
			return pos;
		}

		return null;
	}

	public List getModuleProperties() {
		List properties = new ArrayList();

		properties.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
		properties.add(new ModuleProperty("Slot", "" + slotId));
		String posString = "";
		Position p = getPosition();
		if (p != null) {
			posString = getPosition().getLatitude() + " " + getPosition().getLongitude();
		}
		properties.add(new ModuleProperty("Location", posString));
		properties.add(new ModuleProperty("State", Boolean.toString(deviceOn), "Boolean", true));

		return properties;
	}
	
	private Properties createBasicServiceProperties() {
		Properties p = new Properties();
		p.put("Provider", this.getClass().getName());
		p.put("Slot", Integer.toString(slotId));
		return p;
	}

	public boolean setModuleProperty(IModuleProperty property) {
		if (!property.isMutable()) {
			return false;
		}

		if (property.getName().equals("State")) {
			deviceOn = Boolean.valueOf((String) property.getValue()).booleanValue();
			return true;
		}

		return false;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getModuleId() {
		return moduleName;
	}

	public int getSlotId() {
		return slotId;
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
		if (operation == PublicWSProvider2.GET) {
			return new WSResponse(getPositionXml(), "text/xml");
		}
		return null;
	}

	private String getPositionXml() {
		Position p = getPosition();

		XmlNode root = new XmlNode("Location");
		try {
			root.addChildElement(new XmlNode("Latitude", p.getLatitude().toString()));
			root.addChildElement(new XmlNode("Longitude", p.getLongitude().toString()));
			root.addChildElement(new XmlNode("Altitude", p.getAltitude().toString()));

			RMC rmc = gpsd.getRMC();
			root.addChildElement(new XmlNode("LatitudeDegrees", Double.toString(rmc.getLatitudeAsDMS().toDecimalDegrees())));
			root.addChildElement(new XmlNode("LongitudeDegrees", Double.toString(rmc.getLongitudeAsDMS().toDecimalDegrees())));

		} catch (SelfReferenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return root.toString();
	}

	public String getPublicName() {
		return serviceName;
	}

	public void setPublicName(String name) {
		serviceName = name;
	}

	public String getDescription() {
		return "Returns location as provided by GPS module.";
	}

	public LatLon getLatitudeLongitude() {
		LatLon latlong = new LatLon();
		RMC rmc = gpsd.getRMC();

		if (rmc != null) {
			latlong.latitude = rmc.getLatitudeAsDMS().toDecimalDegrees();
			latlong.longitude = rmc.getLongitudeAsDMS().toDecimalDegrees();

			return latlong;
		}

		return null;
	}

	public InputStream getInputStream() throws IOException {
		return gpsInputStream;
	}

	public int setLEDGreen(boolean state) throws IOException {
		int result = -1;
		if (state) {
			logService.log(LogService.LOG_INFO, "Green LED is on");
			result = 0;
		} else {
			logService.log(LogService.LOG_INFO, "Green LED is off");
			result = 0;
		}
		return result;
	}

	public int setLEDRed(boolean state) throws IOException {
		int result = -1;
		if (state) {
			logService.log(LogService.LOG_INFO, "Red LED is on");
			result = 0;
		} else {
			logService.log(LogService.LOG_INFO, "Red LED is off");
			result = 0;
		}
		return result;
	}

	public int LEDGreenOff() throws IOException {
		return setLEDGreen(false);
	}

	public int LEDGreenOn() throws IOException {
		return setLEDGreen(true);
	}

	public int LEDRedOff() throws IOException {
		return setLEDRed(false);
	}

	public int LEDRedOn() throws IOException {
		return setLEDRed(true);
	}

	public int getStatus() throws IOException {
		return antennaStatus;
	}

	public int setActiveAntenna() throws IOException {
		logService.log(LogService.LOG_INFO, "Antenna is active");
		antennaStatus = STATUS_ACTIVE_ANTENNA;
		return antennaStatus;
	}

	public int setPassiveAntenna() throws IOException {
		logService.log(LogService.LOG_INFO, "Antenna is passive");
		antennaStatus = STATUS_PASSIVE_ANTENNA;
		return antennaStatus;
	}

	public int resume() throws IOException {
		throw new IOException("GPSModlet resume call is not implemented for BUG Simulator");
	}

	public int suspend() throws IOException {
		throw new IOException("GPSModlet suspend call is not implemented for BUG Simulator");
	}
}
