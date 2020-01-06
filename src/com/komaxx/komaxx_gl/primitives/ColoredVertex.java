package com.komaxx.komaxx_gl.primitives;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.komaxx.komaxx_gl.util.RenderUtil;

public class ColoredVertex {
	public static final int STRIDE_FLOATS = 7;		// position:3, color:4
    public static final int STRIDE_BYTES = STRIDE_FLOATS * RenderUtil.FLOAT_SIZE_BYTES;
    public static final int COLOR_OFFSET = 3;
	
	private float[] position = new float[]{ 0,0,0,1 };
	private float[] color = new float[]{ 0,1,0, 1 };
	
	private static float[] tmpColorArray = new float[4];
	
	public void setColorRGBA(float[] rgba) {
		System.arraycopy(rgba, 0, color, 0, 4);
	}

	public void setColorRGB(float[] rgb) {
		System.arraycopy(rgb, 0, color, 0, 3);
	}

	/**
	 * Warning: Color conversion can be costly! Think about storing a float[4]
	 * array with the color (see RenderUtil.color2floatsRGBA( ) for convenience).
	 */
	public void setColorRGBA(int color) {
		setColorRGBA(RenderUtil.color2floatsRGBA(tmpColorArray, color));
	}
	
	
	public float[] getColor() {
		return color;
	}
	
	public void setPosition(float x, float y, float z) {
		position[0] = x;
		position[1] = y;
		position[2] = z;
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

	public void setAlpha(float nuAlpha) {
		this.color[3] = nuAlpha;
	}

	public void writePosition(FloatBuffer vertexBuffer, int vertexOffset) {
		vertexBuffer.position(vertexOffset);
		vertexBuffer.put(position, 0, 3);
	}
	
	public void writeColor(FloatBuffer vertexBuffer, int offset) {
		vertexBuffer.position(offset + COLOR_OFFSET);
		vertexBuffer.put(color);
	}

	public static ShortBuffer allocateIndices(int indexCount) {
		ShortBuffer ret = ByteBuffer.allocateDirect(
				indexCount * RenderUtil.SHORT_SIZE_BYTES
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
