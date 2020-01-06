package com.komaxx.komaxx_gl;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.komaxx.komaxx_gl.scenegraph.ARenderProgramStore;
import com.komaxx.komaxx_gl.texturing.TextureStore;
import com.komaxx.komaxx_gl.util.RenderUtil;


/**
 * A RenderContext is an object that will be available for every node when
 * traversing through the scene graph. 
 * It contains the current state of the GL and other states that are necessary 
 * for rendering.
 * 
 * @author Matthias Schicker
 */
public class RenderContext extends SceneGraphContext {
	public Resources resources;
	
	public TextureStore textureStore;
	public RenderProgram currentRenderProgram;
	public ARenderProgramStore renderProgramStore;

	public int currentRenderProgramIndex = -1;
	public int boundTexture = -1;
	public int boundVboId = -1;

	
	/**
	 * indicates whether the current MVP-matrix is not loaded into the current shader
	 */
	private boolean mvpMatrixInShader = false;
	
	/**
	 * Specifies whether depth buffering is currently active or not. Will be set by
	 * nodes during rendering.
	 */
	public boolean depthTestActivated;
	
	/**
	 * Specifies whether blending is currently active or not. Will be set by nodes
	 * during rendering.
	 */
	public boolean blendingActivated;
	
	/**
	 * The currently enabled blending function (GLES20 constant). Only to change the
	 * destination blend function, the source blend function is always SRC_ALPHA.
	 * No effect when blending not activated.
	 */
	public int blendFunction;
	
	/**
	 * Will be incremented whenever a new surface is created.
	 */
	public int surfaceId = -1;
	
	
	public RenderContext(ARenderProgramStore renderProgramStore) {
		this.renderProgramStore = renderProgramStore;
	}

	/**
	 * resets this context to the values of the provided context.
	 */
	public final void reset(RenderContext basicRenderContext) {
		basicReset(basicRenderContext);

		resources = basicRenderContext.resources;
		textureStore = basicRenderContext.textureStore;
		surfaceId = basicRenderContext.surfaceId;
		
		depthTestActivated = false;
		activateDepthTest(true);
		
		blendingActivated = true;
		activateBlending(false);
		
		boundTexture = -1;
		boundVboId = -1;
		resetRenderProgram();
	}

	/**
	 * Makes sure that no old RenderProgram will be reused. To be called
	 * on onSurfaceCreated. 
	 */
	public final void resetRenderProgram(){
		currentRenderProgramIndex = -1;
		currentRenderProgram = null;
	}
	
	/**
	 * Discards all bound textures. Typically called after a surface changes
	 */
	public final void resetTextureStore() {
		textureStore.reset();
	}
	
	/**
	 * Activates a different RenderProgram.
	 * 
	 * @param renderProgramIndex	The renderProgram (referenced per index)
	 * to activate.
	 */
	public final void switchRenderProgram(int renderProgramIndex) {
		if (renderProgramIndex == currentRenderProgramIndex) return;
		renderProgramStore.activateRenderProgram(renderProgramIndex);
		currentRenderProgramIndex = renderProgramIndex;
		currentRenderProgram = renderProgramStore.getRenderProgram(renderProgramIndex);
		mvpMatrixInShader = false;
	}

	@Override
	public void setMvpMatrixDirty() {
		super.setMvpMatrixDirty();
		mvpMatrixInShader = false;
	}
	
	public void setMvpMatrixInShader() {
		mvpMatrixInShader = true;
	}
	
	public boolean isMvpMatrixInShader() {
		return mvpMatrixInShader;
	}
	
	/**
	 * Normally, users of the scene graph do not have to call this. Only when
	 * RenderPrograms are switched manually, this needs to be called - otherwise
	 * all render commands will most likely fail.
	 */
	public void applyMvpMatrixToShader(){
		if (mvpMatrixInShader) return;
    	GLES20.glUniformMatrix4fv(
    			currentRenderProgram.matrixMVPHandle, 1, false, getMvpMatrix(), 0);
    	setMvpMatrixInShader();
	}
	
	public final void bindVBO(int vboId) {
		if (vboId != boundVboId){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
			boundVboId = vboId;
		}
	}
	
	public final void bindTexture(int textureHandle){
		if (textureHandle != boundTexture){
//			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);		// only necessary once!
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
			boundTexture = textureHandle;
		}
	}

	public void activateDepthTest(boolean b) {
		if (b != depthTestActivated){
			if (b) GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			else GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			depthTestActivated = b;
		}
	}
	
	public void activateBlending(boolean activateBlending){
		if (activateBlending != blendingActivated){
			if (activateBlending){
				GLES20.glEnable(GLES20.GL_BLEND);
				if (RenderConfig.GL_DEBUG) RenderUtil.checkGlError("enable blending");
			} else {
				GLES20.glDisable(GLES20.GL_BLEND);
				if (RenderConfig.GL_DEBUG)RenderUtil.checkGlError("disable blending");
			}
			blendingActivated = activateBlending;
		}
	}
	
	public void setBlendFunction(int nuBlendFunction){
		if (nuBlendFunction != blendFunction){
			GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, nuBlendFunction);
			blendFunction = nuBlendFunction;
		}
	}
}
