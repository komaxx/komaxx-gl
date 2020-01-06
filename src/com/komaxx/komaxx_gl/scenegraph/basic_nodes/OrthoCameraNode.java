package com.komaxx.komaxx_gl.scenegraph.basic_nodes;

import android.opengl.Matrix;

import com.komaxx.komaxx_gl.ICameraInfoProvider;
import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.SceneGraphContext;
import com.komaxx.komaxx_gl.math.MatrixUtil;
import com.komaxx.komaxx_gl.scenegraph.Node;
import com.komaxx.komaxx_gl.scenegraph.interaction.InteractionContext;
import com.komaxx.komaxx_gl.scenegraph.interaction.Pointer;
import com.komaxx.komaxx_gl.util.ObjectsStore;

/**
 * This simple camera just provides a screen aligned ortho matrix.
 * 
 * @author Matthias Schicker
 */
public class OrthoCameraNode extends Node implements ICameraInfoProvider {
	// control the clipping planes and the depth buffer precision in here!
	private float nearPlane = 5000;
	private float farPlane = -5000;

	private float[] pvMatrix = new float[MatrixUtil.MATRIX_SIZE];
	
	private float[] upVector = new float[]{ 0f, 1f, 0f };
	
	// caches
	private float[] tmpViewMatrix = new float[MatrixUtil.MATRIX_SIZE];
	private float surfaceWidth = 0;
	private float surfaceHeight = 0;
	
	/**
	 * When true, the node does not translate world coords to pixel coords
	 * but to some other given size.
	 */
	private boolean fixSize = false;
	private float[] fixSizes = new float[]{ 1f, 0.6f };
	
	public OrthoCameraNode() {
		handlesInteraction = false;
		transforms = true;
	}
	
	@Override
	public boolean onTransform(SceneGraphContext sgContext) {
		sgContext.setCameraInfoProvider(this);
		
		sgContext.modelMatrixStack.push();
		
		float[] stackPvMatrix = sgContext.projectionViewMatrixStack.push();
		System.arraycopy(pvMatrix, 0, stackPvMatrix, 0, MatrixUtil.MATRIX_SIZE);
		
		float[] eyePoint = sgContext.eyePointStack.push();
		eyePoint[0] = surfaceWidth/2;
		eyePoint[1] = surfaceHeight/2;
		// technically, there is no eyepoint. However, when moving the assumed eyepoint
		// very far away, the perspective and the orthographic projection get reasonably
		// similar regarding ray casting, so that the interaction will still work.
		eyePoint[2] = 100000;
		
		sgContext.setMvpMatrixDirty();
		
		return true;
	}

	@Override
	public void onUnTransform(SceneGraphContext sgContext) {
		sgContext.modelMatrixStack.pop();
		sgContext.projectionViewMatrixStack.pop();
		sgContext.eyePointStack.pop();
		sgContext.setMvpMatrixDirty();
	}
	
	@Override
	public void onTransformInteraction(InteractionContext ic) {
		for (int i = 0; i < InteractionContext.MAX_POINTER_COUNT; i++){
			Pointer pointer = ic.getPointers()[i];
			if (!pointer.isActive()) continue;
			float[] pushRayPoint = pointer.pushRayPoint();
			pushRayPoint[0] = pushRayPoint[0] / ic.surfaceWidth * surfaceWidth;
			pushRayPoint[1] = - pushRayPoint[1] / ic.surfaceHeight * surfaceHeight;
			pushRayPoint[3] = 1;
		}
	}
	
	@Override
	public void onUnTransformInteraction(InteractionContext ic) {
		for (int i = 0; i < InteractionContext.MAX_POINTER_COUNT; i++){
			Pointer pointer = ic.getPointers()[i];
			if (pointer.isActive()) pointer.popRayPoint();
		}
	}
	
	@Override
	protected void onSurfaceCreated(RenderContext renderContext) {
		recreatePvMatrix(renderContext);
	}
	
	@Override
	public void onSurfaceChanged(RenderContext renderContext) {
		recreatePvMatrix(renderContext);
	}
	
	private void recreatePvMatrix(RenderContext renderContext) {
		if (fixSize){
			surfaceWidth = fixSizes[0];
			surfaceHeight = fixSizes[1];
		} else {
			surfaceWidth = renderContext.surfaceWidth;
			surfaceHeight = renderContext.surfaceHeight;
		}

        Matrix.orthoM(pvMatrix, 0, -surfaceWidth/2, surfaceWidth/2, -surfaceHeight/2, surfaceHeight/2, nearPlane, farPlane);
       	Matrix.setLookAtM(tmpViewMatrix, 0, 
				surfaceWidth/2, -surfaceHeight/2, 10, 
				surfaceWidth/2, -surfaceHeight/2, 0, 
				upVector[0], upVector[1], upVector[2]);
       	
       	float[] pvMatrixClone = ObjectsStore.getCloneMatrix(pvMatrix);
       	Matrix.multiplyMM(pvMatrix, 0, pvMatrixClone, 0, tmpViewMatrix, 0);
       	ObjectsStore.recycleMatrix(pvMatrixClone);
	}

	@Override
	public void pixel2ZeroPlaneCoord(float[] result, float[] pixelXY) {
		result[0] = pixelXY[0];
		result[1] = -pixelXY[1];
	}
	
	public void setFixSize(boolean toggle, float fixWidth, float fixHeight){
		fixSize = toggle;
		fixSizes[0] = fixWidth;
		fixSizes[1] = fixHeight;
	}
}
