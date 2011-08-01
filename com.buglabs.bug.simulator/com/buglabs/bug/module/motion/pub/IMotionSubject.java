package com.buglabs.bug.module.motion.pub;

/**
 * Register for notification on motion events.
 * @author aroman
 * @deprecated This module is not supported in BUG 2.0
 *
 */
public interface IMotionSubject {
	/**
	 * Register as an observer for motion events.
	 * @param obs
	 */
	public void register(IMotionObserver obs);

	/**
	 * Unregister as an observer for motion events.
	 * @param obs
	 */
	public void unregister(IMotionObserver obs);
}