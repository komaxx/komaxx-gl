package com.komaxx.komaxx_gl.renderprograms;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.util.RenderUtil;

public class AlphaTestRenderProgram extends RenderProgram {
	@Override
	protected void findHandles() {
        vertexXyzHandle = GLES20.glGetAttribLocation(programHandle, "aPosition");
        RenderUtil.checkGlError("glGetAttribLocation aPosition");
        if (vertexXyzHandle == -1) {
            throw new RuntimeException("Could not get attrib location for position");
        }
        vertexAlphaHandle = GLES20.glGetAttribLocation(programHandle, "aAlpha");
        RenderUtil.checkGlError("glGetAttribLocation aAlpha");
        if (vertexAlphaHandle == -1) {
            throw new RuntimeException("Could not get attrib location for vertexAlpha");
        }

        matrixMVPHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
		RenderUtil.checkGlError("glGetUniformLocation uMVPMatrix");
		if (matrixMVPHandle == -1) {
			throw new RuntimeException("Could not get uniform location for uMVPMatrix");
		}
	}

	@Override
	protected String getVertexShader() {
		return textureTestVertexShader;
	}

	@Override
	protected String getFragmentShader() {
		return textureTestFragmentShader;
	}

	private final String textureTestVertexShader = 
		  "uniform mat4 uMVPMatrix;\n"
		
		+ "attribute vec4 aPosition;\n"
		+ "attribute float aAlpha;\n"
		
		+ "varying float vAlpha;\n"
		
		+ "void main() {\n"
		+ "  gl_Position = uMVPMatrix * aPosition;\n"
		+ "  vAlpha = aAlpha;\n"
		+ "}\n";

	private final String textureTestFragmentShader = 
		  "precision highp float;\n"
		
		+ "uniform sampler2D sTexture;\n"
		
		+ "varying float vAlpha;\n"
		
		+ "void main() {\n"
		+ "  gl_FragColor = vec4(1, vAlpha, vAlpha, 1);\n"
		+ "}\n";
}
