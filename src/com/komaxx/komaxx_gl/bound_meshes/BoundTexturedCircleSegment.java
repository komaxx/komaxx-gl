package com.komaxx.komaxx_gl.bound_meshes;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.PointF;
import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.primitives.TexturedCircleSegment;

public class BoundTexturedCircleSegment extends ABoundMesh {
	private final int segments;
	
    protected final float[] centerPosition = new float[3];
    protected float startAngle = 0;
    protected float endAngle = 90;
    protected float radius = 100;
    protected boolean positionDirty = true;

	protected final PointF hubTexCoordsUv = new PointF();
	protected final PointF rimTexCoordsUv = new PointF();
	protected boolean texCoordsDirty = true;

	protected float alpha = 1;
	protected boolean alphaDirty = true;
	
	protected final FloatBuffer vertexBuffer;


	public BoundTexturedCircleSegment(int segments){
		this.segments = segments;
		vertexBuffer = TexturedCircleSegment.allocate(segments);
		indexBuffer = createIndexBuffer();
	}
	
    public void setTexCoordsUv(PointF hub, PointF rim) {
    	hubTexCoordsUv.set(hub);
    	rimTexCoordsUv.set(rim);
    	texCoordsDirty = true;
	}
	
    public void setAngles(float startAngle, float endAngle){
    	this.startAngle = startAngle;
    	this.endAngle = endAngle;
    	positionDirty = true;
    }
    
	public void positionXY(float centerX, float centerY) {
		centerPosition[0] = centerX;
		centerPosition[1] = centerY;
		positionDirty = true;
	}

	public void setRadius(float radius){
		this.radius = radius;
		positionDirty = true;
	}
	
	public void position(float centerX, float centerY, float z) {
		centerPosition[0] = centerX;
		centerPosition[1] = centerY;
		centerPosition[2] = z;
		positionDirty = true;
    }
    
    public void positionZ(float z) {
		centerPosition[2] = z;
        positionDirty = true;
    }
	
	@Override
	public int render(RenderContext rc, ShortBuffer frameIndexBuffer){
		if (!visible) return 0;
		
		boolean vboDirty = false;
		
		if (positionDirty){
			TexturedCircleSegment.position(vertexBuffer, 0, 
					centerPosition[0], centerPosition[1], centerPosition[2],
					radius, startAngle, endAngle, segments);
			positionDirty = false;
			vboDirty = true;
		}
		
		if (alphaDirty){
			TexturedCircleSegment.setAlpha(vertexBuffer, 0, alpha, alpha, segments);
			alphaDirty = false;
			vboDirty = true;
		}
		
		if (texCoordsDirty){
			TexturedCircleSegment.setUVMapping(vertexBuffer, 0, 
					hubTexCoordsUv.x, hubTexCoordsUv.y,
					rimTexCoordsUv.x, rimTexCoordsUv.y, segments);
			
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
		return TexturedCircleSegment.indicesCount(segments);
	}

	@Override
	public int getMaxVertexCount() {
		return TexturedCircleSegment.vertexCount(segments);
	}

	@Override
	public int getMaxIndexCount() {
		return TexturedCircleSegment.indicesCount(segments);
	}
    
	protected short[] createIndexBuffer() {
		return TexturedCircleSegment.allocateIndexArray(segments);
	}

	public void setAlpha(float a) {
		alpha = a;
		alphaDirty = true;
	}
}
