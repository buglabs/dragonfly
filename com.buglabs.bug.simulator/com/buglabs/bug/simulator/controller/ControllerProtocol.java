package com.buglabs.bug.simulator.controller;

import org.osgi.framework.BundleContext;

import com.buglabs.bug.bmi.api.IModletFactory;
import com.buglabs.bug.dragonfly.module.IModuleControl;
import com.buglabs.util.osgi.OSGiServiceLoader;

/**
 * Protocol for simulator controller.
 * 
 * @author kgilmer
 * 
 */
public class ControllerProtocol {

	public static final String CMD_ERROR_RESPONSE = "ER";
	public static final String CMD_SUCCESS_RESPONSE = "OK";

	public static final String CMD_CONFIGURE_MODULE = "CM";
	public static final String CMD_MODULE_LIST = "ML";
	public static final String CMD_GOODBYE = "GB";
	private final BundleContext context;
	//private Manager bmiManager;

	public ControllerProtocol(BundleContext context) {
		this.context = context;
	//	bmiManager = Manager.getManager();
	}

	public String processInput(String inText) throws Exception {
		if (inText.equalsIgnoreCase(CMD_MODULE_LIST)) {
			return handleML();
		} else if (inText.startsWith(CMD_CONFIGURE_MODULE)) {
			return handleCM(inText.split(" "));
		} else if (inText.startsWith(CMD_GOODBYE)) {
			return CMD_GOODBYE;
		} else {
			throw new Exception("Invalid command: " + inText);
		}
	}

	private String handleCM(String[] cmd) throws Exception {

		String module = null;
		final Integer slotId = Integer.parseInt(cmd[1]);
		if (cmd.length > 2) {
			module = cmd[2];
		}

		if (slotId < 0 || slotId > 3) {
			throw new Exception("Invalid slot id.");
		}

		if (module != null) {
			final String fmodule = module;
			Thread t = new Thread(new Runnable() {

				public void run() {
					/*BMIMessage insertMSG = new BMIMessage(fmodule, "emulator", slotId, BMIMessage.EVENT_INSERT);
					bmiManager.processMessage(insertMSG.toString());*/
				}

			});
			t.start();
		} else {
			Loader l = new Loader(slotId);
			OSGiServiceLoader.loadServices(context, IModuleControl.class.getName(), null, l);

			module = l.getModuleName();

			if (module == null) {
				throw new Exception(("No module is in slot " + slotId));
			}
			final String fmodule = module;
			Thread t = new Thread(new Runnable() {

				public void run() {
				/*	BMIMessage removeMSG = new BMIMessage(fmodule, "emulator", slotId, BMIMessage.EVENT_REMOVE);
					bmiManager.processMessage(removeMSG.toString());*/
				}

			});
			t.start();
		}

		return CMD_SUCCESS_RESPONSE;
	}

	private class Loader implements OSGiServiceLoader.IServiceLoader {

		private String moduleName = null;
		private final int slot;

		public Loader(int slot) {
			this.slot = slot;
		}

		public void load(Object service) throws Exception {
			if (((IModuleControl) service).getSlotId() == slot) {
				moduleName = ((IModuleControl) service).getModuleName();
			}
		}

		public String getModuleName() {
			return moduleName;
		}
	}

	/**
	 * Module List
	 * 
	 * @return
	 * @throws Exception
	 */
	private String handleML() throws Exception {
		final StringBuffer sb = new StringBuffer();

		OSGiServiceLoader.loadServices(context, IModletFactory.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
			public void load(Object service) throws Exception {
				sb.append(((IModletFactory) service).getModuleId());
				sb.append(',');
			}
		});

		String s = sb.toString();

		if (s.length() > 1) {
			s = s.substring(0, s.length() - 1);
		}

		return s;
	}

}
