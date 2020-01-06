package com.komaxx.komaxx_gl.util;

/**
 * A little comfort class that adds the required "synchronized" blocks
 * automatically.
 * 
 * @author Matthias Schicker
 */
public class Semaphor {
	public void waitS() throws InterruptedException {
		synchronized (this) {
			wait();
		}
	}

	public void waitS(long millis) throws InterruptedException {
		synchronized (this) {
			wait(millis);
		}
	}

	public void notifyS() {
		synchronized (this) {
			notify();
		}
	}

	public void notifyAllS() {
		synchronized (this) {
			notifyAll();
		}
	}
}
