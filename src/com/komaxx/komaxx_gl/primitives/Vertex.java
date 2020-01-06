package com.komaxx.komaxx_gl.primitives;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderConfig;
import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.util.RenderUtil;

/**
 * Contains static utility functions to create and manipulate vertices.
 *  
 * @author Matthias Schicker
 */
public class Vertex {

	public static final int TEXTURED_VERTEX_DATA_STRIDE_FLOATS = 7;		// position:3, textCoords:2, alpha:1, textureIndex:1
	public static final int TEXTURED_VERTEX_DATA_STRIDE_BYTES = TEXTURED_VERTEX_DATA_STRIDE_FLOATS * RenderUtil.FLOAT_SIZE_BYTES;
	public static final int TEXTURED_VERTEX_DATA_UV_OFFSET = 3;
	public static final int TEXTURED_VERTEX_DATA_ALPHA_OFFSET = 5;
	// mutual exclusive!
	public static final int TEXTURED_VERTEX_DATA_TEXTURE_INDEX_OFFSET = 6;
	public static final int TEXTURED_VERTEX_DATA_PULSE_INTENSITY = 6;


	public static final int COLOR_VERTEX_DATA_STRIDE_FLOATS = 7;
	public static final int COLOR_VERTEX_DATA_STRIDE_BYTES = 
			COLOR_VERTEX_DATA_STRIDE_FLOATS * RenderUtil.FLOAT_SIZE_BYTES;
	public static final int COLOR_VERTEX_DATA_COLOR_OFFSET = 3;
	public static final int COLOR_VERTEX_DATA_ALPHA_OFFSET = 6;



	public static ShortBuffer allocateIndices(int vertexCount) {
		ShortBuffer ret = ByteBuffer.allocateDirect(
				vertexCount * RenderUtil.SHORT_SIZE_BYTES
				).order(ByteOrder.nativeOrder()).asShortBuffer();

		return ret;
	}

	public static FloatBuffer allocateVertices(int count, int bytesPerVertex){
		return ByteBuffer.allocateDirect(
				count * bytesPerVertex
				).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	public static FloatBuffer allocateColorVertices(int count) {
		return ByteBuffer.allocateDirect(
				count * COLOR_VERTEX_DATA_STRIDE_BYTES
				).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	public static FloatBuffer allocateTexturedVertices(int count) {
		return ByteBuffer.allocateDirect(
				count * TEXTURED_VERTEX_DATA_STRIDE_BYTES
				).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}


	public static final void positionColored(FloatBuffer data, int offset, float x, float y, float z){
		data.position(offset);
		data.put(x);
		data.put(y);
		data.put(z);
	}

	public static final void color(FloatBuffer data, int offset, float r, float g, float b, float a){
		data.position(offset);
		data.put(r);
		data.put(g);
		data.put(b);
		data.put(a);
	}

	public static final void positionTextured(FloatBuffer data, int vertexIndex, float x, float y, float z){
		data.position(vertexIndex * TEXTURED_VERTEX_DATA_STRIDE_FLOATS);
		data.put(x);
		data.put(y);
		data.put(z);
	}

	public static final void setUVMapping(FloatBuffer data, int vertexIndex, float u, float v){
		data.position(vertexIndex * TEXTURED_VERTEX_DATA_STRIDE_FLOATS + TEXTURED_VERTEX_DATA_UV_OFFSET);
		data.put(u);
		data.put(v);
	}

	public static final void setAlpha(FloatBuffer data, int vertexIndex, float alpha){
		data.position(vertexIndex * TEXTURED_VERTEX_DATA_STRIDE_FLOATS + TEXTURED_VERTEX_DATA_ALPHA_OFFSET);
		data.put(alpha);
	}

	/**
	 * Sets the attribute pointers for the current renderProgram (which should
	 * be one of the color render programs!!). The appropriate color tint
	 * should already be bound previously.
	 */
	public static boolean renderColoredTriangles(RenderProgram rp,  
			int first, int renderedVertexCount, FloatBuffer vertexData, ShortBuffer vertexIndices) {
		vertexData.position(0);
		GLES20.glVertexAttribPointer(
				rp.vertexXyzHandle, 
				3, GLES20.GL_FLOAT, false, Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, vertexData);
		if (RenderConfig.GL_DEBUG && RenderUtil.checkGlError("glVertexAttribPointer vertexXYZ")) return false;
		GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);
		if (RenderConfig.GL_DEBUG && RenderUtil.checkGlError("glEnableVertexAttribArray vertexXYZ")) return false;

		// set the quad float buffer to be used as colorSource
		vertexData.position(Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET);
		GLES20.glVertexAttribPointer(
				rp.vertexColorHandle, 3, GLES20.GL_FLOAT, false,
				Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, vertexData);
		if (RenderConfig.GL_DEBUG) RenderUtil.checkGlError("glVertexAttribPointer vertexColor");
		GLES20.glEnableVertexAttribArray(rp.vertexColorHandle);
		if (RenderConfig.GL_DEBUG) RenderUtil.checkGlError("glEnableVertexAttribArray vertexColorHandler");

		vertexIndices.position(first);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, renderedVertexCount, GLES20.GL_UNSIGNED_SHORT, vertexIndices);

		return true;
	}

