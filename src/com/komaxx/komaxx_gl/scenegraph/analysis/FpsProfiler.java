package com.komaxx.komaxx_gl.scenegraph.analysis;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.util.KoLog;

import android.widget.TextView;

/**
 * Computes FPS and prints them either into a TextView or in the log.
 * If other profiling capabilities are necessary, a FpsCounter node
 * may be utilized (this is a little more expensive).
 * 
 * @author Matthias Schicker
 */
public class FpsProfiler implements IRenderProfiler, Runnable {
	private static final int SECOND_NS = 1 * 1000 * 1000 * 1000;
	/**
	 * The fps will be re-computed with this interval.
	 */
	private static final int COMPUTATION_INTERVAL_NS = SECOND_NS;

	private final TextView targetView;
	
	private long delta;
	private long lastFrameTime;
	
	private int frameCollector;
	private long timeCollector;
	
	private long min;
	private long max;
	
	private float fps;



	public FpsProfiler(TextView optionalTargetView) {
		this.targetView = optionalTargetView;
	}
	
	@Override
	public void frameStart() {
		// ignore. We do everything after the frame was done.
	}

	@Override
	public void frameDone(RenderContext renderContext) {
		delta = renderContext.frameNanoTime-lastFrameTime;
		if (delta > max) max = delta;
		if (delta < min) min = delta;
		timeCollector += delta;
		lastFrameTime = renderContext.frameNanoTime;
		frameCollector++;
		
		if (timeCollector >= COMPUTATION_INTERVAL_NS){
			fps = ((float)frameCollector / ((float)timeCollector / (float)SECOND_NS));
			
			
			if (targetView != null){
				targetView.post(this);
			} else {
				KoLog.i(this, "fps: ", Float.toString(fps), 
						"    max ", Float.toString((float)SECOND_NS/(float)min),
						", min: ", Float.toString((float)SECOND_NS/(float)max));
			}

			timeCollector = 0;
			frameCollector = 0;
			min = Long.MAX_VALUE;
			max = Long.MIN_VALUE;
		}
	}

	@Override
	public void run() {
		if (targetView != null) targetView.setText("" + ( (float)((int)(fps * 10))  / 10.0f));
	}
	
	@Override
	public void globalRunnablesStart() { /* unused */ }

	@Override
	public void globalRunnablesDone() { /* unused */ }

	@Override
	public void startPath(Path path) { /* unused */ }

	@Override
	public void pathDone(Path path) { /* unused */ }

}
