package com.komaxx.komaxx_gl.bound_meshes;

import java.nio.ShortBuffer;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.math.GlCube;
import com.komaxx.komaxx_gl.math.GlRect;
import com.komaxx.komaxx_gl.math.Vector;
import com.komaxx.komaxx_gl.primitives.TexturedQuad;
import com.komaxx.komaxx_gl.util.InterpolatedValue;
import com.komaxx.komaxx_gl.util.InterpolatedValue.AnimationType;

/**
 * About the same as a BoundTexturedQuad, but with the difference
 * that all position changes are animated.
 * 
 * @author Matthias Schicker
 */
public class AnimatedBoundTexturedQuad extends BoundTexturedQuad {
    protected GlCube startPosition = new GlCube();
    protected GlCube targetPosition = new GlCube();
    protected InterpolatedValue posTransition = 
    	new InterpolatedValue(AnimationType.INVERSE_SQUARED, 0, InterpolatedValue.ANIMATION_DURATION_NORMAL);

    private static final GlCube tmpCube = new GlCube();
    private static final float[] tmpVector = new float[4];
	
	private GlCube getCurrentPosition() {
		float phase = posTransition.getLast();
		if (phase == 0){
			tmpCube.set(startPosition);
		} else if (phase >= 1f){
			tmpCube.set(targetPosition);
		} else {
			// ulf
			Vector.aMinusB3(tmpVector, targetPosition.ulf, startPosition.ulf);
			Vector.aPlusB3(tmpCube.ulf, startPosition.ulf, Vector.scalarMultiply3(tmpVector, phase));
			
			// lrb
			Vector.aMinusB3(tmpVector, targetPosition.lrb, startPosition.lrb);
			Vector.aPlusB3(tmpCube.lrb, startPosition.lrb, Vector.scalarMultiply3(tmpVector, phase));
		}
		
		return tmpCube;
	}

	@Override
	public void positionXY(float left, float top, float right, float bottom) {
		startPosition.set(getCurrentPosition());
		targetPosition.setXY(left, top, right, bottom);
		posTransition.setDirect(0);
		posTransition.set(1);
		positionDirty = true;
	}
	
	@Override
	public void positionX(float left, float right) {
		startPosition.set(getCurrentPosition());
		targetPosition.setX(left, right);
		posTransition.setDirect(0);
		posTransition.set(1);
		positionDirty = true;
	}
	
	@Override
	public void positionXY(GlRect nuPosition) {
		positionXY(nuPosition.left, nuPosition.top, nuPosition.right, nuPosition.bottom);
	}

	@Override
	public void position(float left, float top, float front, float right, float bottom, float back) {
		startPosition.set(getCurrentPosition());
		targetPosition.set(left, top, front, right, bottom, back);
		posTransition.setDirect(0);
		posTransition.set(1);
		positionDirty = true;
    }
    
	@Override
	public void position(GlCube nuPos) {
		startPosition.set(getCurrentPosition());
		targetPosition.set(nuPos);
		posTransition.setDirect(0);
		posTransition.set(1);
		positionDirty = true;
	}
	
    @Override
	public void positionZ(float front, float back) {
		startPosition.set(getCurrentPosition());
		targetPosition.setZ(front, back);
		posTransition.setDirect(0);
		posTransition.set(1);
		positionDirty = true;
    }

	public void positionDirectZ(float f, float b) {
		startPosition.setZ(f, b);
		targetPosition.setZ(f, b);
		positionDirty = true;
	}
    
	@Override
	public int render(RenderContext rc, ShortBuffer frameIndexBuffer){
		if (!visible) return 0;
		
		boolean vboDirty = false;
		
		if (positionDirty){
            TexturedQuad.position(vertexBuffer, 0, getPosition(rc.frameNanoTime));
			positionDirty = !posTransition.isDone(rc.frameNanoTime);
			vboDirty = true;
		}
		
		if (alphaDirty){
			float nowAlpha = alpha.get(rc.frameNanoTime);
			TexturedQuad.setAlpha(vertexBuffer, 0, nowAlpha, nowAlpha, nowAlpha, nowAlpha);
			alphaDirty = !alpha.isDone(rc.frameNanoTime);
			vboDirty = true;
		}
		
		if (alpha.getLast() < 0.1f) return 0;
		
		if (texCoordsDirty){
			if (rotateTexCoords)
				TexturedQuad.setUVMappingRotated(vertexBuffer, 0, texCoordsUv);
			else
				TexturedQuad.setUVMapping(vertexBuffer, 0, texCoordsUv);
			texCoordsDirty = false;
			vboDirty = true;
		}
		
		if (vboDirty){
			vertexBuffer.position(0);
			GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 
					firstByteIndex, 
					vertexBuffer.capacity()*4, 
					vertexBuffer);

			vboDirty = false;
		}
		
		frameIndexBuffer.put(indexBuffer);
		return TexturedQuad.INDICES_COUNT;
	}

	@Override
	public boolean contains(float x, float y) {
		return targetPosition.containsXY(x, y);
	}
	
	@Override
	public boolean containsY(float y) {
		return targetPosition.containsY(y);
	}

    @Override
	public GlCube getPosition() {
        return getCurrentPosition();
    }

    public GlCube getTargetPosition() {
		return targetPosition;
	}
    
    public GlCube getPosition(long nanoTime) {
    	posTransition.get(nanoTime);
        return getCurrentPosition();
    }

	public void shortcut() {
		posTransition.shortcut();
	}
	
	public void setPositionAnimationDuration(long nuDuration){
		posTransition.setDuration(nuDuration);
	}
}
