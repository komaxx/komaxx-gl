package com.komaxx.komaxx_gl.scenegraph;

import com.komaxx.komaxx_gl.RenderContext;

/**
 * A simple implementation of the IGlRunnable that will reschedule
 * itself until a given delay is reached.
 * 
 * @author matthias.schicker
 */
public abstract class ADelayedGlRunnable implements IGlRunnable {
	private final Node queueNode;
	private final int delayMs;
	private long triggerTime;

	public ADelayedGlRunnable(Node queueNode, int delayMs){
		this.queueNode = queueNode;
		this.delayMs = delayMs;
	}
	
	@Override
	public final void run(RenderContext rc) {
		if (triggerTime == 0){
			triggerTime = rc.frameNanoTime + delayMs * 1000L * 1000L;
			reschedule();
		} else if (rc.frameNanoTime < triggerTime){
			reschedule();
		} else {
			doRun(rc);
		}
	}

	protected abstract void doRun(RenderContext rc);

	@Override
	public void abort(){
		// nothing in the default case.
	}
	
	private final void reschedule() {
		queueNode.queueOnceInGlThread(this);
	}
	
	/**
	 * Resets the trigger, so the run method will only be executed
	 * after the set delay.
	 */
	public void postponeTrigger(){
		triggerTime = 0;
	}
}
