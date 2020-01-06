package com.komaxx.komaxx_gl.bound_meshes;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.math.GlCube;
import com.komaxx.komaxx_gl.math.GlRect;
import com.komaxx.komaxx_gl.primitives.TexturedQuad;
import com.komaxx.komaxx_gl.texturing.TextureSegment;
import com.komaxx.komaxx_gl.util.InterpolatedValue;
import com.komaxx.komaxx_gl.util.InterpolatedValue.AnimationType;

public class BoundTexturedQuad extends ABoundMesh {
    protected final GlCube position = new GlCube();
	protected boolean positionDirty = true;

	protected final RectF texCoordsUv = new RectF();
	protected boolean rotateTexCoords = false;

	protected boolean texCoordsDirty = true;
	protected final InterpolatedValue alpha = new InterpolatedValue(AnimationType.INVERSE_SQUARED, 1f, InterpolatedValue.ANIMATION_DURATION_SLOW);
    
	protected boolean alphaDirty = true;

	protected final FloatBuffer vertexBuffer = TexturedQuad.allocateQuads(1);
	protected final Rect texCoordsPx = new Rect();


	public BoundTexturedQuad(){
		indexBuffer = createIndexBuffer();
	}
	
    public void setTexCoordsUv(RectF uvCoords) {
		setTexCoordsUv(uvCoords, false);
	}
	
	public void setTexCoordsUv(RectF uvCoords, boolean rotated) {
		texCoordsUv.set(uvCoords);
		rotateTexCoords = rotated;
		texCoordsDirty = true;
	}
	
	public void setTexCoordsUv(float ulU, float ulV, float lrU, float lrV, boolean rotated) {
		texCoordsUv.set(ulU, ulV, lrU, lrV);
		rotateTexCoords = rotated;
		texCoordsDirty = true;
	}
	
	public void positionXY(float left, float top, float right, float bottom) {
		position.setXY(left, top, right, bottom);
		positionDirty = true;
	}

	public void positionY(float top, float bottom) {
		position.setY(top, bottom);
		positionDirty = true;
	}
	
	public void positionX(float left, float right) {
		position.setX(left, right);
		positionDirty = true;
	}


	public void positionXY(GlRect nuPosition) {
		position.setXY(nuPosition);
		positionDirty = true;
	}

	public void position(GlCube nuPos) {
		position.set(nuPos);
        positionDirty = true;
	}
	
	public void position(float left, float top, float front, float right, float bottom, float back) {
		position.set(left, top, front, right, bottom, back);
        positionDirty = true;
    }
    
    public void positionZ(float front, float back) {
        position.setZ(front, back);
        positionDirty = true;
    }
    
	public void setAlpha(float nuAlpha){
		alpha.set(nuAlpha);
		alphaDirty = true;
	}
	
	@Override
	public int render(RenderContext rc, ShortBuffer frameIndexBuffer){
		if (!visible) return 0;
		
		boolean vboDirty = false;
		
		if (alphaDirty){
			float nowAlpha = alpha.get(rc.frameNanoTime);
			TexturedQuad.setAlpha(vertexBuffer, 0, nowAlpha, nowAlpha, nowAlpha, nowAlpha);
			alphaDirty = !alpha.isDone(rc.frameNanoTime);
			vboDirty = true;
		}
		
		if (alpha.getLast() < 0.05f) return 0;
		
		if (positionDirty){
			TexturedQuad.position(vertexBuffer, 0, position);
			positionDirty = false;
			vboDirty = true;
		}
		
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
		this.texCoordsDirty = true;
	}

	public boolean contains(float x, float y) {
		return position.containsXY(x, y);
	}
	
	public boolean containsY(float y) {
		return position.containsY(y);
	}

    public GlCube getPosition() {
        return this.position;
    }

	@Override
	public int getMaxVertexCount() {
		return TexturedQuad.VERTEX_COUNT;
	}

	@Override
	public int getMaxIndexCount() {
		return TexturedQuad.INDICES_COUNT;
	}

	public void setAlphaDirect(float nuAlpha) {
		alpha.setDirect(nuAlpha);
		alphaDirty = true;
	}

    public float getAlpha() {
        return this.alpha.getLast();
    }

    public float getAlpha(long time) {
        return this.alpha.get(time);
    }
    
	protected static short[] createIndexBuffer() {
		return TexturedQuad.allocateQuadIndexArray(1);
	}

	public float getTargetAlpha() {
		return alpha.getTarget();
	}

	public void setAlphaAnimationDuration(long nuDuration) {
		alpha.setDuration(nuDuration);
	}
}
