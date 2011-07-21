package com.buglabs.bug.module.motion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.buglabs.bug.module.motion.pub.IMotionObserver;
import com.buglabs.bug.module.motion.pub.IMotionSubject;

/**
 * @deprecated This module is not supported in BUG 2.0 *
 */
public class MotionSubject implements IMotionSubject {

	private List motionObservers;
	
	public MotionSubject() {
		motionObservers = new ArrayList();
	}

	public void register(IMotionObserver obs) {
		if (!motionObservers.contains(obs)) {
			motionObservers.add(obs);
		}
	}

	public void unregister(IMotionObserver obs) {
		motionObservers.remove(obs);
	}

	public void notifyObservers() {
		Iterator iter = motionObservers.iterator();
		while (iter.hasNext()) {
			IMotionObserver obs = (IMotionObserver) iter.next();
			obs.motionDetected();
		}
	}

}
