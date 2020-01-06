package com.komaxx.komaxx_gl.primitives;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderConfig;
import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.util.RenderUtil;

/**
 * Contains static utility functions to create and manipulate lines.
 *  
 * @author Matthias Schicker
 */
public class Line {
	public static final int VERTEX_COUNT = 2;
	
	// //////////////////////////////////////////////////////////////////////////
	// Color lines
	public static final int COLOR_LINE_FLOATS = VERTEX_COUNT * Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	public static final int COLOR_LINE_BYTES = VERTEX_COUNT * Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES;

	private static final int COLOR_ONE_OFFSET = 0;
	private static final int COLOR_TWO_OFFSET = Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;

	private static final int COLOR_ONE_COLOR_OFFSET = Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET;
	private static final int COLOR_TWO_COLOR_OFFSET = Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET + Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
	
	// //////////////////////////////////////////////////////////////////////////
	// Textured lines
	public static final int TEXTURED_LINE_FLOATS = VERTEX_COUNT * Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	public static final int TEXTURED_LINE_BYTES = VERTEX_COUNT * Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES;
	
	private static final int TEXTURED_ONE_XYZ_OFFSET = 0;
	private static final int TEXTURED_TWO_XYZ_OFFSET = Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	
	private static final int TEXTURED_ONE_UV_OFFSET = Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET;
	private static final int TEXTURED_TWO_UV_OFFSET = Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET + Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	
	private static final int TEXTURED_ONE_ALPHA_OFFSET = Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET;
	private static final int TEXTURED_TWO_ALPHA_OFFSET = Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET + Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
	
