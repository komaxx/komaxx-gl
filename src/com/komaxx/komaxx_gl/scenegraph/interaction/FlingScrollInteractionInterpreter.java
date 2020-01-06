package com.komaxx.komaxx_gl.scenegraph.interaction;

import android.graphics.PointF;

import com.komaxx.komaxx_gl.SceneGraphContext;
import com.komaxx.komaxx_gl.util.Interpolators;

/**
 * With this, a list or something similar can be handled
 * (scrolled, flinged, clicked).
 * 
 * @author Matthias Schicker
 */
public class FlingScrollInteractionInterpreter {
	private static final long CENTER_DURATION_NS = 500000000L;

	/**
	 * When the movement delta is smaller than this, no flinging is executed.
	 * (worldX per NS)
	 */
	private static final float MIN_FLING_SPEED = 0.00000001f;
	private static final long FLING_DURATION_NS = 2000000000L;

	
	private final IFlingable flingable;
	
	private boolean interacting = false;
	
	private MotionEventHistory moveEventHistory = new MotionEventHistory(15);
	private Interpolators.Interpolator flingInterpolator;

	private float currentScrollOffset = 0;
	
	private float scrollLimitMax = 10000;
	private float scrollLimitMin = 0;
	

	// ///////////////////////////////////////
	// tmps, caches
	protected float lastInteraction;
	
	
	public FlingScrollInteractionInterpreter(IFlingable flingable){
		this.flingable = flingable;
	}
	
	public float proceed(SceneGraphContext sgContext) {
		if (!interacting){
			boolean flinging = executeFlinging(sgContext);
			if (flinging) sgContext.setNotIdle();
		} else {  // currently interacting
			sgContext.setNotIdle();
		}
		return currentScrollOffset;
	}
	
	protected boolean executeFlinging(SceneGraphContext sgContext) {
		if (flingInterpolator == null) return false;
		currentScrollOffset = flingInterpolator.getValue(sgContext.frameNanoTime);

		if (-currentScrollOffset > scrollLimitMax 
				&& flingInterpolator.getEndY() < currentScrollOffset){
			// moving outside of the bounds -> clamp
			currentScrollOffset = -scrollLimitMax;
			flingInterpolator = null;
		} else if (-currentScrollOffset < scrollLimitMin
				&& flingInterpolator.getEndY() > currentScrollOffset){
			// moving outside of the bounds -> clamp
			currentScrollOffset = -scrollLimitMin;
			flingInterpolator = null;
		} else if (sgContext.frameNanoTime > flingInterpolator.getEndX()){
			flingInterpolator = null;
		}
		
		return true;	
	}

	private void move(InteractionContext ic){
		
		throw new RuntimeException("Not implemented yet");
		// TODO
		
//		float nowInteraction = ic.getZ0interactionPoints()[0].x;
//		float tmpMoveDelta = nowInteraction - lastInteraction;
//		moveEventHistory.add(ic.frameNanoTime, tmpMoveDelta, tmpMoveDelta);
//		currentScrollOffset += tmpMoveDelta;
//		lastInteraction = nowInteraction;
	}
	
	public boolean onInteraction(InteractionContext interactionContext) {
		throw new RuntimeException("Not implemented yet");

		/*
		InteractionEvent event = interactionContext.getCurrentInteractionEvent();
		if (event.getPointerCount() != 1){
			return false;
		}
		
		PointF[] z0interactionPoints = interactionContext.getZ0interactionPoints();
		
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN && flingable.inBounds(z0interactionPoints[0])){
			lastInteraction = interactionContext.getZ0interactionPoints()[0].x;
			interacting = true;
		}

		if (action != MotionEvent.ACTION_DOWN && !interacting){
			// nothing for us
			return false;
		}
		
		InteractionEvent lastEvent = interactionContext.getLastInteractionEvent();
		flingInterpolator = null;

		if (lastEvent != null){
			move(interactionContext);
		}

		if (event.getAction() == MotionEvent.ACTION_UP){
		
			// TODO
			// could be click!
			
			computeFlinging(interactionContext);
			moveEventHistory.clear();
			interacting = false;
		} else if (event.getAction() == MotionEvent.ACTION_CANCEL){
			moveEventHistory.clear();
			interacting = false;
		}

		// snap back to allowed area
		if (event.isUpOrCancel() && -currentScrollOffset > scrollLimitMax){
			centerOn(interactionContext, scrollLimitMax);
		} else if (event.isUpOrCancel() && -currentScrollOffset < scrollLimitMin){
			centerOn(interactionContext, scrollLimitMin);
		}
		
		return interacting;
		//*/
	}
	
	private void computeFlinging(InteractionContext interactionContext) {
        // compute the fling speed
        long historyTime = 0;
        float historyDistance = 0;
        
        long frameTime = interactionContext.frameNanoTime;
        
        MotionEventHistory.HistoryMotionEvent historyEvent;
        int historyCount = 0;
        for (; historyCount < moveEventHistory.size(); historyCount++){
            historyEvent = moveEventHistory.get(historyCount);
            historyTime = frameTime - historyEvent.time;
            historyDistance += historyEvent.x;
            if (historyTime > 200000000) break;
        }
        
        if (moveEventHistory.size() >= 2 && historyTime > 0){
        	double flingSpeed = (double)historyDistance/(double)historyTime;

        	if (Math.abs(flingSpeed) > MIN_FLING_SPEED){
	        	flingInterpolator = 
	                new Interpolators.AttenuationInterpolator(
	                		currentScrollOffset, frameTime, frameTime + FLING_DURATION_NS, flingSpeed );
        	}
        }
        moveEventHistory.clear();
	}

	public void centerOn(SceneGraphContext sc, float y) {
		long frameTime = sc.frameNanoTime;
    	flingInterpolator = 
            new Interpolators.HyperbelInterpolator(currentScrollOffset, -y, 
            		frameTime, frameTime + CENTER_DURATION_NS);
    	interacting = false;
	}
	
	public void setScrollLimitMax(float scrollLimitMax) {
		this.scrollLimitMax = scrollLimitMax;
	}
	
	public void setScrollLimitMin(float scrollLimitMin) {
		this.scrollLimitMin = scrollLimitMin;
	}
	
	
	public static interface IFlingable {
		public boolean inBounds(PointF p);
	}
}
