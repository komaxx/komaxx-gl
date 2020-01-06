package com.komaxx.komaxx_gl.bound_meshes;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.math.GlCube;
import com.komaxx.komaxx_gl.math.GlRect;
import com.komaxx.komaxx_gl.math.Vector;
import com.komaxx.komaxx_gl.primitives.TexturedQuad;
import com.komaxx.komaxx_gl.texturing.TextureSegment;
import com.komaxx.komaxx_gl.util.InterpolatedValue;
import com.komaxx.komaxx_gl.util.InterpolatedValue.AnimationType;

/**
 * About the same as a BoundSharedTexturedQuad, but with the difference
 * that all position changes are animated. Call shortcut() to place
 * with immediate effect.
 * 
 * @author Matthias Schicker
 */
public class AnimatedBoundSharedTexturedQuad extends ABoundMesh {
	protected static FloatBuffer vertexBuffer = TexturedQuad.allocateQuads(1);
	
    protected GlCube startPosition = new GlCube();
    protected GlCube targetPosition = new GlCube();
    protected InterpolatedValue posTransition = 
    	new InterpolatedValue(AnimationType.INVERSE_SQUARED, 0, InterpolatedValue.ANIMATION_DURATION_NORMAL);

    protected RectF texCoordsUv = new RectF();
    protected boolean rotateTexCoords = false;
	protected final InterpolatedValue alpha = new InterpolatedValue(AnimationType.INVERSE_SQUARED, 1f, InterpolatedValue.ANIMATION_DURATION_SLOW);
	private Rect texCoordsPx = new Rect();

	protected boolean dirty = true;
    
    private static final GlCube tmpCube = new GlCube();
    private static final float[] tmpVector = new float[4];
	
    public AnimatedBoundSharedTexturedQuad(){
    	createIndexBuffer();
    }
    
	public GlCube getCurrentPosition() {
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

	public void setAlpha(float nuAlpha){
		alpha.set(nuAlpha);
		dirty = true;
	}
	
	public void setAlphaDirect(float nuAlpha) {
		alpha.setDirect(nuAlpha);
		dirty = true;
	}

	public void setAlphaAnimationDuration(long nuDuration) {
		alpha.setDuration(nuDuration);
	}

    public float getAlpha() {
        return this.alpha.getLast();
    }

    public float getAlpha(long time) {
        return this.alpha.get(time);
    }
    
	public float getTargetAlpha() {
		return alpha.getTarget();
	}
	
	public void positionXY(float left, float top, float right, float bottom) {
		startPosition.set(getCurrentPosition());
		targetPosition.setXY(left, top, right, bottom);
		posTransition.setDirect(0);
		posTransition.set(1);
		dirty = true;
	}
	
	public void positionX(float left, float right) {
		startPosition.set(getCurrentPosition());
		targetPosition.setX(left, right);
		posTransition.setDirect(0);
		posTransition.set(1);
		dirty = true;
	}
	
	public void positionXY(GlRect nuPosition) {
		positionXY(nuPosition.left, nuPosition.top, nuPosition.right, nuPosition.bottom);
	}

	public void position(float left, float top, float front, float right, float bottom, float back) {
		startPosition.set(getCurrentPosition());
		targetPosition.set(left, top, front, right, bottom, back);
		posTransition.setDirect(0);
		posTransition.set(1);
		dirty = true;
    }
    
	public void position(GlCube nuPos) {
		startPosition.set(getCurrentPosition());
		targetPosition.set(nuPos);
		posTransition.setDirect(0);
		posTransition.set(1);
		dirty = true;
	}
	
	public void positionZ(float front, float back) {
		startPosition.set(getCurrentPosition());
		targetPosition.setZ(front, back);
		posTransition.setDirect(0);
		posTransition.set(1);
		dirty = true;
    }
	
    public void setTexCoordsUv(RectF uvCoords) {
		setTexCoordsUv(uvCoords, false);
	}
	
	public void setTexCoordsUv(RectF uvCoords, boolean rotated) {
		texCoordsUv.set(uvCoords);
		rotateTexCoords = rotated;
		dirty = true;
	}
	
	public void setTexCoordsUv(float ulU, float ulV, float lrU, float lrV, boolean rotated) {
		texCoordsUv.set(ulU, ulV, lrU, lrV);
		rotateTexCoords = rotated;
		dirty = true;
	}

	public void setTexCoordsPx(Rect texCoordsPx) {
		this.texCoordsPx.set(texCoordsPx);
	}
	
	public void setTexCoordsPx(int left, int top, int right, int bottom){
		this.texCoordsPx.set(left, top, right, bottom);
	}

	public Rect getTexCoordsPx() {
		return texCoordsPx;
	}

	public RectF getTexCoordsUv() {
		return texCoordsUv;
	}
	
	public void setTexCoords(TextureSegment ts){
		this.texCoordsPx.set(ts.getPixelCoords());
		this.texCoordsUv.set(ts.getUvCoords());
		this.rotateTexCoords = ts.isVertical();
		dirty = true;
	}
	
	@Override
	public int render(RenderContext rc, ShortBuffer frameIndexBuffer){
		if (!visible) return 0;
		
		if (dirty){
            TexturedQuad.position(vertexBuffer, 0, getPosition(rc.frameNanoTime));
			
   			float nowAlpha = alpha.get(rc.frameNanoTime);
   			TexturedQuad.setAlpha(vertexBuffer, 0, nowAlpha, nowAlpha, nowAlpha, nowAlpha);
   			dirty = !alpha.isDone(rc.frameNanoTime);
    		
    		if (alpha.getLast() < 0.05f) return 0;

			if (rotateTexCoords)
				TexturedQuad.setUVMappingRotated(vertexBuffer, 0, texCoordsUv);
			else
				TexturedQuad.setUVMapping(vertexBuffer, 0, texCoordsUv);

			vertexBuffer.position(0);
			GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 
					firstByteIndex, 
					vertexBuffer.capacity()*4, 
					vertexBuffer);

			dirty |= !posTransition.isDone(rc.frameNanoTime);
		}
		
		frameIndexBuffer.put(indexBuffer);
		return TexturedQuad.INDICES_COUNT;
	}
	
	public boolean contains(float x, float y) {
		return targetPosition.containsXY(x, y);
	}
	
	public boolean containsY(float y) {
		return targetPosition.containsY(y);
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

	@Override
	public int getMaxVertexCount() {
		return TexturedQuad.VERTEX_COUNT;
	}

	@Override
	public int getMaxIndexCount() {
		return TexturedQuad.INDICES_COUNT;
	}

	protected static short[] createIndexBuffer() {
		return TexturedQuad.allocateQuadIndexArray(1);
	}
}
