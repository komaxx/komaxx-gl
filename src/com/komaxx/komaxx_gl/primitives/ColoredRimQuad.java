package com.komaxx.komaxx_gl.primitives;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.util.RenderUtil;

/**
 * Static methods to define and manipulate a mesh used for rendering of
 * event backgrounds.
 * 
 * It looks like this: <br>
 * <pre>
 *    ____________________   
 *  / |                  | \ 
 *  ------------------------ 
 * |  |                  |  |
 * |  |                  |  |
 *  ------------------------ 
 *  \ |                  | / 
 *    ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯   
 * </pre>
 * 
 * @author Matthias Schicker
 */
public class ColoredRimQuad {
	public static final int TRIANGLE_COUNT = 4 + 6 + 4;
	public static final int VERTEX_COUNT = 2 + 4 + 4 + 2;
	public static final int INDICES_COUNT = TRIANGLE_COUNT * 3;
	
	// vertex numeration is line-based:
	// 		0	1
	//  2	3	4	5
	//	6	7	8	9
	//		10	11
	
	public static final int PATCH_FLOATS = VERTEX_COUNT * Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	public static final int PATCH_BYTES = VERTEX_COUNT * Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES;
	
	private static final float[] tmpX = new float[4];
	private static final float[] tmpY = new float[4];
	
	
	private static short[] basicIndices = new short[]{
		0,2,3,  0,3,1,  1,3,4,   1,4,5,
		2,6,3,  3,6,7,  3,7,4,   4,7,8,  4,8,5,  5,8,9,
		6,10,7, 7,10,8, 8,10,11, 8,11,9
	};
	private static short[] tmpIndices = new short[basicIndices.length];

	public static ShortBuffer allocatePatchIndices(int count) {
		ShortBuffer ret = ByteBuffer.allocateDirect(
				INDICES_COUNT * count * RenderUtil.SHORT_SIZE_BYTES
                ).order(ByteOrder.nativeOrder()).asShortBuffer();
		
		ret.position(0);
		int offset = 0;
		for (int i = 0; i < count; i++){
			ret.put(getOffsetedIndices(tmpIndices, offset));
			offset += VERTEX_COUNT;
		}
		
		return ret;
	}

