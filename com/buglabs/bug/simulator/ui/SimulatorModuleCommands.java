package com.buglabs.bug.simulator.ui;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;

import com.buglabs.bug.bmi.pub.BMIMessage;
import com.buglabs.bug.bmi.pub.Manager;
import com.buglabs.bug.module.pub.IModletFactory;
import com.buglabs.module.IModuleControl;
import com.buglabs.osgi.shell.IShellCommandProvider;
import com.buglabs.osgi.shell.pub.AbstractCommand;
import com.buglabs.util.OSGiServiceLoader;

/**
 * Commands for interacting with the BUG Simulator.  Insert/remove modules.
 * @author kgilmer
 *
 */
public class SimulatorModuleCommands implements IShellCommandProvider {

	private final Manager bmiManager;
	private String moduleName;

	public SimulatorModuleCommands(Manager bmiManager) {
		this.bmiManager = bmiManager;
	}

	@Override
	public List getCommands() {
		List l = new ArrayList();
		
		l.add(new InsertModuleCommand());
		l.add(new ListModulesCommand());
		l.add(new RemoveModuleCommand());
		
		return l;
	}
	
	private class RemoveModuleCommand extends AbstractCommand {

		@Override
		public void execute() throws Exception {
			Integer slotId = Integer.parseInt((String) this.arguments.get(0));
			
			String module = getModuleInSlot(context, slotId);
			
			if (module == null) {
				err.write(("No module is in slot " + slotId).getBytes());
				err.write('\n');
				err.flush();
				return;
			}
			
			BMIMessage removeMSG = new BMIMessage(module, "emulator", slotId, BMIMessage.EVENT_REMOVE);
			bmiManager.processMessage(removeMSG.toString());
		}

		@Override
		public String getName() {
			return "rmmodule";
		}
		
		@Override
		public String getUsage() {
			return "slot";
		}
	}
	
	/**
	 * List available modules that can be attached to BUG Simulator.
	 * @author kgilmer
	 *
	 */
	private class ListModulesCommand extends AbstractCommand {

		@Override
		public void execute() throws Exception {
			OSGiServiceLoader.loadServices(context, IModletFactory.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {				
				@Override
				public void load(Object service) throws Exception {
					outWriter.write(((IModletFactory) service).getModuleId());
					outWriter.write('\n');
					outWriter.flush();
				}
			});
		}

		@Override
		public String getName() {
			return "lsmodules";
		}
	}
	
	private String getModuleInSlot(BundleContext context, final int slot) throws Exception {
	
		OSGiServiceLoader.loadServices(context, IModuleControl.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {

			@Override
			public void load(Object service) throws Exception {
				if (((IModuleControl) service).getSlotId() == slot) {
					moduleName = ((IModuleControl) service).getModuleName();
				}
				
			}
		});
		
		return moduleName;
	}
	
	/**
	 * Insert a BUG module into the specified slot.
	 * @author kgilmer
	 *
	 */
	private class InsertModuleCommand extends AbstractCommand {

		@Override
		public void execute() throws Exception {
			String module = (String) this.arguments.get(0);
			Integer slotId = Integer.parseInt((String) this.arguments.get(1));
				
			BMIMessage insertMSG = new BMIMessage(module, "emulator", slotId, BMIMessage.EVENT_INSERT);
			bmiManager.processMessage(insertMSG.toString());
		}

		@Override
		public String getName() {
			return "insmodule";
		}
		
		@Override
		public String getUsage() {
			return "module slot";
		}
		
		@Override
		public boolean isValid() {	
			return arguments.size() == 2;
		}
		
		@Override
		public String getDescription() {
			return "Inserts a BUG module into the specified BMI slot.";
		}
		
	}

}
