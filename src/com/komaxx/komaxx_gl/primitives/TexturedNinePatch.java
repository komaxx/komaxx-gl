package com.komaxx.komaxx_gl.primitives;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.RectF;
import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.math.GlRect;
import com.komaxx.komaxx_gl.util.RenderUtil;

/**
 * Static methods to define and manipulate a nine-patch, a rectangular field
 * with 9 sizable patched, with a position, texture-coords and an alpha value for each vertex.
 *  
 * @author Matthias Schicker
 */
public class TexturedNinePatch {
	public static final int VERTEX_COUNT = 4*4;
	
	// vertex numeration is line-based:
	// 	0	1	2	3
	//	4	5	6	7
	//	8	9	10	11
	//	12	13	14	15
	
	public static final int NINEPATCH_FLOATS = VERTEX_COUNT * Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	public static final int NINEPATCH_BYTES = VERTEX_COUNT * Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES;
	
	private static final float[] tmpX = new float[4];
	private static final float[] tmpY = new float[4];
	
	private static final float[] tmpU = new float[4];
	private static final float[] tmpV = new float[4];
	
	private static short[] basicIndices = new short[]{
		0,4,1, 1,4,5,  1,5,2, 2,5,6,  2,6,3,  3,6,7,
			4,8,5, 5,8,9,  
//			5,9,6, 6,9,10,		// comment out to remove middle!
			6,10,7, 7,10,11,
		8,12,9, 9,12,13,  9,13,10, 10,13,14,  10,14,11, 11,14,15  
	};
	public static final int INDICES_COUNT = basicIndices.length;
	private static short[] tmpIndices = new short[basicIndices.length];

	
	public static ShortBuffer allocateNinePatchIndices(int count) {
		ShortBuffer ret = ByteBuffer.allocateDirect(
				INDICES_COUNT * count * RenderUtil.SHORT_SIZE_BYTES
                ).order(ByteOrder.nativeOrder()).asShortBuffer();
		
		ret.position(0);
		int offset = 0;
		for (int i = 0; i < count; i++){
			ret.put(moveBaseIndices(offset));
			offset += VERTEX_COUNT;
		}
		
		return ret;
	}


	public static void getOffsetedIndices(short[] ret, short firstIndex) {
		for (int i = 0; i < INDICES_COUNT; i++){
			ret[i] = (short) (firstIndex + basicIndices[i]);
		}
	}

	public static short[] createIndexArray(int count) {
		short[] ret = new short[INDICES_COUNT * count];
		short offset = 0;
		int i = 0;
		for (int c = 0; c < count; c++){
			for (int j = 0; j < INDICES_COUNT; j++){
				ret[i] = (short) (offset + basicIndices[j]);
				i++;
			}
			offset += VERTEX_COUNT;
		}
		
		return ret;
	}


	public static void putOffsetedIndices(ShortBuffer indexData, int offset) {
		indexData.put(moveBaseIndices(offset));
	}
	
	private static short[] moveBaseIndices(int offset) {
		for (int i = 0; i < INDICES_COUNT; i++){
			tmpIndices[i] = (short) (offset + basicIndices[i]);
		}
		return tmpIndices;
	}

	public static FloatBuffer allocateNinePatches(int count) {
		return ByteBuffer.allocateDirect(
				count * VERTEX_COUNT * Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES
                ).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}


	public static void positionXY(FloatBuffer data, int offset, GlRect position, float patchWidth) {
		tmpX[0] = position.left;
		tmpX[1] = position.left + patchWidth; 
		tmpX[2] = position.right - patchWidth; 
		tmpX[3] = position.right;
		
		tmpY[0] = position.top; 
		tmpY[1] = position.top - patchWidth; 
		tmpY[2] = position.bottom + patchWidth; 
		tmpY[3] = position.bottom;
		
		for (int y = 0; y < 4; y++){
			for (int x = 0; x < 4; x++){
				data.position(offset);
				data.put(tmpX[x]);
				data.put(tmpY[y]);
				offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
			}
		}
	}

	public static void positionXY(FloatBuffer data, int offset, GlRect position, float z, float patchWidth) {
		tmpX[0] = position.left;
		tmpX[1] = position.left + patchWidth; 
		tmpX[2] = position.right - patchWidth; 
		tmpX[3] = position.right;
		
		tmpY[0] = position.top; 
		tmpY[1] = position.top - patchWidth; 
		tmpY[2] = position.bottom + patchWidth; 
		tmpY[3] = position.bottom;
		
		for (int y = 0; y < 4; y++){
			for (int x = 0; x < 4; x++){
				data.position(offset);
				data.put(tmpX[x]);
				data.put(tmpY[y]);
				data.put(z);
				offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
			}
		}
	}
	
