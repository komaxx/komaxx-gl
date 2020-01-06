package com.komaxx.komaxx_gl.scenegraph.basic_nodes;

import android.opengl.Matrix;
import android.util.FloatMath;
import android.view.MotionEvent;

import com.komaxx.komaxx_gl.ICameraInfoProvider;
import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.SceneGraphContext;
import com.komaxx.komaxx_gl.math.MatrixUtil;
import com.komaxx.komaxx_gl.math.Vector;
import com.komaxx.komaxx_gl.scenegraph.Node;
import com.komaxx.komaxx_gl.scenegraph.interaction.InteractionContext;
import com.komaxx.komaxx_gl.scenegraph.interaction.InteractionEvent;
import com.komaxx.komaxx_gl.scenegraph.interaction.MotionEventHistory;
import com.komaxx.komaxx_gl.scenegraph.interaction.Pointer;
import com.komaxx.komaxx_gl.util.Interpolators;
import com.komaxx.komaxx_gl.util.RenderUtil;

/**
 * A camera node is essentially a comfortable wrapper to control
 * the view and projection matrix, as well as the model matrix.
 * 
 * NOTE: Only computes the matrices and stores them in the corresponding
 * matrix stacks - does <b>not</b> set any matrices in the GL!
 *  
 * @author Matthias Schicker
 */
public class CameraNode extends Node implements ICameraInfoProvider {
	private static final long CAMERA_CENTER_DURATION_NS = 500000000L;

	/**
	 * When the movement delta is smaller than this, no flinging is executed.
	 * (worldX per NS)
	 */
	private static final float MIN_FLING_SPEED = 0.00000001f;
	private static final long FLING_DURATION_NS = 3000000000L;

	/**
	 * The amount of nanos until a zoomSnap completes.
	 */
	private static final long ZOOM_SNAP_SPEED = 500000000L;

	/**
	 * Factor multiplied on top of the three-finger delta to compute the tilt angle.
	 */
	private static final float TILT_FACTOR = 0.002f;
	/**
	 * Maximum angles that the camera may be tilted.
	 */
	private static final float MAX_TILT_X_AXIS = 35;
	private static final float MAX_TILT_Y_AXIS = 10;	
	
	private static float[] ZOOM_SNAP_LEVELS = {
		10f, 28f, 180f
	};
	private static final float START_CAMERA_DISTANCE = ZOOM_SNAP_LEVELS[1];

	// limitations to the camera's movement freedom
	protected static final float MAX_CAMERA_DISTANCE = ZOOM_SNAP_LEVELS[ZOOM_SNAP_LEVELS.length-1]*1.3f;		// defines, how far the camera can be away from the xy-plane at most
	public static final float MIN_CAMERA_DISTANCE = ZOOM_SNAP_LEVELS[0]*0.2f;		// defines, how close the camera can get to the xy-plane
	
	/**
	 * When the user zooms farther out than this, the view goes all the way to 
	 * max overview.
	 */
	private static final float MAX_ZOOM_THRESHOLD = 150f;
	
	private float cameraDistance = START_CAMERA_DISTANCE;
	
	protected float nearPlane = 2;
	protected float farPlane = MAX_CAMERA_DISTANCE * 15;	// control the depth buffer precision in here!

	// controls the field of view
	private static final float XY_PANE_CAMERA_DISTANCE = 100;
	private static final float XY_PANE_DISTANCE_SPAN = 120;
	// useful for various computations. Do not set directly.
	protected float nearPlaneFactor = (nearPlane / XY_PANE_CAMERA_DISTANCE) * XY_PANE_DISTANCE_SPAN;
	
	protected float[] projectionMatrix = new float[MatrixUtil.MATRIX_SIZE];
	protected float[] viewMatrix = new float[MatrixUtil.MATRIX_SIZE];
	protected float[] invertedViewMatrix = new float[MatrixUtil.MATRIX_SIZE];
	private float[] modelMatrix = new float[MatrixUtil.MATRIX_SIZE];
	