	/**
	 * Sets the attribute pointers for the current renderProgram (which should
	 * be one of the texturing render programs!!) and paints it. The appropriate
	 * texture should already be bound previously.
	 */
	public static boolean renderTexturedTriangles(RenderProgram rp,  
			int firstIndex, int renderedVertexCount, FloatBuffer vertexData, ShortBuffer vertexIndices) {

		// set the float buffer to be used as position source
		vertexData.position(0);
		GLES20.glVertexAttribPointer(
				rp.vertexXyzHandle, 
				3, GLES20.GL_FLOAT, false, Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, vertexData);
		GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);

		// set the float buffer to be used as colorSource
		if (rp.vertexUvHandle != -1){
			vertexData.position(Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET);
			GLES20.glVertexAttribPointer(
					rp.vertexUvHandle, 2, GLES20.GL_FLOAT, false,
					Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, vertexData);
			GLES20.glEnableVertexAttribArray(rp.vertexUvHandle);
		}

		// set the float buffer to be used as alpha source
		if (rp.vertexAlphaHandle != -1){
			vertexData.position(Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET);
			GLES20.glVertexAttribPointer(
					rp.vertexAlphaHandle, 1, GLES20.GL_FLOAT, false,
					Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, vertexData);
			GLES20.glEnableVertexAttribArray(rp.vertexAlphaHandle);
		}

		// set the quad float buffer to be used as texture index source (if available in RenderProgram)
		if (rp.vertexTextureIndexHandle != -1){
			vertexData.position(Vertex.TEXTURED_VERTEX_DATA_TEXTURE_INDEX_OFFSET);
			GLES20.glVertexAttribPointer(
					rp.vertexTextureIndexHandle, 1, GLES20.GL_FLOAT, false,
					Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, vertexData);
			GLES20.glEnableVertexAttribArray(rp.vertexTextureIndexHandle);
		} else if(rp.vertexPulseIntensityHandle != -1){
			vertexData.position(Vertex.TEXTURED_VERTEX_DATA_PULSE_INTENSITY);
			GLES20.glVertexAttribPointer(
					rp.vertexPulseIntensityHandle, 1, GLES20.GL_FLOAT, false,
					Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, vertexData);
			GLES20.glEnableVertexAttribArray(rp.vertexPulseIntensityHandle);
		}

