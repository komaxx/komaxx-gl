package com.komaxx.komaxx_gl.scenegraph.basic_nodes;

import android.opengl.Matrix;

import com.komaxx.komaxx_gl.SceneGraphContext;
import com.komaxx.komaxx_gl.math.MatrixUtil;
import com.komaxx.komaxx_gl.scenegraph.Node;
import com.komaxx.komaxx_gl.scenegraph.interaction.InteractionContext;
import com.komaxx.komaxx_gl.scenegraph.interaction.Pointer;
import com.komaxx.komaxx_gl.util.InterpolatedValue;
import com.komaxx.komaxx_gl.util.ObjectsStore;
import com.komaxx.komaxx_gl.util.InterpolatedValue.AnimationType;

/**
 * This node has it's own transformation matrix. This matrix will be applied
 * when rendering and also affect all following nodes in the branch. Does not
 * have any specific Rendering capabilities otherwise.
 * 
 * @author Matthias Schicker
 */
public class AnimatedScaleNode extends Node {
	private float[] transformationMatrix = MatrixUtil.buildMatrix();
	
	private int changeId = 0;
	
	private boolean scaleDirty = true;
	
	protected InterpolatedValue scale = new InterpolatedValue(AnimationType.OVERBUMP, 1);
	
	public AnimatedScaleNode(){
		transforms = true;
	}
	
	@Override
	public boolean onTransform(SceneGraphContext sgContext) {
		float[] modelMatrix = sgContext.modelMatrixStack.push();
		
		if (scaleDirty){
			recomputeTransformationMatrix(sgContext);
		}
		
		sgContext.worldIdStack.push().add(changeId);
		
		float[] cloneMatrix = ObjectsStore.getCloneMatrix(modelMatrix);
		Matrix.multiplyMM(modelMatrix, 0, cloneMatrix, 0, transformationMatrix, 0);
		ObjectsStore.recycleMatrix(cloneMatrix);
		
		sgContext.setMvpMatrixDirty();
		
		inverseTranslate(sgContext.eyePointStack.push());
		
		return true;
	}
	
	private void recomputeTransformationMatrix(SceneGraphContext sgContext) {
		Matrix.setIdentityM(transformationMatrix, 0);
		float nowScale = scale.get(sgContext.frameNanoTime);
		Matrix.scaleM(transformationMatrix, 0, nowScale, nowScale, nowScale);
		changeId++;	
		
		scaleDirty = !scale.isDone(sgContext.frameNanoTime);
	}

	@Override
	public void onTransformInteraction(InteractionContext interactionContext) {
		for (int i = 0; i < InteractionContext.MAX_POINTER_COUNT; i++){
			Pointer pointer = interactionContext.getPointers()[i];
			if (!pointer.isActive()) continue;
			float[] rayPoint = pointer.pushRayPoint();
			inverseTranslate(rayPoint);
		}
	}
	
	private void inverseTranslate(float[] v4){
		float invScale = scale.getLast();
		if (invScale != 0) invScale = 1f/invScale;
		v4[0] *= invScale;
		v4[1] *= invScale;
		v4[2] *= invScale;
	}
	
	@Override
	public void onUnTransform(SceneGraphContext sgContext) {
		sgContext.eyePointStack.pop();
		sgContext.modelMatrixStack.pop();
		sgContext.worldIdStack.pop();
		sgContext.setMvpMatrixDirty();
	}
	
	@Override
	public void onUnTransformInteraction(InteractionContext interactionContext) {
		for (int i = 0; i < InteractionContext.MAX_POINTER_COUNT; i++){
			Pointer pointer = interactionContext.getPointers()[i];
			if (pointer.isActive()) pointer.popRayPoint();
		}
	}

	public void setScale(float nuScale){
		if (nuScale != scale.getTarget()){ 
			scale.set(nuScale);
			scaleDirty = true;
		}
	}

	public void setScaleDirect(float nuScale){
		scale.setDirect(nuScale);
		scaleDirty = true;
	}
	
	public void setAnimationSpeed(long nuExecutionTime){
		scale.setDuration(nuExecutionTime);
	}
}
