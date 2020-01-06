package com.komaxx.komaxx_gl.scenegraph.basic_nodes;

import com.komaxx.komaxx_gl.math.GlRect;
import com.komaxx.komaxx_gl.scenegraph.basic_nodes.CameraNode.ICameraMovementConstrainer;
import com.komaxx.komaxx_gl.util.RenderUtil;

/**
 * With this constrainer assigned to a CameraNode, it's eye-point can not
 * leave the inside of a pyramid sitting orthogonal on the xy-plane.
 * 
 * @author Matthias Schicker
 */
public class PyramidCameraConstrainer implements ICameraMovementConstrainer {
	private final GlRect z0area;
	private final float pyramidHeight;

	public PyramidCameraConstrainer(GlRect z0area, float pyramidHeight){
		this.z0area = z0area;
		this.pyramidHeight = pyramidHeight;
	}

	@Override
	public boolean clamp(float[] eyePoint) {
		float preX = eyePoint[0];
		float preY = eyePoint[1];
		
		// compute rect on this height
		// the movement constraints depend on the distance from the xy-plane.
		float scaleFactor = getScaleFactor(eyePoint[2]);

		float width = z0area.width() * scaleFactor;
		float leftConstrain = z0area.centerX() - width/2;
		eyePoint[0] = RenderUtil.clamp(eyePoint[0], leftConstrain, leftConstrain + width);
		
		float height = z0area.height() * scaleFactor;
		float bottomConstrain = z0area.centerY() - height/2;
		eyePoint[1] = RenderUtil.clamp(eyePoint[1], bottomConstrain, bottomConstrain + height);
		
		return preX!=eyePoint[0] || preY!=eyePoint[1];
	}

	private float getScaleFactor(float cameraHeight) {
		return (pyramidHeight-cameraHeight) / pyramidHeight;
	}
}
