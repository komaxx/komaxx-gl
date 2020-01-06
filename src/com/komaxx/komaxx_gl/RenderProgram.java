package com.komaxx.komaxx_gl;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.util.KoLog;
import com.komaxx.komaxx_gl.util.RenderUtil;

/**
 * A RenderProgram represents a vertex and fragment shader as well as the
 * handles to the used uniform and input fields 
 *  
 * @author Matthias Schicker
 */
public abstract class RenderProgram {
	public int programHandle = -1;
	
	// shader handles. Not all are necessarily used - which are available depends on the used renderer
	public int vertexXyzHandle = -1;
	public int vertexUvHandle = -1;
	public int vertexColorHandle = -1;
	public int vertexAlphaHandle = -1;
	
	// mutual exclusive!
	public int vertexTextureIndexHandle = -1;
	public int vertexPulseIntensityHandle = -1;
	
	public int vertexUvModFactorHandle = -1;
	
	public int uPulseIntensity = -1;
	public int uPulsePhase = -1;
	
	public int uColor = -1;
	public int uAlpha = -1;
	
	public int matrixMVPHandle = -1;
	
	public int uUvModificator = -1;
	
	public int texture0Handle = -1;
	public int texture1Handle = -1;
	public int texture2Handle = -1;
	public int texture3Handle = -1;
	
	
	public final void createProgram(){
        programHandle = createProgram(getVertexShader(), getFragmentShader());
        if (programHandle == 0) {
            throw new RuntimeException("Unable to create program: " + this);
        }

        findHandles();
	}

	/**
	 * Extension point to find the handles specific to this program.
	 * This connects shader variables to handles.
	 */
	protected abstract void findHandles();

	/**
	 * Utility function to find the handle for a uniform.
	 */
	protected int getUniformHandle(String s) {
		int ret = GLES20.glGetUniformLocation(programHandle, s);
		RenderUtil.checkGlError("glGetUniformLocation " + s);
		if (ret == -1) {
			throw new RuntimeException("Could not get uniform location for "+s);
		}
		return ret;
	}
	
	/**
	 * Utility function to read out the handle of an attribute (part of a vertex information)
	 */
	protected int getAttributeHandle(String s) {
		int ret = GLES20.glGetAttribLocation(programHandle, s);
		RenderUtil.checkGlError("glGetAttribLocation " + s);
		if (ret == -1) {
			throw new RuntimeException("Could not get attrib location for "+s);
		}
		return ret;
	}
	
	/**
	 * Changes the state of the GL to use this RenderProgram (costly!)
	 */
	public final void activate() {
		GLES20.glUseProgram(programHandle);
		onActivate();
	}
	
	/**
	 * extension point to execute additional code when activating.
	 */
	protected void onActivate(){
		// nothing in the generic case
	}

	private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            RenderUtil.checkGlError("glAttachShader vertex");
            GLES20.glAttachShader(program, pixelShader);
            RenderUtil.checkGlError("glAttachShader pixel");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                KoLog.e(this, "Could not link program: ");
                KoLog.e(this, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }
	
    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
            	KoLog.e(this, "Could not compile shader " + shaderType + ":");
                KoLog.e(this, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }
	
    /**
     * Deliver the source of the vertex shader.
     */
	protected abstract String getVertexShader();
	
	/**
	 * Deliver the source of the fragment shader.
	 */
	protected abstract String getFragmentShader();
}
