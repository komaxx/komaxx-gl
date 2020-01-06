package com.komaxx.komaxx_gl.threading;

import java.util.ArrayDeque;

public abstract class RunnablePool<T extends APoolRunnable> {
	private static final int MAX_POOL_SIZE = 20;
	
	private ArrayDeque<T> pool = new ArrayDeque<T>();
	
	public T get(Object... params){
		synchronized (pool) {
			T ret;
			if (pool.size() < 1){
				ret = createPoolRunnable();
				ret.setPool(this);
			} else {
				ret = pool.pop();
			}
			ret.set(params);
			return ret;
		}
	}
	
	protected abstract T createPoolRunnable();

	public void recycle(T toRecycle) {
		synchronized (pool) {
			if (pool.size() >= MAX_POOL_SIZE) return;
			pool.push(toRecycle);
		}
	}
}
