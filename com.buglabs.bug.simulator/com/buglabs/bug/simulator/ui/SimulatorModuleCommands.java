package com.buglabs.bug.simulator.ui;

import java.util.ArrayList;
import java.util.List;

import org.knapsack.shell.pub.IKnapsackCommand;
import org.osgi.framework.BundleContext;

import com.buglabs.bug.bmi.api.IModletFactory;
import com.buglabs.bug.bmi.pub.BMIMessage;
import com.buglabs.bug.bmi.pub.Manager;
import com.buglabs.bug.dragonfly.module.IModuleControl;
import com.buglabs.bug.simulator.AbstractCommand;
import com.buglabs.util.osgi.OSGiServiceLoader;

/**
 * Commands for interacting with the BUG Simulator.  Insert/remove modules.
 * @author kgilmer
 *
 */
public class SimulatorModuleCommands {

	private final Manager bmiManager;
	private String moduleName;

	public SimulatorModuleCommands(Manager bmiManager) {
		this.bmiManager = bmiManager;
	}

	public List<IKnapsackCommand> getCommands() {
		List<IKnapsackCommand> l = new ArrayList<IKnapsackCommand>();
		
		l.add(new InsertModuleCommand());
		l.add(new ListModulesCommand());
		l.add(new RemoveModuleCommand());
		
		return l;
	}
	
	private class RemoveModuleCommand extends AbstractCommand {

		public String execute() throws Exception {
			Integer slotId = Integer.parseInt((String) this.arguments.get(0));				
				
			String module = getModuleInSlot(context, slotId);
			
			if (module == null) 
				return ("No module is in slot " + slotId + "\n");				
			
			
			BMIMessage removeMSG = new BMIMessage(module, "emulator", slotId, BMIMessage.EVENT_REMOVE);
			bmiManager.processMessage(removeMSG.toString());		
			
			return "";
		}

		public String getName() {
			return "rmmodule";
		}
		
		public String getUsage() {
			return "(slot 1 - 4)";
		}
		@Override
		public String getDescription() {
			return "Remove module from slot on BUG Simulator.";
		}
		
		@Override
		public boolean isValid() {			
			if (arguments.size() != 1) {
				return false;
			}
			
			try {
				int index = Integer.parseInt((String) arguments.get(0)); 
				
				if (index < 1 || index > 4) {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
			
			return true;
		}
	}
	
	/**
	 * List available modules that can be attached to BUG Simulator.
	 * @author kgilmer
	 *
	 */
	private class ListModulesCommand extends AbstractCommand {

		public String execute() throws Exception {
			final StringBuilder sb = new StringBuilder();
			
			OSGiServiceLoader.loadServices(context, IModletFactory.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {				

				public void load(Object service) throws Exception {
					sb.append(((IModletFactory) service).getModuleId());
					sb.append('\n');
				}
			});
			
			return sb.toString();
		}

		public String getName() {
			return "lsmodules";
		}
		
		@Override
		public String getDescription() {
			return "List available modules that can be attached to BUG Simulator";
		}
		
		@Override
		public boolean isValid() {		
			return arguments.size() == 0;
		}
	}
	
	private String getModuleInSlot(BundleContext context, final int slot) throws Exception {
	
		OSGiServiceLoader.loadServices(context, IModuleControl.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {

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

		public String execute() throws Exception {
			String module = (String) this.arguments.get(0);
			Integer slotId = Integer.parseInt((String) this.arguments.get(1));
				
			BMIMessage insertMSG = new BMIMessage(module, "emulator", slotId, BMIMessage.EVENT_INSERT);
			bmiManager.processMessage(insertMSG.toString());
			
			return "";
		}

		public String getName() {
			return "insmodule";
		}
		
		@Override
		public String getUsage() {
			return "(module name) (slot 1 - 4)";
		}
		
		@Override
		public boolean isValid() {	
			if (arguments.size() != 2) {
				return false;
			}
			
			try {
				int index = Integer.parseInt((String) arguments.get(1)); 
				
				if (index < 1 || index > 4) {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
			
			return true;
		}
		
		@Override
		public String getDescription() {
			return "Inserts a BUG module into the specified BMI slot.";
		}
	}
}
