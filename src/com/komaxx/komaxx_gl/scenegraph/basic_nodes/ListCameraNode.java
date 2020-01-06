package com.komaxx.komaxx_gl.scenegraph.basic_nodes;

import android.opengl.Matrix;

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

/**
 * This is a special simplified CameraNode that is suitable for list
 * views. It aligns itself in such a way that in the z0 one pixel
 * corresponds to exactly one virtual unit.
 * 
 * @author Matthias Schicker
 */
public class ListCameraNode extends Node implements ICameraInfoProvider {
	private static final long CAMERA_CENTER_DURATION_NS = 500000000L;

	/**
	 * When the movement delta is smaller than this, no flinging is executed.
	 * (worldX per NS)
	 */
	private static final float MIN_FLING_SPEED = 0.00000001f;
	private static final long FLING_DURATION_NS = 2000000000L;

	private float fovAngleX = 40f;		// degrees
	
	protected float[] projectionMatrix = new float[MatrixUtil.MATRIX_SIZE];
	protected float[] viewMatrix = new float[MatrixUtil.MATRIX_SIZE];
	protected float[] invertedViewMatrix = new float[MatrixUtil.MATRIX_SIZE];
	private float[] modelMatrix = new float[MatrixUtil.MATRIX_SIZE];
	
	protected float[] eyePoint = new float[]{ 0f, 0f, 1234f };
	protected float[] lookingDirection = new float[]{ 0f, 0f, -1f };
	protected float[] upVector = new float[]{ 0f, 1f, 0f };
	
	protected boolean pvMatrixDirty = true;
	/**
	 *  Whenever the perspective of the camera changes, this is increased. This way, nodes further
	 *  down in the graph know that they may recompute stuff based on the new perspective.
	 */
	protected int worldChangeId = 0; 
	
	protected boolean interacting = false;
	protected long lastInteractionTime = 0;
	private MotionEventHistory moveEventHistory = new MotionEventHistory(15);
	private Interpolators.Interpolator flingInterpolatorY;

	private float scrollTopLimit = 0;
	private float scrollBottomLimit = Integer.MIN_VALUE;
	
	/**
	 * Caches the current viewProjection matrix
	 */
	protected float[] tmpPvMatrix = new float[MatrixUtil.MATRIX_SIZE];
	
	protected float[] tmpInteractionCoordsNow;
	protected float[] tmpInteractionCoordsLast;
	protected float[] downTiltXY = new float[2];

	// ///////////////////////////////////////
	// tmps, caches
	protected float[] tmpTargetPoint = new float[3];
	
	protected float surfaceWidth = 0;
	protected float surfaceHeight = 0;
	protected float surfaceRatio = 0;

	
	public ListCameraNode(){
		Matrix.setIdentityM(modelMatrix, 0);
		// just start with something that makes almost sense, will be corrected with the first frame.
		Matrix.orthoM(projectionMatrix, 0, -1, 1, -1, 1, 1, 50);
		recomputeViewMatrix();
		transforms = true;
		handlesInteraction = true;
	}
	
	public void setScrollLimits(float topLimit, float bottomLimit){
		scrollTopLimit = topLimit;
		scrollBottomLimit = bottomLimit;
		
		sanitizeScrollLimits();
	}

	/**
	 * Ensures that no erratic behavior appears due to too short content
	 * or an erroneous "bottom > top".
	 */
	private void sanitizeScrollLimits() {
		if (scrollTopLimit - scrollBottomLimit < surfaceHeight){
			scrollBottomLimit = scrollTopLimit - surfaceHeight;
		}
	}

	protected void recomputeViewMatrix() {
		tmpTargetPoint = Vector.aPlusB3(tmpTargetPoint, eyePoint, lookingDirection);
		Matrix.setLookAtM(viewMatrix, 0, 
				eyePoint[0], eyePoint[1], eyePoint[2], 
				tmpTargetPoint[0], tmpTargetPoint[1], tmpTargetPoint[2], 
				upVector[0], upVector[1], upVector[2]);
		
		Matrix.translateM(viewMatrix, 0, -surfaceWidth/2, +surfaceHeight/2, 0);
		
		Matrix.invertM(invertedViewMatrix, 0, viewMatrix, 0);
	}

	@Override
	public boolean onTransform(SceneGraphContext sgContext) {
		sgContext.setCameraInfoProvider(this);
		
		// this ensures that interacting is reset to false when the interactionEvents were
		// consumed elsewhere
		if (interacting && sgContext.frameNanoTime - lastInteractionTime > 100000000) interacting = false;

		if (!interacting) executeFlinging(sgContext);
		
		if (pvMatrixDirty){
			recomputePvMatrix();
			worldChangeId++;
			sgContext.setNotIdle();
		}
		
		// when the cam is hidden while interacting, the 'cancel' will never reach this node
		// fallback to do it here.
		if (!visible || !sgContext.visibilityStack.peek().b) interacting = false;
		
		if (interacting){
			worldChangeId++;	// this avoids that other nodes perceive the state as idle
			sgContext.setNotIdle();
		}
		
		sgContext.worldIdStack.push().add(worldChangeId);
		
		float[] sgEyePoint = sgContext.eyePointStack.push();
		Vector.set3(sgEyePoint, eyePoint[0]+surfaceWidth/2, eyePoint[1]-surfaceHeight/2, eyePoint[2]);
		sgEyePoint[3] = 1;
		
		float[] stackViewMatrix = sgContext.projectionViewMatrixStack.push();
		System.arraycopy(tmpPvMatrix, 0, stackViewMatrix, 0, MatrixUtil.MATRIX_SIZE);
		
		sgContext.setMvpMatrixDirty();
		
		return true;
	}
	
