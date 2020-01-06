package com.komaxx.komaxx_gl.primitives;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.RectF;
import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.math.GlCube;
import com.komaxx.komaxx_gl.math.GlRect;
import com.komaxx.komaxx_gl.util.RenderUtil;

/**
 * Static methods to define and manipulate a quad with a position, texture-coords and
 * an alpha value for each vertex.
 *  
 * @author Matthias Schicker
 */
public class TexturedQuad {
	public static final int TRIANGLE_COUNT = 2;
	public static final int VERTEX_COUNT = 4;
	public static final int INDICES_COUNT = 2*3;
	

	public static final int QUAD_FLOATS = VERTEX_COUNT * Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	public static final int QUAD_BYTES = VERTEX_COUNT * Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES;
	
	private static final int UPPER_LEFT_XYZ_OFFSET = 0;
	private static final int LOWER_LEFT_XYZ_OFFSET = Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	private static final int LOWER_RIGHT_XYZ_OFFSET = 2 * Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	private static final int UPPER_RIGHT_XYZ_OFFSET = 3 * Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	
	private static final int UPPER_LEFT_UV_OFFSET = Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET;
	private static final int LOWER_LEFT_UV_OFFSET = Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET + Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	private static final int LOWER_RIGHT_UV_OFFSET = Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET + 2 * Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	private static final int UPPER_RIGHT_UV_OFFSET = Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET + 3 * Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	
	private static final int UPPER_LEFT_ALPHA_OFFSET = Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET;
	private static final int LOWER_LEFT_ALPHA_OFFSET = Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET + Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	private static final int LOWER_RIGHT_ALPHA_OFFSET = Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET + 2 * Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	private static final int UPPER_RIGHT_ALPHA_OFFSET = Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET + 3 * Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	
	private static final int UL_TEXTURE_INDEX_OFFSET = Vertex.TEXTURED_VERTEX_DATA_TEXTURE_INDEX_OFFSET;
	private static final int LL_TEXTURE_INDEX_OFFSET = Vertex.TEXTURED_VERTEX_DATA_TEXTURE_INDEX_OFFSET + Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	private static final int LR_TEXTURE_INDEX_OFFSET = Vertex.TEXTURED_VERTEX_DATA_TEXTURE_INDEX_OFFSET + 2 * Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	private static final int UR_TEXTURE_INDEX_OFFSET = Vertex.TEXTURED_VERTEX_DATA_TEXTURE_INDEX_OFFSET + 3 * Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	

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

	public static short[] allocateQuadIndexArray(int quadsCount) {
		short[] ret = new short[6 * quadsCount];
		
		int offset = 0;
		int index = 0;
		for (int i = 0; i < quadsCount; i++){
			ret[index++] = (short)(offset + 0);
			ret[index++] = (short)(offset + 1);
			ret[index++] = (short)(offset + 3);
			ret[index++] = (short)(offset + 3);
			ret[index++] = (short)(offset + 1);
			ret[index++] = (short)(offset + 2);
			offset += 4;
		}
		
		return ret;
	}