	protected float[] eyePoint = new float[]{ 0f, 0f, cameraDistance };
	protected float[] lookingDirection = new float[]{ 0f, 0f, -1f };
	protected float[] upVector = new float[]{ 0f, 1f, 0f };
	
	protected ICameraMovementConstrainer movementConstrainer = null;
	
	protected boolean pvMatrixDirty = true;
	/**
	 *  Whenever the perspective of the camera changes, this is increased. This way, nodes further
	 *  down in the graph know that they may recompute stuff based on the new perspective.
	 */
	protected int worldChangeId = 0; 
	
	private float tiltAngleXAxis = 0.0f;
	private float tiltAngleYAxis = 0.0f;
	
	protected boolean interacting = false;
	private MotionEventHistory moveEventHistory = new MotionEventHistory(15);
	private Interpolators.Interpolator flingInterpolatorX;
	private Interpolators.Interpolator flingInterpolatorY;

	private int lastZoomSnapIndex = 1;
	private Interpolators.Interpolator zoomSnapInterpolator;
	
	// caches
	protected float surfaceWidth = 0;
	protected float surfaceHeight = 0;
	protected float surfaceRatio = 0;
	
	/**
	 * Caches the current viewProjection matrix
	 */
	protected float[] tmpPvMatrix = new float[MatrixUtil.MATRIX_SIZE];
	
	protected float[] tmpInteractionCoordsNow;
	protected float[] tmpInteractionCoordsLast;
	protected float[] downTiltXY = new float[2];
	
	
	public CameraNode(int zLevel){
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.orthoM(projectionMatrix, 0, -1, 1, -1, 1, 1, 50);
		recomputeViewMatrix();
		transforms = true;
		handlesInteraction = true;
		this.zLevel = zLevel;
	}
	
	protected float[] tmpTargetPoint = new float[3];
	protected void recomputeViewMatrix() {
		tmpTargetPoint = Vector.aPlusB3(tmpTargetPoint, eyePoint, lookingDirection);
		Matrix.setLookAtM(viewMatrix, 0, 
				eyePoint[0], eyePoint[1], eyePoint[2], 
				tmpTargetPoint[0], tmpTargetPoint[1], tmpTargetPoint[2], 
				upVector[0], upVector[1], upVector[2]);
		Matrix.invertM(invertedViewMatrix, 0, viewMatrix, 0);
	}

	@Override
	public boolean onTransform(SceneGraphContext sgContext) {
		sgContext.setCameraInfoProvider(this);
		
		float[] stackModelMatrix = sgContext.modelMatrixStack.push();
		Matrix.setIdentityM(stackModelMatrix, 0);
		
		if (!interacting) executeFlinging(sgContext);
		
		if (pvMatrixDirty){
			recomputePvMatrix();
			worldChangeId++;
			sgContext.setNotIdle();
		}
		if (interacting){
			worldChangeId++;	// this avoids that other nodes perceive the state as idle!
			sgContext.setNotIdle();
		}
		
		sgContext.worldIdStack.push().add(worldChangeId);
		
		float[] sgEyePoint = sgContext.eyePointStack.push();
		Vector.set3(sgEyePoint, eyePoint);
		sgEyePoint[3] = 1;
		
		float[] stackViewMatrix = sgContext.projectionViewMatrixStack.push();
		System.arraycopy(tmpPvMatrix, 0, stackViewMatrix, 0, MatrixUtil.MATRIX_SIZE);
		
		sgContext.setMvpMatrixDirty();
		
		return true;
	}
	
	protected void executeFlinging(SceneGraphContext sgContext) {
		long frameTime = sgContext.frameNanoTime;
		pvMatrixDirty |= executeFlingingX(frameTime);
		pvMatrixDirty |= executeFlingingY(frameTime);
		pvMatrixDirty |= executeZoomSnapping(frameTime);
		clampToValidRegion();
	}

