package com.buglabs.dragonfly.model;

public class LoadingBugNode extends BaseTreeNode {
	private static final long serialVersionUID = -5403460660537003077L;

	private LoadingBugNode node = this;

	public LoadingBugNode() {
		super("Listening for BUGs, please wait...");
		/*
		 * int numberOfMillisecondsInTheFuture = 10000; // 10 sec Date timeToRun =
		 * new Date(System.currentTimeMillis()+numberOfMillisecondsInTheFuture);
		 * Timer timer = new Timer();
		 * 
		 * timer.schedule(new TimerTask() { public void run() { setName("No BUGs
		 * found"); DragonflyActivator.getDefault().fireModelChangeEvent(new
		 * ModelNodeChangeEvent(node, node)); } }, timeToRun);
		 */
	}
}
