package com.komaxx.komaxx_gl.scenegraph;

import com.komaxx.komaxx_gl.RenderContext;

/**
 * IGlRunnables can be scheduled in Nodes to be run when processing
 * the Node. Careful: The node must actually be processed, which may
 * not happen when the parent is, e.g., not visible!
 *  
 * @author Matthias Schicker
 */
public interface IGlRunnable {
	public void run(RenderContext rc);

	/**
	 * To be called, when the runnable is no longer necessary and will never be run.
	 * Do clean-ups in here.
	 */
	public void abort();
}
