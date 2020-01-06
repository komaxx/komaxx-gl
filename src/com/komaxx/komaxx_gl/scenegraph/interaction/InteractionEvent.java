package com.komaxx.komaxx_gl.scenegraph.interaction;

import java.util.Stack;

import android.view.MotionEvent;

import com.komaxx.komaxx_gl.RenderConfig;
import com.komaxx.komaxx_gl.math.Vector;

/**
 * InteractionEvents are used and generated when some interaction on
 * the GL surface happens.
 * 
 * @author Matthias Schicker
 *
 */
public class InteractionEvent {
	private static final int MAX_INTERACTION_EVENT_POOL_SIZE = 30;

	public static final int MAX_POINTER_COUNT = 5;
	public static final int LONG_CLICK = -1;
	
	private static Stack<InteractionEvent> interactionEventPool = new Stack<InteractionEvent>();
	
	private long time;
	
	private int pointerCount = 0;
	private float[][] pointers;
	private int[] pointerIDs;
	private float[][] downPoints;
	private long downTime;
	
	private int action = MotionEvent.ACTION_DOWN;
	private boolean canceledByPointerCountChange;
	private boolean tapRangeLeft = false;

	
	public static InteractionEvent obtain(long frameNanoTime){
		if (!interactionEventPool.isEmpty()){
			InteractionEvent pop = interactionEventPool.pop();
			pop.setTime(frameNanoTime);
			return pop;
		}
		InteractionEvent ret = new InteractionEvent();
		ret.setTime(frameNanoTime);
		return ret;
	}

	private void setTime(long frameNanoTime) {
		time = frameNanoTime;
	}

	/**
	 * Not directly creatable. Get one by calling "obtain". Always remember
	 * to recycle no longer used events.
	 */
	private InteractionEvent(){
		pointers = new float[MAX_POINTER_COUNT][];
		downPoints = new float[MAX_POINTER_COUNT][];
		pointerIDs = new int[MAX_POINTER_COUNT];
		for (int i = 0; i < MAX_POINTER_COUNT; i++){
			pointers[i] = new float[2];
			downPoints[i] = new float[2];
		}
	}

	public void recycle() {
		if (interactionEventPool.size() <= MAX_INTERACTION_EVENT_POOL_SIZE){
			reset();
			interactionEventPool.push(this);
		}
	}

	public void set(InteractionEvent from) {
		if (from == null) return;
		
		downTime = from.downTime;
		
		this.downPoints = from.downPoints.clone();
		
		pointerCount = from.pointerCount;
		for (int i = 0; i < MAX_POINTER_COUNT; i++){
			Vector.set2(pointers[i], from.pointers[i]);
			Vector.set2(downPoints[i], from.downPoints[i]);
			pointerIDs[i] = from.pointerIDs[i];
		}
		action = from.action;
		canceledByPointerCountChange = from.canceledByPointerCountChange;
		tapRangeLeft = from.tapRangeLeft;
	}
	
	public void reset() {
		time = 0; 
	}

	public boolean isUpOrCancel() {
		return action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL;
	}

	public int getPointerCount() {
		return pointerCount;
	}

	/**
	 * The framework will translate a pointer count change into a cancel
	 * event for the old pointer count, and a new down event for the new pointer
	 * count. Query this method, to check whether this just happened. Only
	 * valid results when getAction() delviders ACTION_CANCEL.
	 */
	public boolean isCanceledByPointerCountChange() {
		return canceledByPointerCountChange;
	}
	
	public void setCanceledEvent() {
		canceledByPointerCountChange = true;
		tapRangeLeft = true;
		action = MotionEvent.ACTION_CANCEL;
	}

	public void setPointerCountChangeDownEvent(MotionEvent me) {
		pointerCount = Math.min(MAX_POINTER_COUNT, me.getPointerCount());
		for (int i = pointerCount; i < MAX_POINTER_COUNT; i++) pointerIDs[i] = -1;
		setPointers(downPoints, pointerCount, me);
		setPointers(pointers, pointerCount, me);
		
		tapRangeLeft = true;
		downTime = System.currentTimeMillis();
		action = MotionEvent.ACTION_DOWN;
	}

	private void setPointers(float[][] toSet, int pointerCount, MotionEvent me){
		int i = 0;
		for (; i < pointerCount; i++){
			toSet[i][0] = me.getX(i);
			toSet[i][1] = me.getY(i);
			pointerIDs[i] = me.getPointerId(i);
		}
		for (; i < MAX_POINTER_COUNT; i++) pointerIDs[i] = -1;
	}
	
	/**
	 * The last down in System.millis
	 */
	public long getDownTime() {
		return downTime;
	}
	
	public float[][] getPointers() {
		return pointers;
	}
	
	public int[] getPointerIDs(){
		return pointerIDs;
	}
	
	public float[][] getDownPoints() {
		return downPoints;
	}
	
	public void set(MotionEvent me) {
		pointerCount = Math.min(MAX_POINTER_COUNT, me.getPointerCount());
		setPointers(pointers, pointerCount, me);
		action = me.getAction();
		if (action == MotionEvent.ACTION_DOWN){
			downTime = System.currentTimeMillis();
			setPointers(downPoints, pointerCount, me);
		}
	}
	
	@Override
	public String toString() {
		return actionToString() + ", " + pointerCount + " pointers";
	}

	private String actionToString() {
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			return "DOWN";
		case MotionEvent.ACTION_UP:
			return "UP";
		case MotionEvent.ACTION_CANCEL:
			return "CANCEL";
		case MotionEvent.ACTION_MOVE:
			return "MOVE";
		case LONG_CLICK:
			return "LONG_CLICK";
		}
		return "??";
	}

	public void setLongClick() {
		this.action = LONG_CLICK;
	}

	public void setAction(int action2) {
		this.action = action2;
	}
	
	public int getAction() {
		return action;
	}
	
	public boolean wasTapRangeLeft() {
		return tapRangeLeft;
	}

	/**
	 * Call this after the construction and setting of an interaction event is done.
	 * This will trigger some computations to decide, whether this might still be a tap
	 * or is (possibly) even a double tap.
	 */
	public void compute() {
		for (int i = 0; i < pointerCount; i++){
			tapRangeLeft |= Vector.distance2(downPoints[i], pointers[i]) > RenderConfig.TAP_DISTANCE;
		}
	}
	
	public long getTime() {
		return time;
	}

	public void setTapRangeLeft() {
		tapRangeLeft = true;
	}
}
