package com.komaxx.komaxx_gl.primitives;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.math.GlRect;
import com.komaxx.komaxx_gl.util.RenderUtil;

public class ColorQuad {
	public static final int TRIANGLE_COUNT = 2;
	public static final int VERTEX_COUNT = 4;
	public static final int INDICES_COUNT = 6;
	
	public static final int COLOR_QUAD_FLOATS = VERTEX_COUNT * Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	public static final int COLOR_QUAD_BYTES = VERTEX_COUNT * Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES;

	private static final int COLOR_UPPER_LEFT_XYZ_OFFSET = 0;
	private static final int COLOR_LOWER_LEFT_XYZ_OFFSET = Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	private static final int COLOR_LOWER_RIGHT_XYZ_OFFSET = 2 * Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	private static final int COLOR_UPPER_RIGHT_XYZ_OFFSET = 3 * Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;

	private static final int COLOR_UPPER_LEFT_COLOR_OFFSET = Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET;
	private static final int COLOR_LOWER_LEFT_COLOR_OFFSET = Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET + Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	private static final int COLOR_LOWER_RIGHT_COLOR_OFFSET = Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET + 2 * Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	private static final int COLOR_UPPER_RIGHT_COLOR_OFFSET = Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET + 3 * Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	
	/**
	 * Identifies the upper left vertex in calls that accept a vertex identifier as parameter 
	 */
	public static final int UPPER_LEFT_VERTEX = 0;
	/**
	 * Identifies the upper right vertex in calls that accept a vertex identifier as parameter 
	 */
	public static final int UPPER_RIGHT_VERTEX = 1;
	/**
	 * Identifies the upper left vertex in calls that accept a vertex identifier as parameter 
	 */
	public static final int LOWER_LEFT_VERTEX = 2;
	/**
	 * Identifies the upper right vertex in calls that accept a vertex identifier as parameter 
	 */
	public static final int LOWER_RIGHT_VERTEX = 3;
	

	public static ShortBuffer allocateQuadIndices(int quadsCount) {
		ShortBuffer ret = ByteBuffer.allocateDirect(
				6 * quadsCount * RenderUtil.SHORT_SIZE_BYTES
                ).order(ByteOrder.nativeOrder()).asShortBuffer();
		
		ret.position(0);
		int offset = 0;
		for (int i = 0; i < quadsCount; i++){
			ret.put((short) (offset + 0));
			ret.put((short) (offset + 1));
			ret.put((short) (offset + 3));
			ret.put((short) (offset + 3));
			ret.put((short) (offset + 1));
			ret.put((short) (offset + 2));
			offset += 4;
		}
		
		return ret;
	}


