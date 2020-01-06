package com.komaxx.komaxx_gl.threading;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Allows using a handler that is bound to an activity. Runnables are
 * only executed as long as the activity is active.
 *  
 * @author Matthias Schicker
 */
public class OnScreenHandlerThread {
	private HandlerThread thread;
	private Handler handler;
	private boolean running;
	
	public OnScreenHandlerThread(String name){
		thread = new HandlerThread(name, Thread.MIN_PRIORITY+1);
		thread.start();
		handler = new Handler(thread.getLooper());
	}

	public void post(AOnScreenRunnable runnable){
		runnable.setHandlerThread(this);
		handler.post(runnable);
	}
	
	public void onResume(){
		running = true;
	}
	
	public void onPause(){
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * To be called, when the SceneGraph is definitely no longer used.
	 */
	public void onDestroy() {
		thread.quit();
	}
}