	public static FloatBuffer allocateQuads(int quadsCount) {
		return ByteBuffer.allocateDirect(
				quadsCount * VERTEX_COUNT * Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES
                ).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	public static final void position(FloatBuffer data, int offset, 
			float ulX, float ulY, float ulZ, float lrX, float lrY, float lrZ){
		
		// upper left vertex
		data.position(offset + UPPER_LEFT_XYZ_OFFSET);
		data.put(ulX);
		data.put(ulY);
		data.put(ulZ);
		
		// lower left vertex
		data.position(offset + LOWER_LEFT_XYZ_OFFSET);
		data.put(ulX);
		data.put(lrY);
		data.put(ulZ);
		
		// lower right vertex
		data.position(offset + LOWER_RIGHT_XYZ_OFFSET);
		data.put(lrX);
		data.put(lrY);
		data.put(ulZ);

		// upper right vertex
		data.position(offset + UPPER_RIGHT_XYZ_OFFSET);
		data.put(lrX);
		data.put(ulY);
		data.put(lrZ);
	}
	
	public static final void position(FloatBuffer data, int offset, GlCube pos){
		// upper left vertex
		data.position(offset + UPPER_LEFT_XYZ_OFFSET);
		data.put(pos.ulf[0]);
		data.put(pos.ulf[1]);
		data.put(pos.ulf[2]);
		
		// lower left vertex
		data.position(offset + LOWER_LEFT_XYZ_OFFSET);
		data.put(pos.ulf[0]);
		data.put(pos.lrb[1]);
		data.put(pos.ulf[2]);
		
		// lower right vertex
		data.position(offset + LOWER_RIGHT_XYZ_OFFSET);
		data.put(pos.lrb[0]);
		data.put(pos.lrb[1]);
		data.put(pos.lrb[2]);

		// upper right vertex
		data.position(offset + UPPER_RIGHT_XYZ_OFFSET);
		data.put(pos.lrb[0]);
		data.put(pos.ulf[1]);
		data.put(pos.lrb[2]);
	}

	public static final void position(FloatBuffer data, int offset, float[] ulf, float[] lrb){
		// upper left vertex
		data.position(offset + UPPER_LEFT_XYZ_OFFSET);
		data.put(ulf[0]);
		data.put(ulf[1]);
		data.put(ulf[2]);
		
		// lower left vertex
		data.position(offset + LOWER_LEFT_XYZ_OFFSET);
		data.put(ulf[0]);
		data.put(lrb[1]);
		data.put(ulf[2]);
		
		// lower right vertex
		data.position(offset + LOWER_RIGHT_XYZ_OFFSET);
		data.put(lrb[0]);
		data.put(lrb[1]);
		data.put(lrb[2]);

		// upper right vertex
		data.position(offset + UPPER_RIGHT_XYZ_OFFSET);
		data.put(lrb[0]);
		data.put(ulf[1]);
		data.put(lrb[2]);
	}
	
	public static final void position(FloatBuffer data, int offset, 
			float[] ul, float[] ll, float[] lr, float[] ur){
		// upper left vertex
		data.position(offset + UPPER_LEFT_XYZ_OFFSET);
		data.put(ul, 0, 3);
		
		// lower left vertex
		data.position(offset + LOWER_LEFT_XYZ_OFFSET);
		data.put(ll, 0, 3);
		
		// lower right vertex
		data.position(offset + LOWER_RIGHT_XYZ_OFFSET);
		data.put(lr, 0, 3);

		// upper right vertex
		data.position(offset + UPPER_RIGHT_XYZ_OFFSET);
		data.put(ur, 0, 3);
	}
	
	public static final void positionXY(FloatBuffer data, int offset, 
			float ulX, float ulY, float lrX, float lrY){
		
		// upper left vertex
		data.position(offset + UPPER_LEFT_XYZ_OFFSET);
		data.put(ulX);
		data.put(ulY);
		
		// lower left vertex
		data.position(offset + LOWER_LEFT_XYZ_OFFSET);
		data.put(ulX);
		data.put(lrY);
		
		// lower right vertex
		data.position(offset + LOWER_RIGHT_XYZ_OFFSET);
		data.put(lrX);
		data.put(lrY);

		// upper right vertex
		data.position(offset + UPPER_RIGHT_XYZ_OFFSET);
		data.put(lrX);
		data.put(ulY);
	}
	
	public static final void positionY(FloatBuffer data, int offset, float topY, float bottomY){
		// upper left vertex
		data.put(offset + UPPER_LEFT_XYZ_OFFSET + 1, topY);
		
		// lower left vertex
		data.put(offset + LOWER_LEFT_XYZ_OFFSET + 1, bottomY);
		
		// lower right vertex
		data.put(offset + LOWER_RIGHT_XYZ_OFFSET + 1, bottomY);

		// upper right vertex
		data.put(offset + UPPER_RIGHT_XYZ_OFFSET + 1, topY);
	}
	
	public static void positionXY(FloatBuffer data, int offset, GlRect b) {
		// upper left vertex
		data.position(offset + UPPER_LEFT_XYZ_OFFSET);
		data.put(b.left);
		data.put(b.top);
		
		// lower left vertex
		data.position(offset + LOWER_LEFT_XYZ_OFFSET);
		data.put(b.left);
		data.put(b.bottom);
		
		// lower right vertex
		data.position(offset + LOWER_RIGHT_XYZ_OFFSET);
		data.put(b.right);
		data.put(b.bottom);

		// upper right vertex
		data.position(offset + UPPER_RIGHT_XYZ_OFFSET);
		data.put(b.right);
		data.put(b.top);
	}

	/**
	 * @param alpha	MUST be of length >3. Assignment: ul, ll, lr, ur!
	 */
	public static void setAlpha(FloatBuffer data, int offset, float[] alpha) {
		// upper left vertex
		data.position(offset + UPPER_LEFT_ALPHA_OFFSET);
		data.put(alpha[0]);
		
		// lower left vertex
		data.position(offset + LOWER_LEFT_ALPHA_OFFSET);
		data.put(alpha[1]);
		
		// lower right vertex
		data.position(offset + LOWER_RIGHT_ALPHA_OFFSET);
		data.put(alpha[2]);

		// upper right vertex
		data.position(offset + UPPER_RIGHT_ALPHA_OFFSET);
		data.put(alpha[3]);
	}
	
	public static final void setAlpha(FloatBuffer data, int offset, float ulA, float llA, float urA, float lrA){
		// upper left vertex
		data.position(offset + UPPER_LEFT_ALPHA_OFFSET);
		data.put(ulA);
		
		// lower left vertex
		data.position(offset + LOWER_LEFT_ALPHA_OFFSET);
		data.put(llA);
		
		// lower right vertex
		data.position(offset + LOWER_RIGHT_ALPHA_OFFSET);
		data.put(lrA);

		// upper right vertex
		data.position(offset + UPPER_RIGHT_ALPHA_OFFSET);
		data.put(urA);
	}
	
	public static void setPulseIntensity(FloatBuffer data, int offset, float intensity) {
		// upper left vertex
		data.position(offset + UL_TEXTURE_INDEX_OFFSET);
		data.put(intensity);
		
		// lower left vertex
		data.position(offset + LL_TEXTURE_INDEX_OFFSET);
		data.put(intensity);
		
		// lower right vertex
		data.position(offset + LR_TEXTURE_INDEX_OFFSET);
		data.put(intensity);

		// upper right vertex
		data.position(offset + UR_TEXTURE_INDEX_OFFSET);
		data.put(intensity);
	}
	
	public static void setTextureIndex(FloatBuffer data, int offset, int index) {
		// upper left vertex
		data.position(offset + UL_TEXTURE_INDEX_OFFSET);
		data.put(index);
		
		// lower left vertex
		data.position(offset + LL_TEXTURE_INDEX_OFFSET);
		data.put(index);
		
		// lower right vertex
		data.position(offset + LR_TEXTURE_INDEX_OFFSET);
		data.put(index);

		// upper right vertex
		data.position(offset + UR_TEXTURE_INDEX_OFFSET);
		data.put(index);
	}

	
	public static final void setUVMapping(FloatBuffer data, int offset, RectF uvCoords) {
		setUVMapping(data, offset, uvCoords.left, uvCoords.top, uvCoords.right, uvCoords.bottom);
	}
	
	public static final void setUVMapping(FloatBuffer data, int offset, float ulU, float ulV, float lrU, float lrV){
		// upper left vertex
		data.position(offset + UPPER_LEFT_UV_OFFSET);
		data.put(ulU);
		data.put(ulV);
		
		// lower left vertex
		data.position(offset + LOWER_LEFT_UV_OFFSET);
		data.put(ulU);
		data.put(lrV);
		
		// lower right vertex
		data.position(offset + LOWER_RIGHT_UV_OFFSET);
		data.put(lrU);
		data.put(lrV);

		// upper right vertex
		data.position(offset + UPPER_RIGHT_UV_OFFSET);
		data.put(lrU);
		data.put(ulV);
	}
	
	public static void setUVMappingRotated(FloatBuffer data, int offset, RectF bounds) {
		setUVMappingRotated(data, offset, bounds.left, bounds.top, bounds.right, bounds.bottom);
	}

	
	public static void setUVMappingRotated(FloatBuffer data, int offset, float left, float top, float right, float bottom) {
		// upper left vertex
		data.position(offset + UPPER_LEFT_UV_OFFSET);
		data.put(right);
		data.put(top);
		
		// lower left vertex
		data.position(offset + LOWER_LEFT_UV_OFFSET);
		data.put(left);
		data.put(top);
		
		// lower right vertex
		data.position(offset + LOWER_RIGHT_UV_OFFSET);
		data.put(left);
		data.put(bottom);

		// upper right vertex
		data.position(offset + UPPER_RIGHT_UV_OFFSET);
		data.put(right);
		data.put(bottom);
	}


	/**
	 * To be used when only a quad's U mapping is to be set.
	 */
	public static final void setUMapping(FloatBuffer data, int offset, float ulU, float lrU){
		// upper left vertex
		data.position(offset + UPPER_LEFT_UV_OFFSET);
		data.put(ulU);
		
		// lower left vertex
		data.position(offset + LOWER_LEFT_UV_OFFSET);
		data.put(ulU);
		
		// lower right vertex
		data.position(offset + LOWER_RIGHT_UV_OFFSET);
		data.put(lrU);

		// upper right vertex
		data.position(offset + UPPER_RIGHT_UV_OFFSET);
		data.put(lrU);
	}


	/**
	 * Sets the attribute pointers for the current renderProgram (which should
	 * be one of the texturing render programs!!) and paints it. The appropriate
	 * texture should already be bound previously.
	 */
	public static boolean renderTextured(RenderProgram rp,  
			int first, int quadsCount, FloatBuffer quadsData, ShortBuffer quadsIndices) {
		
		first *= TexturedQuad.QUAD_FLOATS;
		
		// set the quad float buffer to be used as position source
		quadsData.position(first);
		GLES20.glVertexAttribPointer(
				rp.vertexXyzHandle, 3, GLES20.GL_FLOAT, false, Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, quadsData);
		GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);

		// set the quad float buffer to be used as textureSource
		if (rp.vertexUvHandle != -1){
			quadsData.position(first + Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET);
			GLES20.glVertexAttribPointer(
					rp.vertexUvHandle, 2, GLES20.GL_FLOAT, false,
					Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, quadsData);
			GLES20.glEnableVertexAttribArray(rp.vertexUvHandle);
		}
		
		// set the quad float buffer to be used as alpha source (if available in RenderProgram)
		if (rp.vertexAlphaHandle != -1){
			quadsData.position(first + Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET);
			GLES20.glVertexAttribPointer(
					rp.vertexAlphaHandle, 1, GLES20.GL_FLOAT, false,
					Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, quadsData);
			GLES20.glEnableVertexAttribArray(rp.vertexAlphaHandle);
		}

		// set the quad float buffer to be used as texture index source (if available in RenderProgram)
		if (rp.vertexTextureIndexHandle != -1){
			quadsData.position(first + Vertex.TEXTURED_VERTEX_DATA_TEXTURE_INDEX_OFFSET);
			GLES20.glVertexAttribPointer(
					rp.vertexTextureIndexHandle, 1, GLES20.GL_FLOAT, false,
					Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, quadsData);
			GLES20.glEnableVertexAttribArray(rp.vertexTextureIndexHandle);
		} else if (rp.vertexPulseIntensityHandle != -1){
			quadsData.position(first + Vertex.TEXTURED_VERTEX_DATA_TEXTURE_INDEX_OFFSET);
			GLES20.glVertexAttribPointer(
					rp.vertexPulseIntensityHandle, 1, GLES20.GL_FLOAT, false,
					Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, quadsData);
			GLES20.glEnableVertexAttribArray(rp.vertexPulseIntensityHandle);
		}
		
		quadsIndices.position(0);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, quadsCount * INDICES_COUNT,
				GLES20.GL_UNSIGNED_SHORT, quadsIndices);
		