	protected void executeFlinging(SceneGraphContext sgContext) {
		long frameTime = sgContext.frameNanoTime;
		pvMatrixDirty |= executeFlingingY(frameTime);
	}

	private boolean executeFlingingY(long frameTime) {
		if (flingInterpolatorY == null) return false;
		eyePoint[1] = flingInterpolatorY.getValue(frameTime);

		if (eyePoint[1] > scrollTopLimit && flingInterpolatorY.getEndY() > eyePoint[1]){
			// moving outside of the bounds -> clamp
			eyePoint[1] = scrollTopLimit;
			flingInterpolatorY = null;
		} else if (eyePoint[1]-surfaceHeight < scrollBottomLimit && flingInterpolatorY.getEndY() < eyePoint[1]){
			eyePoint[1] = scrollBottomLimit+surfaceHeight;
			flingInterpolatorY = null;
		} else if (frameTime > flingInterpolatorY.getEndX())  flingInterpolatorY = null;
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

        setFovAngle(fovAngleX);
        sanitizeScrollLimits();
	}
	
	/**
	 * Adjust the horizontal FOV of the camera. The focus plane will not be changed, it's still
	 * the z=0 plane, so the camera distance will change! </br>
	 * Think <i>Hitchcock effect ;)</i> </br>
	 * <b>NOTE:</b> GL thread only.
	 * @param nuFovAngle	the new angle in degrees
	 */
	public void setFovAngle(float nuFovAngle){
		fovAngleX = nuFovAngle;

		if (surfaceWidth == 0) return;		// would fuck up the frustum!
		
        float cameraDistance = (float) ((surfaceWidth/2f) / Math.tan(Math.toRadians(fovAngleX)));
        eyePoint[2] = cameraDistance;

        float NEAR_PLANE_DISTANCE_FACTOR = 0.5f;
        float FAR_PLANE_DISTANCE_FACTOR = 10f;
        
        float nearPlaneDistance = cameraDistance * NEAR_PLANE_DISTANCE_FACTOR;
        float farPlaneDistance = cameraDistance * FAR_PLANE_DISTANCE_FACTOR;
        
       	Matrix.frustumM(projectionMatrix, 0, 
       			- surfaceWidth/2 * NEAR_PLANE_DISTANCE_FACTOR, surfaceWidth/2 * NEAR_PLANE_DISTANCE_FACTOR, 
       			- surfaceHeight/2 * NEAR_PLANE_DISTANCE_FACTOR, surfaceHeight/2 * NEAR_PLANE_DISTANCE_FACTOR,
        			nearPlaneDistance, farPlaneDistance);
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

	@Override
	public boolean onInteraction(InteractionContext interactionContext) {
		
		throw new RuntimeException("Not implemented yet");
		
//		lastInteractionTime = interactionContext.frameNanoTime;
//		if (pvMatrixDirty) recomputePvMatrix();
//		InteractionEvent event = interactionContext.getCurrentInteractionEvent();
//		InteractionEvent lastEvent = interactionContext.getLastInteractionEvent();
//		int pointerCount = event.getPointerCount();
//
//		boolean ret = false;
//		
//		if (lastEvent != null && pointerCount == 1){
//			move(interactionContext);
//			pvMatrixDirty = true;
//		} 
//		
//		flingInterpolatorY = null;
//		
//		if (pointerCount == 1){
//			if (event.getAction() == MotionEvent.ACTION_UP){
//				computeFlinging(interactionContext);
//				moveEventHistory.clear();
//			} else if (event.getAction() == MotionEvent.ACTION_CANCEL){
//				moveEventHistory.clear();
//			}
//		}
//
//		// snap back to allowed area
//		if (event.isUpOrCancel() && eyePoint[1] > scrollTopLimit){
//			centerOnY(interactionContext, scrollTopLimit -interactionContext.getZBoundingBox().height()/2);
//		} else if (event.isUpOrCancel() && eyePoint[1]-surfaceHeight < scrollBottomLimit){
//			centerOnY(interactionContext, scrollBottomLimit + interactionContext.getZBoundingBox().height()/2);
//		}
//		
//		interacting = !event.isUpOrCancel();
//		
//		return ret;
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
			coords[offset] = event.getPointers()[i][0] + eyePoint[0];
			coords[offset+1] = -event.getPointers()[i][1] + eyePoint[1];
			
			offset += 3;
		}
	}

	private float[] tmpMoveEyeDelta = new float[3];
	private void move(InteractionContext ic){
		tmpMoveEyeDelta = Vector.aToB3(tmpMoveEyeDelta, tmpInteractionCoordsNow, tmpInteractionCoordsLast);
		tmpMoveEyeDelta[2] = 0;
		moveEventHistory.add(ic.frameNanoTime, tmpMoveEyeDelta[0], tmpMoveEyeDelta[1]);
		tmpMoveEyeDelta[0] = 0;
		Vector.addBtoA3(eyePoint, tmpMoveEyeDelta);
	}
	
	@Override
	public void pixel2ZeroPlaneCoord(float[] result, float[] pixelXY) {
		result[0] = pixelXY[0];
		result[1] = - (pixelXY[1] - eyePoint[1]);
	}
	
	public void centerOnY(SceneGraphContext sc, float y) {
		long frameTime = sc.frameNanoTime;
    	flingInterpolatorY = 
            new Interpolators.HyperbelInterpolator(
            		eyePoint[1], y + surfaceHeight/2, 
            		frameTime, frameTime + CAMERA_CENTER_DURATION_NS);
    	interacting = false;
	}
}