	public static void putIndices(ShortBuffer quadsIndices, int offset) {
		quadsIndices.put((short) (offset + 0));
		quadsIndices.put((short) (offset + 1));
		quadsIndices.put((short) (offset + 3));
		quadsIndices.put((short) (offset + 3));
		quadsIndices.put((short) (offset + 1));
		quadsIndices.put((short) (offset + 2));
	}

	
	public static FloatBuffer allocateColorQuads(int quadsCount) {
		return ByteBuffer.allocateDirect(
				quadsCount * VERTEX_COUNT * Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES
                ).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	
	/**
	 * Creates a new short array with a length that can hold indices necessary for 'count' quads.
	 */
	public static short[] createIndexArray(int count) {
		return getOffsetedIndices(new short[INDICES_COUNT * count], (short)0);
	}
	
	public static short[] getOffsetedIndices(short[] ret, short vertexOffset) {
		ret[0] = vertexOffset;
		ret[1] = ret[4] = (short)(vertexOffset + 1);
		ret[2] = ret[3] = (short)(vertexOffset + 3);
		ret[5] = (short)(vertexOffset + 2);
		return ret;
	}
	
	public static final void position(FloatBuffer data, int offset, 
			float ulX, float ulY, float ulZ, float lrX, float lrY, float lrZ){
		
		// upper left vertex
		data.position(offset + COLOR_UPPER_LEFT_XYZ_OFFSET);
		data.put(ulX);
		data.put(ulY);
		data.put(ulZ);
		
		// lower left vertex
		data.position(offset + COLOR_LOWER_LEFT_XYZ_OFFSET);
		data.put(ulX);
		data.put(lrY);
		data.put(ulZ);
		
		// lower right vertex
		data.position(offset + COLOR_LOWER_RIGHT_XYZ_OFFSET);
		data.put(lrX);
		data.put(lrY);
		data.put(ulZ);

		// upper right vertex
		data.position(offset + COLOR_UPPER_RIGHT_XYZ_OFFSET);
		data.put(lrX);
		data.put(ulY);
		data.put(lrZ);
	}
	
	public static final void positionXY(FloatBuffer data, int offset, 
			float ulX, float ulY, float lrX, float lrY){
		
		// upper left vertex
		data.position(offset + COLOR_UPPER_LEFT_XYZ_OFFSET);
		data.put(ulX);
		data.put(ulY);
		
		// lower left vertex
		data.position(offset + COLOR_LOWER_LEFT_XYZ_OFFSET);
		data.put(ulX);
		data.put(lrY);
		
		// lower right vertex
		data.position(offset + COLOR_LOWER_RIGHT_XYZ_OFFSET);
		data.put(lrX);
		data.put(lrY);

		// upper right vertex
		data.position(offset + COLOR_UPPER_RIGHT_XYZ_OFFSET);
		data.put(lrX);
		data.put(ulY);
	}
	
	public static void positionXY(FloatBuffer data, int offset, GlRect b) {
		// upper left vertex
		data.position(offset + COLOR_UPPER_LEFT_XYZ_OFFSET);
		data.put(b.left);
		data.put(b.top);
		
		// lower left vertex
		data.position(offset + COLOR_LOWER_LEFT_XYZ_OFFSET);
		data.put(b.left);
		data.put(b.bottom);
		
		// lower right vertex
		data.position(offset + COLOR_LOWER_RIGHT_XYZ_OFFSET);
		data.put(b.right);
		data.put(b.bottom);

		// upper right vertex
		data.position(offset + COLOR_UPPER_RIGHT_XYZ_OFFSET);
		data.put(b.right);
		data.put(b.top);
	}

	/**
	 * Colors single vertices.
	 */
	public static void color(FloatBuffer data, int offset, int vertex, float[] colorRGBA) {
		switch (vertex){
		case UPPER_LEFT_VERTEX:
			offset += COLOR_UPPER_LEFT_COLOR_OFFSET;
			break;
		case UPPER_RIGHT_VERTEX:
			offset += COLOR_UPPER_RIGHT_COLOR_OFFSET;
			break;
		case LOWER_LEFT_VERTEX:
			offset += COLOR_LOWER_LEFT_COLOR_OFFSET;
			break;
		case LOWER_RIGHT_VERTEX:
			offset += COLOR_LOWER_RIGHT_COLOR_OFFSET;
			break;
		}
		data.position(offset);
		data.put(colorRGBA);
	}

	/**
	 * Change the color of all vertices.
	 * @param backColorRGBA		Colors in a float[]. <b>MUST</b> be of length >= 4
	 */
	public static void color(FloatBuffer colorQuads, int offset, float[] backColorRGBA) {
		color(colorQuads, offset, backColorRGBA[0], backColorRGBA[1], backColorRGBA[2], backColorRGBA[3]);
	}

	/**
	 * only changes the alpha of all vertices.
	 */
	public static void alpha(FloatBuffer data, int offset, float alpha) {
		offset += Vertex.COLOR_VERTEX_DATA_ALPHA_OFFSET;
		data.position(offset);
		data.put(alpha);
		
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset);
		data.put(alpha);

		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset);
		data.put(alpha);

		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset);
		data.put(alpha);
	}
	
	/**
	 * Assigns to all vertices the same color
	 */
	public static final void color(FloatBuffer data, int offset, float r, float g, float b, float a){
		// upper left vertex
		data.position(offset + COLOR_UPPER_LEFT_COLOR_OFFSET);
		data.put(r);
		data.put(g);
		data.put(b);
		data.put(a);
		
		// lower left vertex
		data.position(offset + COLOR_LOWER_LEFT_COLOR_OFFSET);
		data.put(r);
		data.put(g);
		data.put(b);
		data.put(a);
		
		// lower right vertex
		data.position(offset + COLOR_LOWER_RIGHT_COLOR_OFFSET);
		data.put(r);
		data.put(g);
		data.put(b);
		data.put(a);

		// upper right vertex
		data.position(offset + COLOR_UPPER_RIGHT_COLOR_OFFSET);
		data.put(r);
		data.put(g);
		data.put(b);
		data.put(a);
	}
	
	/**
	 * Sets the attribute pointers for the current renderProgram (which should
	 * be one of the color render programs!). The appropriate color tint (if used)
	 * should already be bound previously as uniform.
	 */
	public static boolean renderColored(RenderProgram rp,  
			int firstQuad, int quadsCount, FloatBuffer quadsData, ShortBuffer quadsIndices) {
		
		quadsData.position(0);
        GLES20.glVertexAttribPointer(
        		rp.vertexXyzHandle, 
        		3, GLES20.GL_FLOAT, false, Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, quadsData);
        GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);
        
        // set the quad float buffer to be used as colorSource
        if (rp.vertexColorHandle != -1){
	        quadsData.position(Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET);
	        GLES20.glVertexAttribPointer(
	        		rp.vertexColorHandle, 4, GLES20.GL_FLOAT, false,
	        		Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, quadsData);
	        GLES20.glEnableVertexAttribArray(rp.vertexColorHandle);
        }

        quadsIndices.position(firstQuad * INDICES_COUNT);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, quadsCount * INDICES_COUNT, GLES20.GL_UNSIGNED_SHORT, quadsIndices);
		
		return true;
	}


	public static void positionY(FloatBuffer data, int offset, float top, float bottom) {
		data.put(offset + COLOR_UPPER_LEFT_XYZ_OFFSET + 1, top);
		data.put(offset + COLOR_UPPER_RIGHT_XYZ_OFFSET + 1, top);
		data.put(offset + COLOR_LOWER_LEFT_XYZ_OFFSET + 1, bottom);
		data.put(offset + COLOR_LOWER_RIGHT_XYZ_OFFSET + 1, bottom);
	}
}
