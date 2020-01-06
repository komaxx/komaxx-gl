package com.komaxx.komaxx_gl.renderprograms;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.util.RenderUtil;

public class PulsatingRenderProgram extends RenderProgram {
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
        vertexAlphaHandle = GLES20.glGetAttribLocation(programHandle, "aAlpha");
        RenderUtil.checkGlError("glGetAttribLocation aAlpha");
        if (vertexAlphaHandle == -1) {
            throw new RuntimeException("Could not get attrib location for vertexAlpha");
        }
        vertexPulseIntensityHandle = GLES20.glGetAttribLocation(programHandle, "aPulseIntensity");
        RenderUtil.checkGlError("glGetAttribLocation aPulseIntensity");
        if (vertexPulseIntensityHandle == -1) {
            throw new RuntimeException("Could not get attrib location for pulseIntensity");
        }

        uPulseIntensity = GLES20.glGetUniformLocation(programHandle, "uPulseIntensity");
        RenderUtil.checkGlError("glGetAttribLocation uPulseIntensity");
        if (uPulseIntensity == -1) {
            throw new RuntimeException("Could not get attrib location for uPulseIntensity");
        }
        
        uPulsePhase = GLES20.glGetUniformLocation(programHandle, "uPulsePhase");
        RenderUtil.checkGlError("glGetAttribLocation uPulsePhase");
        if (uPulsePhase == -1) {
            throw new RuntimeException("Could not get attrib location for uPulsePhase");
        }
		
        matrixMVPHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
		RenderUtil.checkGlError("glGetUniformLocation uMVPMatrix");
		if (matrixMVPHandle == -1) {
			throw new RuntimeException("Could not get uniform location for uMVPMatrix");
		}
		
		texture0Handle = GLES20.glGetUniformLocation(programHandle, "sTexture");
		RenderUtil.checkGlError("glGetUniformLocation sTexture");
		if (texture0Handle == -1) {
			throw new RuntimeException("Could not get uniform location for sTexture");
		}
	}

	@Override
	protected void onActivate() {
		GLES20.glUniform1i(texture0Handle, 0);
	}
	
	@Override
	protected String getVertexShader() {
		return pulsatingTextureVertexShader;
	}

	@Override
	protected String getFragmentShader() {
		return pulsatingTextureFragmentShader;
	}

	private final String pulsatingTextureVertexShader = 
		      "uniform mat4 uMVPMatrix;\n"
			+ "attribute vec4 aPosition;\n"
			+ "attribute vec2 aTextureCoord;\n"
			+ "attribute float aAlpha;\n"
			+ "attribute float aPulseIntensity;\n"
			
			+ "varying vec2 vTextureCoord;\n"
			+ "varying float vAlpha;\n"
			+ "varying float vPulseIntensity;\n"
			
			+ "void main() {\n"
			+ "  gl_Position = uMVPMatrix * aPosition;\n"
			+ "  vTextureCoord = aTextureCoord;\n"
			+ "  vPulseIntensity = aPulseIntensity;\n"
			+ "  vAlpha = aAlpha;\n"
			+ "}\n";

	private final String pulsatingTextureFragmentShader = 
			  "precision mediump float;\n"
			
			+ "uniform sampler2D sTexture;\n"
			
			+ "uniform float uPulseIntensity;\n"
			+ "uniform float uPulsePhase;\n"
			
			+ "varying vec2 vTextureCoord;\n"
			+ "varying float vAlpha;\n"
			+ "varying float vPulseIntensity;\n"
			
			+ "void main() {\n"
			+ "  vec4 baseColor = texture2D(sTexture, vTextureCoord);\n"
			+ "    float phase = clamp((uPulsePhase/0.707106) - (length(vTextureCoord - vec2(0.5, 0.5)) / 0.707106), 0.0, 1.0) "
			+ "             * (3.142/0.707106);\n"
			+ "    float pulse = pow( clamp(sin(phase), 0.0, 1.0), 3.0 ) * uPulseIntensity;\n"
			+ "    baseColor.rgb = baseColor.rgb + pulse*vPulseIntensity;\n"
			+ "    gl_FragColor = baseColor * vAlpha;\n"
			+ "}\n";
}
