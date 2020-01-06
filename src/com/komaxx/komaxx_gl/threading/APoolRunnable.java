package com.komaxx.komaxx_gl.threading;

@SuppressWarnings("rawtypes")
public abstract class APoolRunnable implements Runnable {
	private RunnablePool pool;

	@Override
	@SuppressWarnings("unchecked")
	public final void run() {
		doRun();
		pool.recycle(this);
		reset();
	}

	/**
	 * Called when recycling the APoolRunnable. Remove lingering references
	 * to avoid memory leaks.
	 */
	protected abstract void reset();

	protected abstract void doRun();
	
	public void setPool(RunnablePool pool){
		this.pool = pool;
	}
	
	public abstract void set(Object... params);
}