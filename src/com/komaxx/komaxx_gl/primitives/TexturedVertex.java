package com.komaxx.komaxx_gl.primitives;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.komaxx.komaxx_gl.util.RenderUtil;

public class TexturedVertex {
	public static final int STRIDE_FLOATS = 7;		// position:3, textCoords:2, alpha:1, textureIndex:1
    public static final int STRIDE_BYTES = STRIDE_FLOATS * RenderUtil.FLOAT_SIZE_BYTES;
    public static final int UV_OFFSET = 3;
    public static final int ALPHA_OFFSET = 5;
	
	
	private float[] position = new float[]{ 0,0,0,1 };
	private float[] uvCoords = new float[]{ 0,0 };
	private float alpha = 1;
	
	private int[] uvCoordsPx = new int[]{ 0,0 };
	
	public void setUvCoords(float u, float v) {
		uvCoords[0] = u;
		uvCoords[1] = v;
	}

	public int[] getUvCoordsPx() {
		return uvCoordsPx;
	}
	
	public void setUvCoordsPx(int u, int v) {
		this.uvCoordsPx[0] = u;
		this.uvCoordsPx[1] = v;
	}
	
	public void setPositionXY(float x, float y) {
		position[0] = x;
		position[1] = y;
	}
	
	public void setPositionXY(float[] v) {
		position[0] = v[0];
		position[1] = v[1];
	}
	
	/**
	 * Delivers the internal data structure. Do only modify if you know
	 * what you're doing.
	 */
	public float[] getPosition() {
		return position;
	}

	/**
	 * Multiplies the coords with the given factors
	 */
	public void scaleCoords(float xScale, float yScale) {
		position[0] *= xScale;
		position[1] *= yScale;
	}
	
	public void setPosition(float x, float y, float z) {
		position[0] = x;
		position[1] = y;
		position[2] = z;
	}

	public void setAlpha(float nuAlpha) {
		this.alpha = nuAlpha;
	}

	public void writePosition(FloatBuffer vertexBuffer, int vertexOffset) {
		vertexBuffer.position(vertexOffset);
		vertexBuffer.put(position, 0, 3);
	}
	
	public void writeUvCoords(FloatBuffer vertexBuffer, int offset) {
		vertexBuffer.position(offset + UV_OFFSET);
		vertexBuffer.put(uvCoords);
	}

	public void writeAlpha(FloatBuffer vertexBuffer, int offset) {
		vertexBuffer.position(offset + ALPHA_OFFSET);
		vertexBuffer.put(alpha);
	}

	public static ShortBuffer allocateIndices(int vertexCount) {
		ShortBuffer ret = ByteBuffer.allocateDirect(
				vertexCount * RenderUtil.SHORT_SIZE_BYTES
                ).order(ByteOrder.nativeOrder()).asShortBuffer();
		
		return ret;
	}
    
    public static FloatBuffer allocate(int count){
		return ByteBuffer.allocateDirect(
				count * STRIDE_BYTES
                ).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }
    
    @Override
    public String toString() {
    	return "["+ 
    			RenderUtil.printFloat(position[0], 2) + ","+
    			RenderUtil.printFloat(position[1], 2) + "," +
    			RenderUtil.printFloat(position[2], 2) + 
    			"]";
    }
}