	public static FloatBuffer allocateMeshes(int count) {
		return ByteBuffer.allocateDirect(
				count * VERTEX_COUNT * Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES
                ).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	
	public static void putOffsetedIndices(ShortBuffer indexBuffer, int indexOffset) {
		indexBuffer.put(getOffsetedIndices(tmpIndices, indexOffset));
	}
	
	public static short[] getOffsetedIndices(short[] ret, int offset) {
		for (int i = 0; i < INDICES_COUNT; i++){
			ret[i] = (short) (offset + basicIndices[i]);
		}
		return ret;
	}

	public static final void position(FloatBuffer data, int offset, 
			float ulX, float ulY, 
			float lrX, float lrY, 
			float z, float rimWidth		
	){
		
		tmpX[0] = ulX;
		tmpX[1] = ulX + rimWidth;
		tmpX[2] = lrX - rimWidth; 
		tmpX[3] = lrX;
		
		tmpY[0] = ulY;
		tmpY[1] = ulY - rimWidth;
		tmpY[2] = lrY + rimWidth;
		tmpY[3] = lrY;
		
		// top row (2 vertices)
		data.position(offset);
		data.put(tmpX[1]);
		data.put(ulY);
		data.put(z);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		
		data.position(offset);
		data.put(tmpX[2]);
		data.put(ulY);
		data.put(z);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		
		// two middle rows
		for (int yIndex = 1; yIndex < 3; yIndex++){
			float y = tmpY[yIndex];
			for (int xIndex = 0; xIndex < 4; xIndex++){
				data.position(offset);
				data.put(tmpX[xIndex]);
				data.put(y);
				data.put(z);
				offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
			}
		}
		
		// bottom row (2 vertices)
		data.position(offset);
		data.put(tmpX[1]);
		data.put(lrY);
		data.put(z);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		
		data.position(offset);
		data.put(tmpX[2]);
		data.put(lrY);
		data.put(z);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	}

	/**
	 * expects a float[8] array containing first the rim, then the content color
	 */
	public static void setColor(FloatBuffer data, int offset, float[] rimContentColors, float topBottomAlphaFactor) {
		offset += Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET;
		
		// top row
		data.position(offset); data.put(rimContentColors, 0, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimContentColors, 0, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		
		// top middle row
		data.position(offset); data.put(rimContentColors, 0, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimContentColors, 4, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimContentColors, 4, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimContentColors, 0, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		
		for (int i = 0; i < 8; i++) rimContentColors[i] *= topBottomAlphaFactor;
		// bottom middle row
		data.position(offset); data.put(rimContentColors, 0, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimContentColors, 4, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimContentColors, 4, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimContentColors, 0, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		
		// bottom row
		data.position(offset); data.put(rimContentColors, 0, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimContentColors, 0, 4);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	}
	
	public static void setColor(FloatBuffer data, int offset, 
			float rimR, float rimG, float rimB, float rimA,
			float contentR, float contentG, float contentB, float contentA) {
		
		offset += Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET;
		
		// top row
		data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);data.put(rimA);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);data.put(rimA);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		
		// middle rows
		for (int i = 0; i < 2; i++){
			data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);data.put(rimA);
			offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
			data.position(offset); data.put(contentR);data.put(contentG);data.put(contentB);data.put(contentA);
			offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
			data.position(offset); data.put(contentR);data.put(contentG);data.put(contentB);data.put(contentA);
			offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
			data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);data.put(rimA);
			offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		}
		
		// bottom row
		data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);data.put(rimA);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);data.put(rimA);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	}

	public static void setColor(FloatBuffer data, int offset, 
			float rimR, float rimG, float rimB, 
			float contentR, float contentG, float contentB) {
		
		offset += Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET;
		
		// top row
		data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		
		// middle rows
		for (int i = 0; i < 2; i++){
			data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);
			offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
			data.position(offset); data.put(contentR);data.put(contentG);data.put(contentB);
			offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
			data.position(offset); data.put(contentR);data.put(contentG);data.put(contentB);
			offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
			data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);
			offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		}
		
		// bottom row
		data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset); data.put(rimR);data.put(rimG);data.put(rimB);
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	}
	
	public static final void setAlpha(FloatBuffer data, int offset, float alpha){
		offset += Vertex.COLOR_VERTEX_DATA_ALPHA_OFFSET;
		for (int i = 0; i < VERTEX_COUNT; i++){
			data.position(offset);
			data.put(alpha);
			offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		}
	}
	
	public static final void setAlpha(FloatBuffer data, int offset, float topAlpha, float bottomAlpha){
		offset += Vertex.COLOR_VERTEX_DATA_ALPHA_OFFSET;
		// upper two rows
		for (int i = 0; i < 6; i++){
			data.position(offset);
			data.put(topAlpha);
			offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		}
		// lower two row
		for (int i = 0; i < 6; i++){
			data.position(offset);
			data.put(bottomAlpha);
			offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		}
	}
	
	/**
	 * Sets the attribute pointers for the current renderProgram (which should
	 * be one of the texturing render programs!!) and paints it. The appropriate
	 * texture should already be bound previously.
	 */
	public static boolean render(RenderProgram rp,  
			int firstPatch, int patchCount, FloatBuffer patchData, ShortBuffer patchIndices) {
		
		// set the patch float buffer to be used as position source
		patchData.position(0);
		GLES20.glVertexAttribPointer(
				rp.vertexXyzHandle, 3, GLES20.GL_FLOAT, false, Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, patchData);
		GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);

		// set the patch float buffer to be used as colorSource
		patchData.position(Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET);
		GLES20.glVertexAttribPointer(
				rp.vertexColorHandle, 4, GLES20.GL_FLOAT, false,
				Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, patchData);
		GLES20.glEnableVertexAttribArray(rp.vertexColorHandle);
		
		patchIndices.position(firstPatch * INDICES_COUNT);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, patchCount * INDICES_COUNT,
				GLES20.GL_UNSIGNED_SHORT, patchIndices);
		
		return true;
	}
}
