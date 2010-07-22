package com.buglabs.bug.module.motion.commands;

import java.io.OutputStream;
import java.util.List;

import org.osgi.framework.BundleContext;

import com.buglabs.bug.module.motion.MotionSubject;
import com.buglabs.osgi.shell.ICommand;

public class MotionCommand implements ICommand {

	MotionSubject motionSubject;

	public MotionCommand(MotionSubject motionSubject) {
		this.motionSubject = motionSubject;
	}

	public void execute() throws Exception {
		motionSubject.notifyObservers();
	}

	public String getDescription() {
		return "Generates a motion event";
	}

	public String getName() {
		return "motion";
	}

	public String getUsage() {
		return "motion";
	}

	public void initialize(List arguments, OutputStream out, OutputStream err, BundleContext context) {
		// nothing to initialize
	}

	public boolean isValid() {
		return true;
	}

}