		vertexData.position(0);
		vertexIndices.position(firstIndex);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 
				renderedVertexCount, GLES20.GL_UNSIGNED_SHORT, vertexIndices);

		return true;
	}

	public static boolean renderTexturedTriangles(RenderProgram rp, int firstIndex, int indexCount,
			ShortBuffer indexBuffer) {

		// position
		GLES20.glVertexAttribPointer(rp.vertexXyzHandle, 3, GLES20.GL_FLOAT, false, 
				Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, 0);
		GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);

		// textured
		if (rp.vertexUvHandle != -1){
			GLES20.glVertexAttribPointer(
					rp.vertexUvHandle, 2, GLES20.GL_FLOAT, false,
					TEXTURED_VERTEX_DATA_STRIDE_BYTES, 
					TEXTURED_VERTEX_DATA_UV_OFFSET * RenderUtil.FLOAT_SIZE_BYTES);
			GLES20.glEnableVertexAttribArray(rp.vertexUvHandle);
		}

		// set the float buffer to be used as alpha source
		if (rp.vertexAlphaHandle != -1){
			GLES20.glVertexAttribPointer(
					rp.vertexAlphaHandle, 1, GLES20.GL_FLOAT, false,
					TEXTURED_VERTEX_DATA_STRIDE_BYTES, 
					TEXTURED_VERTEX_DATA_ALPHA_OFFSET * RenderUtil.FLOAT_SIZE_BYTES);
			GLES20.glEnableVertexAttribArray(rp.vertexAlphaHandle);
		}

		// set the quad float buffer to be used as texture index source (if available in RenderProgram)
		if (rp.vertexTextureIndexHandle != -1){
			GLES20.glVertexAttribPointer(
					rp.vertexTextureIndexHandle, 1, GLES20.GL_FLOAT, false,
					TEXTURED_VERTEX_DATA_STRIDE_BYTES, 
					TEXTURED_VERTEX_DATA_TEXTURE_INDEX_OFFSET * RenderUtil.FLOAT_SIZE_BYTES);
			GLES20.glEnableVertexAttribArray(rp.vertexTextureIndexHandle);
		} 

		indexBuffer.position(firstIndex);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 
				indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

		return true;
	}

	public static boolean renderTexturedLines(RenderProgram rp, int firstIndex, int indicesCount, ShortBuffer indexBuffer) {
		// position
		GLES20.glVertexAttribPointer(rp.vertexXyzHandle, 3, GLES20.GL_FLOAT, false, 
				Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, 0);
		GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);

		// textured
		if (rp.vertexUvHandle != -1){
			GLES20.glVertexAttribPointer(
					rp.vertexUvHandle, 2, GLES20.GL_FLOAT, false,
					TEXTURED_VERTEX_DATA_STRIDE_BYTES, 
					TEXTURED_VERTEX_DATA_UV_OFFSET * RenderUtil.FLOAT_SIZE_BYTES);
			GLES20.glEnableVertexAttribArray(rp.vertexUvHandle);
		}

		// set the float buffer to be used as alpha source
		if (rp.vertexAlphaHandle != -1){
			GLES20.glVertexAttribPointer(
					rp.vertexAlphaHandle, 1, GLES20.GL_FLOAT, false,
					TEXTURED_VERTEX_DATA_STRIDE_BYTES, 
					TEXTURED_VERTEX_DATA_ALPHA_OFFSET * RenderUtil.FLOAT_SIZE_BYTES);
			GLES20.glEnableVertexAttribArray(rp.vertexAlphaHandle);
		}

		// set the quad float buffer to be used as texture index source (if available in RenderProgram)
		if (rp.vertexTextureIndexHandle != -1){
			GLES20.glVertexAttribPointer(
					rp.vertexTextureIndexHandle, 1, GLES20.GL_FLOAT, false,
					TEXTURED_VERTEX_DATA_STRIDE_BYTES, 
					TEXTURED_VERTEX_DATA_TEXTURE_INDEX_OFFSET * RenderUtil.FLOAT_SIZE_BYTES);
			GLES20.glEnableVertexAttribArray(rp.vertexTextureIndexHandle);
		} 

		indexBuffer.position(firstIndex);
		GLES20.glDrawElements(GLES20.GL_LINES, 
				indicesCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

		return true;
	}

	public static boolean renderColoredLines(RenderProgram rp, int first, int indicesCount, ShortBuffer indexBuffer) {
		// position
		GLES20.glVertexAttribPointer(rp.vertexXyzHandle, 3, GLES20.GL_FLOAT, false, 
				Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, 0);
		GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);

		// textured
		if (rp.vertexColorHandle != -1){
			GLES20.glVertexAttribPointer(
					rp.vertexColorHandle, 4, GLES20.GL_FLOAT, false,
					COLOR_VERTEX_DATA_STRIDE_BYTES, 
					COLOR_VERTEX_DATA_COLOR_OFFSET * RenderUtil.FLOAT_SIZE_BYTES);
			GLES20.glEnableVertexAttribArray(rp.vertexColorHandle);
		}

		indexBuffer.position(first);
		GLES20.glDrawElements(GLES20.GL_LINES, 
				indicesCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

		return true;
	}


	public static boolean renderColoredLines(RenderProgram rp, int vertexCount, FloatBuffer lineVertices) {
		lineVertices.position(0);
		// positioning
		GLES20.glVertexAttribPointer(
				rp.vertexXyzHandle, 
				3, GLES20.GL_FLOAT, false, Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, lineVertices);
		if (RenderConfig.GL_DEBUG && RenderUtil.checkGlError("glVertexAttribPointer vertexXYZ")) return false;
		GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);
		if (RenderConfig.GL_DEBUG && RenderUtil.checkGlError("glEnableVertexAttribArray vertexXYZ")) return false;

		// set the line float buffer to be used as colorSource
		lineVertices.position(Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET);
		GLES20.glVertexAttribPointer(
				rp.vertexColorHandle, 4, GLES20.GL_FLOAT, false,
				Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, lineVertices);
		if (RenderConfig.GL_DEBUG) RenderUtil.checkGlError("glVertexAttribPointer vertexColor");
		GLES20.glEnableVertexAttribArray(rp.vertexColorHandle);
		if (RenderConfig.GL_DEBUG) RenderUtil.checkGlError("glEnableVertexAttribArray vertexColorHandler");

		lineVertices.position(0);
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);

		return true;
	}

	/**
	 * Draws a vertex VBO (which needs to be already bound!) with a user space index buffer
	 */
	public static void renderColoredTriangles(
			RenderProgram renderProgram, int firstIndex, int vertexCount,
			ShortBuffer indexBuffer) {

		GLES20.glVertexAttribPointer(renderProgram.vertexXyzHandle, 3, GLES20.GL_FLOAT, false, 
				Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, 0);
		GLES20.glEnableVertexAttribArray(renderProgram.vertexXyzHandle);

		GLES20.glVertexAttribPointer(renderProgram.vertexColorHandle, 4, GLES20.GL_FLOAT, false, 
				Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, RenderUtil.FLOAT_SIZE_BYTES*3);
		GLES20.glEnableVertexAttribArray(renderProgram.vertexColorHandle);

		indexBuffer.position(firstIndex);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
	}
}