	public static final void position(FloatBuffer data, int offset, 
			float ulX, float ulY, 
			float lrX, float lrY, float z,
			float leftPatchWidth, float topPatchHeight, float rightPatchWidth, float bottomPatchHeight		
	){
		tmpX[0] = ulX;
		tmpX[1] = ulX + leftPatchWidth; 
		tmpX[2] = lrX - rightPatchWidth; 
		tmpX[3] = lrX;
		
		tmpY[0] = ulY; 
		tmpY[1] = ulY - topPatchHeight; 
		tmpY[2] = lrY + bottomPatchHeight; 
		tmpY[3] = lrY;
		
		for (int y = 0; y < 4; y++){
			for (int x = 0; x < 4; x++){
				data.position(offset);
				data.put(tmpX[x]);
				data.put(tmpY[y]);
				data.put(z);
				offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
			}
		}
	}
	
	public static final void positionXY(FloatBuffer data, int offset, 
			float ulX, float ulY, 
			float lrX, float lrY, 
			float leftPatchWidth, float topPatchHeight, float rightPatchWidth, float bottomPatchHeight		
	){
		tmpX[0] = ulX;
		tmpX[1] = ulX + leftPatchWidth; 
		tmpX[2] = lrX - rightPatchWidth; 
		tmpX[3] = lrX;
		
		tmpY[0] = ulY; 
		tmpY[1] = ulY - topPatchHeight; 
		tmpY[2] = lrY + bottomPatchHeight; 
		tmpY[3] = lrY;
		
		for (int y = 0; y < 4; y++){
			for (int x = 0; x < 4; x++){
				data.position(offset);
				data.put(tmpX[x]);
				data.put(tmpY[y]);
				offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
			}
		}
	}
	
	public static void setPulsatingIntensity(FloatBuffer data, int offset, float intensity) {
		offset += Vertex.TEXTURED_VERTEX_DATA_PULSE_INTENSITY;
		for (int i = 0; i < 16; i++){
			data.position(offset);
			data.put(intensity);
			offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
		}
	}
	
	public static final void setAlpha(FloatBuffer data, int offset, float alpha){
		offset += Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET;
		for (int i = 0; i < 16; i++){
			data.position(offset);
			data.put(alpha);
			offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
		}
	}
	

	public static void setUVMapping(FloatBuffer vertexData, int offset, RectF uvCoords) {
		setUVMapping(vertexData, offset, uvCoords.left, uvCoords.top, uvCoords.right, uvCoords.bottom);
	}

	
	/**
	 * Simple mapping method that expects that the patches go exactly to the center of the map.
	 */
	public static final void setUVMapping(FloatBuffer data, int offset, 
			float ulU, float ulV, float lrU, float lrV){
		
		float halfWidth = ulU + (lrU-ulU)/2;
		float halfHeight = ulV + (lrV-ulV)/2;
		tmpU[0] = ulU; tmpU[1] = halfWidth; tmpU[2] = halfWidth; tmpU[3] = lrU;
		tmpV[0] = ulV; tmpV[1] = halfHeight; tmpV[2] = halfHeight; tmpV[3] = lrV;
		
		offset += Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET;
		
		for (int y = 0; y < 4; y++){
			for (int x = 0; x < 4; x++){
				data.position(offset);
				data.put(tmpU[x]);
				data.put(tmpV[y]);
				offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
			}
		}
	}
	
	/**
	 * Sets the attribute pointers for the current renderProgram (which should
	 * be one of the texturing render programs!!) and paints it. The appropriate
	 * texture should already be bound previously.
	 */
	public static boolean renderTextured(RenderProgram rp,  
			int firstIndex, int patchCount, FloatBuffer patchData, ShortBuffer patchIndices) {
		
		// set the patch float buffer to be used as position source
		patchData.position(0);
		GLES20.glVertexAttribPointer(
				rp.vertexXyzHandle, 3, GLES20.GL_FLOAT, false, Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, patchData);
		GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);

		// set the patch float buffer to be used as colorSource
		patchData.position(Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET);
		GLES20.glVertexAttribPointer(
				rp.vertexUvHandle, 2, GLES20.GL_FLOAT, false,
				Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, patchData);
		GLES20.glEnableVertexAttribArray(rp.vertexUvHandle);
		
		// set the patch float buffer to be used as alpha source (if available in RenderProgram)
		if (rp.vertexAlphaHandle != -1){
			patchData.position(Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET);
			GLES20.glVertexAttribPointer(
					rp.vertexAlphaHandle, 1, GLES20.GL_FLOAT, false,
					Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, patchData);
			GLES20.glEnableVertexAttribArray(rp.vertexAlphaHandle);
		}
		
		if(rp.vertexPulseIntensityHandle != -1){
			patchData.position(Vertex.TEXTURED_VERTEX_DATA_PULSE_INTENSITY);
			GLES20.glVertexAttribPointer(
					rp.vertexPulseIntensityHandle, 1, GLES20.GL_FLOAT, false,
					Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, patchData);
			GLES20.glEnableVertexAttribArray(rp.vertexPulseIntensityHandle);
		}

		patchIndices.position(firstIndex * INDICES_COUNT);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, patchCount * INDICES_COUNT,
				GLES20.GL_UNSIGNED_SHORT, patchIndices);
		
		return true;
	}
}
