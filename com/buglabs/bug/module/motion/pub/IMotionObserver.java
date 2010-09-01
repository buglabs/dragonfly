package com.buglabs.bug.module.motion.pub;

/**
 * Implementors will register this with <code>IMotionSubject</code> to be
 * notified when motion events occur.
 * 
 * @author aroman
 * @deprecated This module is not supported in BUG 2.0
 * 
 */
public interface IMotionObserver {
	/**
	 * Signals the client that motion has been detected.
	 */
	public void motionDetected();

}