	public static FloatBuffer allocateColorLines(int count) {
		return ByteBuffer.allocateDirect(
				count * VERTEX_COUNT * Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES
                ).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	
	public static FloatBuffer allocateTexturedLines(int count) {
		return ByteBuffer.allocateDirect(
				count * VERTEX_COUNT * Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES
                ).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	
	public static final void positionColored(FloatBuffer data, int offset, 
			float x1, float y1, float z1, 
			float x2, float y2, float z2){
		
		// first vertex
		data.position(offset  + COLOR_ONE_OFFSET);
		data.put(x1);
		data.put(y1);
		data.put(z1);
		
		// second vertex
		data.position(offset + COLOR_TWO_OFFSET);
		data.put(x2);
		data.put(y2);
		data.put(z2);
	}
	
	public static void positionColoredX(FloatBuffer data, int offset, float x1, float x2){
		offset *= COLOR_LINE_FLOATS;
		// first vertex
		data.position(offset + COLOR_ONE_OFFSET);
		data.put(x1);
		
		// second vertex
		data.position(offset + COLOR_TWO_OFFSET);
		data.put(x2);
	}
	
	public static void positionColoredXY(FloatBuffer data,
			int offset, float x1, float y1, float x2, float y2) {

		// first vertex
		data.position(offset + COLOR_ONE_OFFSET);
		data.put(x1);
		data.put(y1);
		
		// secend vertex
		data.position(offset  + COLOR_TWO_OFFSET);
		data.put(x2);
		data.put(y2);
	}
	
	public static final void color(FloatBuffer data, int offset, 
			float r1, float g1, float b1, float r2, float g2, float b2){
		
		// first vertex
		data.position(offset + COLOR_ONE_COLOR_OFFSET);
		data.put(r1);
		data.put(g1);
		data.put(b1);
		
		// second vertex
		data.position(offset + COLOR_TWO_COLOR_OFFSET);
		data.put(r2);
		data.put(g2);
		data.put(b2);
	}
	
	public static void alpha(FloatBuffer data, int offset, float alpha) {
		offset += Vertex.COLOR_VERTEX_DATA_ALPHA_OFFSET;
		data.position(offset);
		data.put(alpha);
		
		offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
		data.position(offset);
		data.put(alpha);
	}

	public static void positionTexturedX(FloatBuffer data, int offset, float x1, float x2) {
		// first vertex
		data.position(offset + TEXTURED_ONE_XYZ_OFFSET);
		data.put(x1);
		
		// secend vertex
		data.position(offset + TEXTURED_TWO_XYZ_OFFSET);
		data.put(x2);
	}
	
	public static final void positionTextured(FloatBuffer data, int offset, 
			float x1, float y1, float z1, 
			float x2, float y2, float z2){
		
		// first vertex
		data.position(offset + TEXTURED_ONE_XYZ_OFFSET);
		data.put(x1);
		data.put(y1);
		data.put(z1);
		
		// secend vertex
		data.position(offset + TEXTURED_TWO_XYZ_OFFSET);
		data.put(x2);
		data.put(y2);
		data.put(z2);
	}
	
	public static void setAlpha(FloatBuffer data, int offset, float alpha, float alpha2) {
		// first vertex
		data.position(offset + TEXTURED_ONE_ALPHA_OFFSET);
		data.put(alpha);
		
		// second vertex
		data.position(offset + TEXTURED_TWO_ALPHA_OFFSET);
		data.put(alpha2);
	}

	
	public static final void setUVMapping(FloatBuffer data, int offset, 
			float u1, float v1, float u2, float v2){
		// upper left vertex
		data.position(offset + TEXTURED_ONE_UV_OFFSET);
		data.put(u1);
		data.put(v1);
		
		// lower left vertex
		data.position(offset + TEXTURED_TWO_UV_OFFSET);
		data.put(u2);
		data.put(v2);
	}
	
	/**
	 * Sets the attribute pointers for the current renderProgram (which should
	 * be one of the color render programs!!). The appropriate color tint
	 * should already be bound previously.
	 */
	public static boolean renderColored(RenderProgram rp, int first, int lineCount, FloatBuffer lineData) {
		lineData.position(0);
        GLES20.glVertexAttribPointer(
        		rp.vertexXyzHandle, 
        		3, GLES20.GL_FLOAT, false, Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, lineData);
        if (RenderConfig.GL_DEBUG && RenderUtil.checkGlError("glVertexAttribPointer vertexXYZ")) return false;
        GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);
        if (RenderConfig.GL_DEBUG && RenderUtil.checkGlError("glEnableVertexAttribArray vertexXYZ")) return false;
        
        
        // set the quad float buffer to be used as colorSource
        lineData.position(Vertex.COLOR_VERTEX_DATA_COLOR_OFFSET);
        GLES20.glVertexAttribPointer(
        		rp.vertexColorHandle, 4, GLES20.GL_FLOAT, false,
        		Vertex.COLOR_VERTEX_DATA_STRIDE_BYTES, lineData);
        if (RenderConfig.GL_DEBUG) RenderUtil.checkGlError("glVertexAttribPointer vertexColor");
        GLES20.glEnableVertexAttribArray(rp.vertexColorHandle);
        if (RenderConfig.GL_DEBUG) RenderUtil.checkGlError("glEnableVertexAttribArray vertexColorHandler");

        GLES20.glDrawArrays(GLES20.GL_LINES, first * VERTEX_COUNT, lineCount * VERTEX_COUNT);
		
		return true;
	}
	
	/**
	 * Sets the attribute pointers for the current renderProgram (which should
	 * be one of the color render programs!!). The appropriate color tint
	 * should already be bound previously.
	 */
	public static boolean renderTextured(RenderProgram rp, int first, int lineCount, FloatBuffer lineData) {
		// set the quad float buffer to be used as position source
		lineData.position(first);
		GLES20.glVertexAttribPointer(
				rp.vertexXyzHandle, 3, GLES20.GL_FLOAT, false, Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, lineData);
		GLES20.glEnableVertexAttribArray(rp.vertexXyzHandle);

		// set the quad float buffer to be used as colorSource
		lineData.position(first + Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET);
		GLES20.glVertexAttribPointer(
				rp.vertexUvHandle, 2, GLES20.GL_FLOAT, false,
				Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, lineData);
		GLES20.glEnableVertexAttribArray(rp.vertexUvHandle);
		
		// set the quad float buffer to be used as alpha source (if available in RenderProgram)
		if (rp.vertexAlphaHandle != -1){
			lineData.position(first + Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET);
			GLES20.glVertexAttribPointer(
					rp.vertexAlphaHandle, 1, GLES20.GL_FLOAT, false,
					Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES, lineData);
			GLES20.glEnableVertexAttribArray(rp.vertexAlphaHandle);
		}

		GLES20.glDrawArrays(GLES20.GL_LINES, first * VERTEX_COUNT, lineCount * VERTEX_COUNT);
		
		return true;
	}
}
