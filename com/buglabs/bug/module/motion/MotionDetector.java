package com.buglabs.bug.module.motion;

import com.buglabs.bug.module.motion.pub.IMotionSubject;

public class MotionDetector extends Thread {
	private IMotionSubject motionSubject;

	private boolean tearDown;

	public MotionDetector(IMotionSubject motionSubject) {
		this.motionSubject = motionSubject;
		tearDown = false;
	}

	public void run() {

	}

}
