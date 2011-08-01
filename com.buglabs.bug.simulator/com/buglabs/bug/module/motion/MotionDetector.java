package com.buglabs.bug.module.motion;

import com.buglabs.bug.module.motion.pub.IMotionSubject;

/**
 * @deprecated This module is not supported in BUG 2.0 *
 */
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
