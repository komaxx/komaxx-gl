package com.komaxx.komaxx_gl.scenegraph.basic_nodes;

import android.widget.TextView;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.SceneGraphContext;
import com.komaxx.komaxx_gl.scenegraph.Node;
import com.komaxx.komaxx_gl.util.KoLog;

/**
 * This class is supposed to be added on a top level of the scene graph. It's
 * sole purpose is to count frames and calculate the FPS. It does *not* paint
 * anything.
 *  
 * @author Matthias Schicker
 */
public class FpsCounter extends Node implements Runnable {
	private static final int SECOND_NS = 1 * 1000 * 1000 * 1000;
	/**
	 * The fps will be re-computed with this interval.
	 */
	private static final int COMPUTATION_INTERVAL_NS = SECOND_NS;

	private long delta;
	private long lastFrameTime;
	
	private int frameCollector;
	private long timeCollector;
	
	private long min;
	private long max;
	
	private float fps;

	private final TextView targetView;
	
	public FpsCounter(TextView targetView){
		draws = true;
		transforms = false;
		handlesInteraction = false;
		blending = DONT_CARE;
		depthTest = DONT_CARE;
		scissorTest = DONT_CARE;

		clusterIndex = CLUSTER_INDEX_INVISIBLE;
		
		zLevel = Integer.MAX_VALUE-2;
		
		this.targetView = targetView;
	}
	
	@Override
	protected void applyStateChangeRendering(RenderContext renderContext) {
		// do nothing!
	}
	
	@Override
	protected void applyStateChangeTransform(SceneGraphContext scContext) {
		// do nothing
	}
	
	@Override
	public boolean onRender(RenderContext renderContext) {
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
				KoLog.i(this, "fps: " + fps 
						+ "    max " + (float)SECOND_NS/(float)min
						+ ", min: " + (float)SECOND_NS/(float)max);
			}

			timeCollector = 0;
			frameCollector = 0;
			min = Long.MAX_VALUE;
			max = Long.MIN_VALUE;
		}
		return true;
	}

	public float getFps() {
		return fps;
	}
	
	@Override
	public void run() {
		if (targetView != null) targetView.setText("" + ( (float)((int)(fps * 10))  / 10.0f));
	}
}
