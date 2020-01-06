package com.komaxx.komaxx_gl.scenegraph.basic_nodes;

import android.opengl.Matrix;

import com.komaxx.komaxx_gl.SceneGraphContext;
import com.komaxx.komaxx_gl.math.MatrixUtil;
import com.komaxx.komaxx_gl.math.Vector;
import com.komaxx.komaxx_gl.scenegraph.Node;
import com.komaxx.komaxx_gl.scenegraph.interaction.InteractionContext;
import com.komaxx.komaxx_gl.scenegraph.interaction.Pointer;
import com.komaxx.komaxx_gl.util.ObjectsStore;

/**
 * This node has it's own transformation matrix. This matrix will be applied
 * when rendering and also affect all following nodes in the branch. Does not
 * have any specific Rendering capabilities otherwise.
 * 
 * @author Matthias Schicker
 */
public class RotationNode extends Node {
	private float[] transformationMatrix = MatrixUtil.buildMatrix();
	private float[] transposedMatrix = MatrixUtil.buildMatrix();
	
	private int changeId = 0; 
	
	public RotationNode(){
		transforms = true;
	}
	
	@Override
	public boolean onTransform(SceneGraphContext sgContext) {
		sgContext.worldIdStack.push().add(changeId);
		
		float[] modelMatrix = sgContext.modelMatrixStack.push();
		float[] modelClone = ObjectsStore.getCloneMatrix(modelMatrix);
		Matrix.multiplyMM(modelMatrix, 0, modelClone, 0, transformationMatrix, 0);
		
		sgContext.setMvpMatrixDirty();
		
		inverseTranslate(sgContext.eyePointStack.push());
		
		ObjectsStore.recycleMatrix(modelClone);
		return true;
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
	
	private static float[] tmpVector = new float[4];
	private void inverseTranslate(float[] v4){
		Matrix.multiplyMV(tmpVector, 0, transposedMatrix, 0, v4, 0);
		Vector.set4(v4, tmpVector);
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
	
	public void setRotation(float angleDegrees, float axisX, float axisY, float axisZ) {
		Matrix.setIdentityM(transformationMatrix, 0);
		Matrix.setRotateM(transformationMatrix, 0, angleDegrees, axisX, axisY, axisZ);
		Matrix.transposeM(transposedMatrix, 0, transformationMatrix, 0);
		changeId++;
	}
}
