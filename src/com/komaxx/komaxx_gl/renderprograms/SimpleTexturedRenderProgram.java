package com.komaxx.komaxx_gl.renderprograms;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.util.RenderUtil;

public class SimpleTexturedRenderProgram extends RenderProgram {
	@Override
	protected void findHandles() {
        vertexXyzHandle = GLES20.glGetAttribLocation(programHandle, "aPosition");
        RenderUtil.checkGlError("glGetAttribLocation aPosition");
        if (vertexXyzHandle == -1) {
            throw new RuntimeException("Could not get attrib location for position");
        }
        vertexUvHandle = GLES20.glGetAttribLocation(programHandle, "aTextureCoord");
        RenderUtil.checkGlError("glGetAttribLocation aTextureCoord");
        if (vertexUvHandle == -1) {
            throw new RuntimeException("Could not get attrib location for textureCoord");
        }
		
		matrixMVPHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
		RenderUtil.checkGlError("glGetUniformLocation uMVPMatrix");
		if (matrixMVPHandle == -1) {
			throw new RuntimeException("Could not get uniform location for uMVPMatrix");
		}
	}
	
	@Override
	protected String getVertexShader() {
		return alphaTextureVertexShader;
	}

	@Override
	protected String getFragmentShader() {
		return alphaTextureFragmentShader;
	}

	private final String alphaTextureVertexShader = 
			  "uniform mat4 uMVPMatrix;\n"
			
			+ "attribute vec4 aPosition;\n"
			+ "attribute vec2 aTextureCoord;\n"
			
			+ "varying vec2 vTextureCoord;\n"
			
			+ "void main() {\n"
			+ "  gl_Position = uMVPMatrix * aPosition;\n"
			+ "  vTextureCoord = aTextureCoord;\n"
			+ "}\n";

	private final String alphaTextureFragmentShader = 
			  "precision highp float;\n"
			
			+ "uniform sampler2D sTexture;\n"
			
			+ "varying vec2 vTextureCoord;\n"
			
			+ "void main() {\n"
			+ "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
			+ "}\n";
}
