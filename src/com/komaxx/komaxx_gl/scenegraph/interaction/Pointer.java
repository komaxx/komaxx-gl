package com.komaxx.komaxx_gl.scenegraph.interaction;

import android.view.MotionEvent;

import com.komaxx.komaxx_gl.MatrixStack;
import com.komaxx.komaxx_gl.math.Vector;

/**
 * Contains information about one interaction point (one place
 * where a finger touches the screen).
 *  
 * @author Matthias Schicker
 */
public class Pointer {
	
	public static final int MOVE = MotionEvent.ACTION_MOVE;
	public static final int DOWN = MotionEvent.ACTION_DOWN;
	public static final int UP = MotionEvent.ACTION_UP;
	public static final int CANCEL = MotionEvent.ACTION_CANCEL;
	
	/**
	 * This is the action sent when a spot was touched for a sufficient
	 * long time (without moving too much).
	 */
	public static final int LONG_CLICK = -2;
	/**
	 * This is the value the action field gets assigned when the pointer
	 * is not active.
	 */
	public static final int INACTIVE = -1;
	
	/**
	 * What happened in this frame to this pointer (DOWN/MOVE/UP/...)
	 */
	private int action = INACTIVE; 
	
	/**
	 * This is what the system gives us. Just the pure screen coords, not yet
	 * transformed into world coords of any kind.
	 */
	private float[] screenCoords = new float[2];
	
	
	/**
	 * A point somewhere in the world coords that lies on the ray from the eyepoint
	 * into the world.</br> 
	 * NOTE: This point may or may not lie in the z0 plane of the current coordinate
	 * system. Call "moveRaypointToZ0plane" to make that sure. 
	 */
	private MatrixStack rayPoint = new MatrixStack(4);
	
	/**
	 * Same as rayPoint, but from the last frame. 
	 */
	private MatrixStack lastRayPoint = new MatrixStack(4);
	
	// speed up field. Information is actually already contained in "action"
	private boolean active = false;
	
	// //////////////////////////////////////////////////////////////////
	// tmps
	private static final float[] tmpRaySlopeVector = new float[4];


	protected void setAction(int action) {
		this.action = action;
		active = action!=INACTIVE;
	}
	
	protected void setScreenCoords(float x, float y){
		screenCoords[0] = x;
		screenCoords[1] = y;
		
		float[] peek = rayPoint.peek();
		peek[0] = x;
		peek[1] = y;
		peek[2] = 0;
		peek[3] = 1;
	}
	
	/**
	 * Call this to make sure that the current ray point lies in the z0 plane in
	 * the current coordinate system. 
	 */
	public final void moveRaypointsToZ0plane(InteractionContext ic){
		float[] peek = rayPoint.peek();
		if (peek[2] == 0) return;		// already in the z0-plane!
		
		float[] eyePoint = ic.eyePointStack.peek();
		
		movePointToZ0plane(peek, eyePoint);
		movePointToZ0plane(lastRayPoint.peek(), eyePoint);
	}

	private static void movePointToZ0plane(float[] point, float[] eyePoint) {
		Vector.aToB3(tmpRaySlopeVector, eyePoint, point);

		// find the zero point: eyePoint + ß*raySlope =! (?,?,0)
		// <=> eyePoint.z + ß*raySlope.z = 0
		// <=> ß = -eyePoint.z / raySlope.z
		float gamma = -eyePoint[2] / tmpRaySlopeVector[2];

		Vector.set3(point, 
				eyePoint[0] + gamma * tmpRaySlopeVector[0],
				eyePoint[1] + gamma * tmpRaySlopeVector[1], 0);
	}

	public boolean isActive() {
		return active;
	}

	public MatrixStack getRayPointStack() {
		return rayPoint;
	}

	public float[] getRayPoint() {
		return rayPoint.peek();
	}
	
	public float[] getScreenCoords(){
		return screenCoords;
	}

	public int getAction() {
		return action;
	}

	public boolean tapRangeLeft() {
		
		// TODO
		
		return false;
	}

	public float[] pushRayPoint() {
		return rayPoint.push();
	}
	
	public void popRayPoint(){
		rayPoint.pop();
	}

	public void reset() {
		rayPoint.reset();
		
		if (active){
			float[] peek = rayPoint.peek();
			peek[0] = screenCoords[0];
			peek[1] = screenCoords[1];
			peek[2] = 0;
			peek[3] = 1;
		}
	}
	
	@Override
	public String toString() {
		return "[" + (isActive() ? "X" : " ") + "] ("+Vector.toString(rayPoint.peek())+")";
	}
}