	private boolean executeZoomSnapping(long frameTime) {
		if (zoomSnapInterpolator == null) return false;
		eyePoint[2] = zoomSnapInterpolator.getValue(frameTime);
		if (frameTime > zoomSnapInterpolator.getEndX()) zoomSnapInterpolator = null;
		return true;
	}

	private boolean executeFlingingX(long frameTime) {
		if (flingInterpolatorX == null) return false;
		eyePoint[0] = flingInterpolatorX.getValue(frameTime);
		if (frameTime > flingInterpolatorX.getEndX())  flingInterpolatorX = null;
		return true;
	}

	private boolean executeFlingingY(long frameTime) {
		if (flingInterpolatorY == null) return false;
		eyePoint[1] = flingInterpolatorY.getValue(frameTime);
		if (frameTime > flingInterpolatorY.getEndX())  flingInterpolatorY = null;
		return true;	
	}
	
	protected void recomputePvMatrix() {
		recomputeViewMatrix();
		Matrix.multiplyMM(tmpPvMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
		pvMatrixDirty = false;
	}

	@Override
	public void onUnTransform(SceneGraphContext sgContext) {
		sgContext.eyePointStack.pop();
		sgContext.worldIdStack.pop();
		sgContext.modelMatrixStack.pop();
		sgContext.projectionViewMatrixStack.pop();
		sgContext.setMvpMatrixDirty();
	}
	
	@Override
	protected void onSurfaceCreated(RenderContext renderContext) {
		pvMatrixDirty = true;
	}
	
	@Override
	public void onSurfaceChanged(RenderContext renderContext) {
		surfaceWidth = renderContext.surfaceWidth;
		surfaceHeight = renderContext.surfaceHeight;
        surfaceRatio = surfaceWidth / surfaceHeight;
       	Matrix.frustumM(projectionMatrix, 0, 
        			-surfaceRatio*nearPlaneFactor, surfaceRatio*nearPlaneFactor, 
        			-nearPlaneFactor, nearPlaneFactor, nearPlane, farPlane);
        pvMatrixDirty = true;
	}

	@Override
	public void onTransformInteraction(InteractionContext ic) {
		if (pvMatrixDirty) recomputePvMatrix();
		
		throw new RuntimeException("Not implemented yet");
		

//		InteractionEvent event = ic.getCurrentInteractionEvent();
//		tmpInteractionCoordsNow = ic.currentRayPointCoords.push();
//		computeInteractionPoints(tmpInteractionCoordsNow, event);
//		
//		InteractionEvent lastEvent = ic.getLastInteractionEvent();
//		if (lastEvent != null){
//			tmpInteractionCoordsLast = ic.lastRayPointCoords.push();
//			computeInteractionPoints(tmpInteractionCoordsLast, lastEvent);
//		}
	}
	
	@Override
	public void onUnTransformInteraction(InteractionContext ic) {
		for (int i = 0; i < InteractionContext.MAX_POINTER_COUNT; i++){
			Pointer pointer = ic.getPointers()[i];
			if (pointer.isActive()) pointer.popRayPoint();
		}
	}

	/**
	 * Use this call to limit the movement of the camera. The constrainer
	 * will be asked every frame the camera moves, so requests should be
	 * processed fast.
	 */
	public void setMovementConstrainer(ICameraMovementConstrainer movementConstrainer) {
		this.movementConstrainer = movementConstrainer;
	}
	
	public void setCameraAngle(float yAxis, float xAxis) {
		computeViewCenterOnXyPlane(tmpTargetPoint);

		tiltAngleXAxis = xAxis;
		tiltAngleYAxis = yAxis;
		float distance = Vector.distance3(eyePoint, tmpTargetPoint);

		eyePoint[0] = (float) Math.sin(RenderUtil.degreesToRadians(yAxis));
		eyePoint[1] = (float) Math.sin(RenderUtil.degreesToRadians(xAxis));
		eyePoint[2] = 1 * 
			(float) Math.cos(RenderUtil.degreesToRadians(xAxis)) * 
			(float) Math.cos(RenderUtil.degreesToRadians(yAxis));

		Vector.normalize3(eyePoint);
		Vector.scalarMultiply3(eyePoint, distance);

		Vector.addBtoA3(eyePoint, tmpTargetPoint);

		lookingDirection = Vector.aToB3(lookingDirection, eyePoint, tmpTargetPoint);
	}

	private void computeViewCenterOnXyPlane(float[] result) {
		// eyePoint + x*lookingDirection = (?,?,0)
		float x = 0;
		x  = -eyePoint[2] / lookingDirection[2];
		Vector.set3(result, lookingDirection);
		result = Vector.aPlusB3(result, eyePoint, Vector.scalarMultiply3(result, x));
	}

	public float getTiltXAxis() {
		return tiltAngleXAxis;
	}

	public float getTiltYAxis() {
		return tiltAngleYAxis;
	}
	
	@Override
	public boolean onInteraction(InteractionContext interactionContext) {
		
		
		throw new RuntimeException("Not implemented yet");
		

		
//		if (pvMatrixDirty) recomputePvMatrix();
//		InteractionEvent event = interactionContext.getCurrentInteractionEvent();
//		InteractionEvent lastEvent = interactionContext.getLastInteractionEvent();
//		int pointerCount = event.getPointerCount();
//
//		boolean ret = false;
//		
//		if (lastEvent != null){
//			if (pointerCount == 1){
//				move(interactionContext);
//			} else if (pointerCount == 2){
//				zoom(interactionContext);
//			}
//			pvMatrixDirty = true;
//		} 
//		
//		if (pointerCount >= 3){
//			tilt(interactionContext);
//			pvMatrixDirty = true;
//		}
//		
//		flingInterpolatorX = null;
//		flingInterpolatorY = null;
//		zoomSnapInterpolator = null;
//		
//		if (event.getAction() == MotionEvent.ACTION_UP){
//			computeZoomSnap(interactionContext);
//		}
//		
//		if (pointerCount == 1){
//			if (event.getAction() == MotionEvent.ACTION_UP){
//				computeFlinging(interactionContext);
//				moveEventHistory.clear();
//			} else if (event.getAction() == MotionEvent.ACTION_CANCEL){
//				moveEventHistory.clear();
//			}
//			
//			if (isZoomBackInTap(event)){
//				zoomToSnapIndex(interactionContext, ZOOM_SNAP_LEVELS.length-2);
//				centerOnXY(interactionContext, 
//						interactionContext.currentRayPointCoords.peek()[0], 
//						interactionContext.currentRayPointCoords.peek()[1]);
//				ret |= true;
//			}
//		}
//		
//		interacting = !event.isUpOrCancel();
//		
//		return ret;
	}
	
	/**
	 * Decides whether the current interaction is a tap to zoom back into the scene
	 */
	private boolean isZoomBackInTap(InteractionEvent event) {
		return (event.getAction() == MotionEvent.ACTION_UP 
				&& !event.wasTapRangeLeft() && eyePoint[2] > MAX_ZOOM_THRESHOLD);
	}

	/**
	 * Called at the end of a pinch-zoom gesture
	 */
	private void computeZoomSnap(InteractionContext interactionContext) {
		// check for large jump
		int nuIndex = 0;
		float bestDistance = FloatMath.sqrt( Math.abs(eyePoint[2]-ZOOM_SNAP_LEVELS[0]) );
		for (int i = 1; i < ZOOM_SNAP_LEVELS.length; i++){
			float distance = FloatMath.sqrt( Math.abs(eyePoint[2]-ZOOM_SNAP_LEVELS[i]) );
			if (distance < bestDistance){
				nuIndex = i;
				bestDistance = distance;
			}
		}
		
		
		if (nuIndex == lastZoomSnapIndex){
			// was this a short zoom-gesture?
			if (eyePoint[2] / ZOOM_SNAP_LEVELS[lastZoomSnapIndex] < 0.85f){
				nuIndex--;
			} else if (eyePoint[2] / ZOOM_SNAP_LEVELS[lastZoomSnapIndex] > 1.15f){
				nuIndex++;
			}
		}
		nuIndex = RenderUtil.clamp(nuIndex, 0, ZOOM_SNAP_LEVELS.length-1);
		zoomToSnapIndex(interactionContext, nuIndex);
	}

	private void zoomToSnapIndex(InteractionContext interactionContext, int index) {
		lastZoomSnapIndex = index;
		zoomSnapInterpolator = new Interpolators.LinearInterpolator(
				eyePoint[2], ZOOM_SNAP_LEVELS[index], 
				interactionContext.frameNanoTime, 
				interactionContext.frameNanoTime + ZOOM_SNAP_SPEED);
	}

	private void computeFlinging(InteractionContext interactionContext) {
		// not outside of any boundaries -> continue the current movement
        // compute the fling speed
        long historyTime = 0;
        float historyDistanceX = 0;
        float historyDistanceY = 0;
        
        long frameTime = interactionContext.frameNanoTime;
        
        MotionEventHistory.HistoryMotionEvent historyEvent;
        int historyCount = 0;
        for (; historyCount < moveEventHistory.size(); historyCount++){
            historyEvent = moveEventHistory.get(historyCount);
            historyTime = frameTime - historyEvent.time;
            historyDistanceX += historyEvent.x;
            historyDistanceY += historyEvent.y;
            if (historyTime > 200000000) break;
        }
        
        if (moveEventHistory.size() >= 2 && historyTime > 0){
        	double xFlingSpeed = (double)historyDistanceX/(double)historyTime;
        	double yFlingSpeed = (double)historyDistanceY/(double)historyTime;

        	if (Math.abs(xFlingSpeed) > MIN_FLING_SPEED 
        			|| Math.abs(yFlingSpeed) > MIN_FLING_SPEED){
	        	flingInterpolatorX = 
	                new Interpolators.AttenuationInterpolator(eyePoint[0], frameTime, frameTime + FLING_DURATION_NS, xFlingSpeed );
	        	
	        	flingInterpolatorY = 
	                new Interpolators.AttenuationInterpolator(eyePoint[1], frameTime, frameTime + FLING_DURATION_NS, yFlingSpeed );
        	}
        }
        moveEventHistory.clear();
	}

	private void computeInteractionPoints(float[] coords, InteractionEvent event) {
		int pointerCount = event.getPointerCount();
		int offset = 0;
		for (int i = 0; i < pointerCount; i++){
			pixel2ZeroPlaneCoord(coords, offset, 4, event.getPointers()[i]);
			offset += 4;
		}
	}

	public void moveTo(float centerX) {
		eyePoint[0] = centerX;
	}
	
	private float[] tmpMoveEyeDelta = new float[3];
	private void move(InteractionContext ic){
		tmpMoveEyeDelta = Vector.aToB3(tmpMoveEyeDelta, tmpInteractionCoordsNow, tmpInteractionCoordsLast);
		tmpMoveEyeDelta[2] = 0;
		moveEventHistory.add(ic.frameNanoTime, tmpMoveEyeDelta[0], tmpMoveEyeDelta[1]);
		Vector.addBtoA3(eyePoint, tmpMoveEyeDelta);
		
		clampToValidRegion();
	}
	
	protected float[] touchCenterSrc = new float[3];
	protected float[] touchCenterDst = new float[3];
	protected float[] centerToEye = new float[3];
	/**
	 * @param ic  The current interaction environment
	 */
	protected void zoom(InteractionContext ic){
		// compute start center
		Vector.average3(touchCenterSrc, tmpInteractionCoordsLast, 0, tmpInteractionCoordsLast, 3);
		// compute start distance to center
		float distanceStart = Vector.distance3(touchCenterSrc, tmpInteractionCoordsLast);
		
		// compute destination distance to center
		Vector.average3(touchCenterDst, tmpInteractionCoordsNow, 0, tmpInteractionCoordsNow, 3);
		float distanceTarget = Vector.distance3(touchCenterDst, tmpInteractionCoordsNow);
		
		// vector from center to eye-point
		Vector.aToB3(centerToEye, touchCenterSrc, eyePoint);

		// linear stretching factor
		float factor = distanceStart / distanceTarget;
		
		// new eye-point: touchCenter + factor*centerToEye
		centerToEye = Vector.scalarMultiply3(centerToEye, factor);
		// limit the zooming
		float length = Vector.length3(centerToEye);
		if (length < MIN_CAMERA_DISTANCE){
			Vector.normalize3(centerToEye);
			Vector.scalarMultiply3(centerToEye, MIN_CAMERA_DISTANCE);
		} else if (length > MAX_CAMERA_DISTANCE){
			Vector.normalize3(centerToEye);
			Vector.scalarMultiply3(centerToEye, MAX_CAMERA_DISTANCE);
		}
		// new eye-point: touchCenter + factor*centerToEye
		eyePoint = Vector.aPlusB3(eyePoint, touchCenterSrc, centerToEye);
		
		eyePoint[2] = RenderUtil.clamp(eyePoint[2], MIN_CAMERA_DISTANCE, MAX_CAMERA_DISTANCE);
		
		// also: move by center-movement
		eyePoint = Vector.addBtoA3(eyePoint, Vector.aToB3(centerToEye, touchCenterDst, touchCenterSrc));
		
		clampToValidRegion();
	}
	
	private float[] tmpTiltAverage = new float[2];
	private float[] tmpTiltDownAverage = new float[2];
	private void tilt(InteractionContext ic){
		
		throw new RuntimeException("Not implemented yet");
		

		
//		InteractionEvent ie = ic.getCurrentInteractionEvent();
//		float[][] pointers = ie.getPointers();
//		if (ie.getAction() == MotionEvent.ACTION_DOWN){
//			Vector.average2(tmpTiltDownAverage, pointers[0], pointers[1], pointers[2]);
//			downTiltXY[0] = tiltAngleXAxis;
//			downTiltXY[1] = tiltAngleYAxis;
//		} else {
//			Vector.average2(tmpTiltAverage, pointers[0], pointers[1], pointers[2]);
//			float deltaX = tmpTiltDownAverage[0] - tmpTiltAverage[0];
//			float deltaY = tmpTiltDownAverage[1] - tmpTiltAverage[1];
//
//			float xAxisDegrees = RenderUtil.clamp(downTiltXY[0] -
//					deltaY*TILT_FACTOR * MAX_TILT_X_AXIS, -MAX_TILT_X_AXIS, 0);
//			float yAxisDegrees = RenderUtil.clamp(downTiltXY[1] + 
//					deltaX*TILT_FACTOR * MAX_TILT_Y_AXIS, -MAX_TILT_Y_AXIS, MAX_TILT_Y_AXIS);
//
//			setCameraAngle(yAxisDegrees, xAxisDegrees);
//		}
	}
	
	protected void clampToValidRegion() {
		if (movementConstrainer != null){
			pvMatrixDirty |= movementConstrainer.clamp(eyePoint);
		}
	}

	protected float[] eyeToMovedPointRay = new float[4];
	protected float[] movedPoint = new float[4];
	protected float[] focusPlanePoint = new float[4];
	/**
	 * Computes the point on the z==0 plane to a pixel coord.
	 * <b>NOTE<b> When result is at least 4-dimensional, the distance between eye
	 * and that computed point is stored in result[3]
	 */
	protected void pixel2ZeroPlaneCoord(float[] result, int resultOffset, int resultVectorSize, float[] pixelXy){
		// compute point at near plane
		focusPlanePoint[0] = ((pixelXy[0] - surfaceWidth/2) / (surfaceWidth/2)) * surfaceRatio * nearPlaneFactor;
		focusPlanePoint[1] = ((surfaceHeight/2 - pixelXy[1]) / (surfaceHeight/2)) * nearPlaneFactor;
		focusPlanePoint[2] = -nearPlane;
		focusPlanePoint[3] = 1;
		
		Matrix.multiplyMV(movedPoint, 0, invertedViewMatrix, 0, focusPlanePoint, 0);
		Vector.normalize4(movedPoint);
		
		Vector.aToB3(eyeToMovedPointRay, eyePoint, movedPoint);
		
		// now, compute, where this ray crosses the xy-plane
		// EP + x * EtM = (x1, x2, 0)
		float x = - (eyePoint[2] / eyeToMovedPointRay[2]);
		
		result[resultOffset] = eyePoint[0] + x * eyeToMovedPointRay[0];
		result[resultOffset + 1] = eyePoint[1] + x * eyeToMovedPointRay[1];
		result[resultOffset + 2] = eyePoint[2] + x * eyeToMovedPointRay[2];
		
		if (x < 0){		// crossed the plane behind the camera!
			result[resultOffset] = (pixelXy[0] < surfaceWidth/2) ? -1000000f : 1000000f;
			result[resultOffset + 1] = (pixelXy[1] < surfaceHeight/2) ? 1000000f : -1000000f;
		}
		
		if (resultVectorSize >= 4)
			result[resultOffset + 3] = Vector.length3(Vector.aToB3(movedPoint, eyePoint, result, resultOffset));
	}

	/**
	 * Projects the coord v with the camera's viewProjectionMatrix and
	 * stores the result in <code>result</code>.
	 */
	public void project(float[] result, float[] v) {
		if (pvMatrixDirty) recomputePvMatrix();
		Matrix.multiplyMV(result, 0, tmpPvMatrix, 0, v, 0);
	}
	
	public void resetView(float x) {
		flingInterpolatorX = null;
		flingInterpolatorY = null;
		setCameraAngle(0, 0);
		eyePoint[0] = x;
		eyePoint[1] = 0;
		eyePoint[2] = START_CAMERA_DISTANCE;
		
		pvMatrixDirty = true;
	}

	@Override
	public void pixel2ZeroPlaneCoord(float[] result, float[] pixelXY) {
		pixel2ZeroPlaneCoord(result, 0, result.length, pixelXY);
	}
	
	public void centerOnXY(SceneGraphContext sc, float x, float y){
		long frameTime = sc.frameNanoTime;
    	flingInterpolatorX = 
            new Interpolators.HyperbelInterpolator(eyePoint[0], x, frameTime, frameTime + CAMERA_CENTER_DURATION_NS);
    	
    	flingInterpolatorY = 
            new Interpolators.HyperbelInterpolator(eyePoint[1], y, frameTime, frameTime + CAMERA_CENTER_DURATION_NS);
	}
	
	public void centerOnY(SceneGraphContext sc, float y) {
		long frameTime = sc.frameNanoTime;
    	flingInterpolatorY = 
            new Interpolators.HyperbelInterpolator(eyePoint[1], y, frameTime, frameTime + CAMERA_CENTER_DURATION_NS);
    	interacting = false;
	}
	
	public void centerOnX(SceneGraphContext sc, float x){
		long frameTime = sc.frameNanoTime;
    	flingInterpolatorX = 
            new Interpolators.HyperbelInterpolator(eyePoint[0], x, frameTime, frameTime + CAMERA_CENTER_DURATION_NS);
    	interacting = false;
	}
	
	/**
	 * Objects of this type may be assigned to a CameraNode to limit its movement
	 * space. The according clamp functions will be called each frame.
	 */
	public static interface ICameraMovementConstrainer {
		/**
		 * @return	Return true if any aspect of the camera's position was changed, false
		 * when the camera's movement was not clamped.
		 */
		boolean clamp(float[] eyePoint);
	}
}
