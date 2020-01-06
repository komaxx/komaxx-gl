package com.komaxx.komaxx_gl.renderprograms;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.util.RenderUtil;

public class SimpleColorProgram extends RenderProgram {
	@Override
	protected void findHandles() {
        vertexXyzHandle = GLES20.glGetAttribLocation(programHandle, "vertexXYZ");
        RenderUtil.checkGlError("glGetAttribLocation vertexXYZ");
        if (vertexXyzHandle == -1) {
            throw new RuntimeException("Could not get attrib location for position");
        }
		
        vertexColorHandle = GLES20.glGetAttribLocation(programHandle, "vertexColor");
        RenderUtil.checkGlError("glGetAttribLocation vertexColor");
        if (vertexXyzHandle == -1) {
            throw new RuntimeException("Could not get attrib location for position");
        }
		
        matrixMVPHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
		RenderUtil.checkGlError("glGetUniformLocation uMVPMatrix");
		if (matrixMVPHandle == -1) {
			throw new RuntimeException("Could not get uniform location for uMVPMatrix");
		}
	}

	@Override
	protected String getVertexShader() {
		return trivialVertexShader;
	}

	@Override
	protected String getFragmentShader() {
		return trivialFragmentShader;
	}

	private final String trivialVertexShader = 
		      "uniform mat4 uMVPMatrix;\n"
			+ "attribute vec4 vertexXYZ;\n" 
			+ "attribute vec4 vertexColor;\n"
			+ "varying vec4 vVertexColor;\n"
			+ "void main() {\n"
			+ "  vVertexColor = vertexColor;\n"
			+ "  gl_Position = uMVPMatrix * vertexXYZ;\n"
			+ "}\n";

	private final String trivialFragmentShader = 
			"precision lowp float;\n" 
			+ "varying vec4 vVertexColor;\n"
			+ "void main() {\n"
			+ "  gl_FragColor = vVertexColor;\n"
			+ "}\n";
}
