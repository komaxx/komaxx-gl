package com.komaxx.komaxx_gl.scenegraph;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.renderprograms.AlphaTestRenderProgram;
import com.komaxx.komaxx_gl.renderprograms.AlphaTextureRenderProgram;
import com.komaxx.komaxx_gl.renderprograms.DeppenShader;
import com.komaxx.komaxx_gl.renderprograms.SimpleColorProgram;
import com.komaxx.komaxx_gl.renderprograms.SimpleTexturedRenderProgram;
import com.komaxx.komaxx_gl.renderprograms.TextureTestRenderProgram;

/**
 * All available RenderPrograms (i.e., shader) are to be handled in this central
 * repository.
 * 
 * @author Matthias Schicker
 */
public abstract class ARenderProgramStore {
	private RenderProgram[] renderPrograms;
	
	public static final int DEPPEN_SHADER = 0;
	public static final int SIMPLE_COLORED = 1;
	public static final int TEXTURE_TEST = 2;
	public static final int ALPHA_TEST = 3;
	public static final int ALPHA_TEXTURED = 4;
	public static final int SIMPLE_TEXTURED = 5;
	
	private static final int DEFAULT_PROGRAMS_COUNT = 6;
	protected static final int FIRST_CUSTOM_RENDER_PROGRAM = DEFAULT_PROGRAMS_COUNT;

	
	public ARenderProgramStore(){
		buildRenderProgramArray();
	}

	public void activateRenderProgram(int renderProgramIndex) {
		renderPrograms[renderProgramIndex].activate();
	}

	public RenderProgram getRenderProgram(int renderProgramIndex) {
		return renderPrograms[renderProgramIndex];
	}

	/**
	 * Makes the RenderProgramStore (re-)create all RenderingPrograms.
	 */
	public void recreate() {
		for (int i = renderPrograms.length-1; i >=0; --i){
			renderPrograms[i].createProgram();
		}
		GLES20.glReleaseShaderCompiler();
	}
	
	private void buildRenderProgramArray(){
		int al = getAdditionalProgramsCount();
		renderPrograms = new RenderProgram[DEFAULT_PROGRAMS_COUNT + al];
		
		// create the inbuilt RenderPrograms
		renderPrograms[SIMPLE_COLORED] = new SimpleColorProgram();
		renderPrograms[TEXTURE_TEST] = new TextureTestRenderProgram();
		renderPrograms[ALPHA_TEST] = new AlphaTestRenderProgram();
		renderPrograms[DEPPEN_SHADER] = new DeppenShader();

		renderPrograms[ALPHA_TEXTURED] = new AlphaTextureRenderProgram();
		renderPrograms[SIMPLE_TEXTURED] = new SimpleTexturedRenderProgram();

		int l = al + DEFAULT_PROGRAMS_COUNT;
		for (int i = DEFAULT_PROGRAMS_COUNT; i < l; i++){
			renderPrograms[i] = buildRenderProgram(i);
		}
	}

	/**
	 * Your custom RenderPrograms will be requested in here. <br>
	 * <b>NOTE:</b> RenderPrograms will be activated and accessed via their
	 * index in this list. Therefore, you might want to create <i>public static int</i>
	 * constants for the indices. The first allowed index is FIRST_CUSTOM_RENDER_PROGRAM.
	 */
	protected abstract RenderProgram buildRenderProgram(int i);

	/**
	 * The system requests the amount of your custom RenderPrograms in here.
	 * @return	the number of your custom RenderPrograms (additional to the inbuilt programs).
	 */
	protected abstract int getAdditionalProgramsCount();
}