		return true;
	}

	/**
	 * Creates a new short array with a length that can hold indices necessary for 'count' quads.
	 */
	public static short[] createIndexArray(int count) {
		return new short[INDICES_COUNT * count];
	}

	public static void getOffsetedIndices(short[] ret, short vertexOffset) {
		ret[0] = vertexOffset;
		ret[1] = ret[4] = (short)(vertexOffset + 1);
		ret[2] = ret[3] = (short)(vertexOffset + 3);
		ret[5] = (short)(vertexOffset + 2);
	}
	
	/**
	 * writes indices of one quad with offset 'vertexOffset' starting at index retBufferOffset
	 */
	public static void getOffsetedIndices(short[] ret, int retBufferOffset, short vertexOffset) {
		ret[retBufferOffset + 0] = vertexOffset;
		ret[retBufferOffset + 1] = ret[retBufferOffset + 4] = (short)(vertexOffset + 1);
		ret[retBufferOffset + 2] = ret[retBufferOffset + 3] = (short)(vertexOffset + 3);
		ret[retBufferOffset + 5] = (short)(vertexOffset + 2);
	}

	/**
	 * Adds the indices for a quad with the first vertex at <code>vertexOffset</code> 
	 * to the short buffer.
	 */
	public static void putOffsetedIndices(ShortBuffer ret, int offset) {
		ret.put((short) (offset + 0));
		ret.put((short) (offset + 1));
		ret.put((short) (offset + 3));
		ret.put((short) (offset + 3));
		ret.put((short) (offset + 1));
		ret.put((short) (offset + 2));
	}
	
	private TexturedQuad(){
		// no instances allowed!
	}	
}
