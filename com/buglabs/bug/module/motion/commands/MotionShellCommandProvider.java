package com.buglabs.bug.module.motion.commands;

import java.util.ArrayList;
import java.util.List;

import com.buglabs.bug.module.motion.MotionSubject;
import com.buglabs.osgi.shell.IShellCommandProvider;

public class MotionShellCommandProvider implements IShellCommandProvider {

	MotionSubject subject;

	public MotionShellCommandProvider(MotionSubject subject) {
		this.subject = subject;
	}

	public List getCommands() {
		ArrayList list = new ArrayList(1);
		list.add(new MotionCommand(subject));

		return list;
	}

}
