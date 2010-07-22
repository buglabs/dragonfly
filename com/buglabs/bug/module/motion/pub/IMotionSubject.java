package com.buglabs.bug.module.motion.pub;

/**
 * Register for notification on motion events.
 * @author aroman
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