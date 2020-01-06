package com.komaxx.komaxx_gl.threading;

/**
 * Everything that wants to be executed in an OnScreenHandlerThread
 * needs to extend this class. <br>
 * <b>NOTE</b>: When the bound activity is not in front, this AOnScreenRunnable
 * will *not* be executed but discarded (you get a message, though).
 * 
 * @author Matthias Schicker
 */
public abstract class AOnScreenRunnable implements Runnable {
	private OnScreenHandlerThread handlerThread;
	
	void setHandlerThread(OnScreenHandlerThread t){
		handlerThread = t;
	}
	
	@Override
	public final void run() {
		if (handlerThread.isRunning()) doRun();
		else discarded();
	}
	
	/**
	 * Called, when the bound activity is not in front when this runnable
	 * should be executed. Will NOT be rescheduled automatically.
	 */
	protected abstract void discarded();

	/**
	 * Same as <code>run</code> in normal Runnables.
	 */
	protected abstract void doRun();
}
