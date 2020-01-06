package com.komaxx.komaxx_gl.texturing;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.komaxx.komaxx_gl.RenderConfig;
import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.util.ObjectsStore;
import com.komaxx.komaxx_gl.util.RenderUtil;


public class Texture {
	public static boolean highResolutionColor = true;
	
	protected static Config ALPHA_CONFIG = highResolutionColor ? Config.ARGB_8888 : Config.ARGB_4444;
	protected static Config OPAQUE_CONFIG = highResolutionColor ? Config.ARGB_8888 : Config.RGB_565;
	
	private final TextureConfig config;

	protected int handle;

	protected int width;
	protected int height;

	public Texture(TextureConfig config) {
		this.config = config.clone();
		width = findNextPowerOfTwo(config.minWidth);
		height = findNextPowerOfTwo(config.minHeight);
	}
	
	public TextureConfig getConfig() {
		return config;
	}
	
	protected static int findNextPowerOfTwo(int segmentWidth) {
		int ret = 32;
		while (ret < segmentWidth) ret*=2;
		return ret;
	}


	/**
	 * MUST be called before the texture can be used in any way!
	 */
	public boolean create(RenderContext rc) {
		GLES20.glGenTextures(1, ObjectsStore.tmpIntBuffer, 0);

		handle = ObjectsStore.tmpIntBuffer[0];
		rc.bindTexture(handle);

		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, 
				config.mipMapped ? GLES20.GL_LINEAR_MIPMAP_LINEAR : 
					(config.nearestMapping ? GLES20.GL_NEAREST : GLES20.GL_LINEAR));

		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, 
				config.nearestMapping ? GLES20.GL_NEAREST : GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, config.edgeBehavior);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, config.edgeBehavior);
        
        if (config.alphaChannel){
        	GLES20.glTexImage2D(
        			GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 
        			width, height, 0, GLES20.GL_RGBA, 
        			highResolutionColor ? GLES20.GL_UNSIGNED_BYTE : GLES20.GL_UNSIGNED_SHORT_4_4_4_4, null);
        } else {
        	Bitmap b = Bitmap.createBitmap(width, height, OPAQUE_CONFIG);
        	Canvas c = new Canvas(b);
        	c.drawColor(0xFF000000 | config.basicColor);
        	
        	GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, b, 0);
        	if (RenderConfig.RECYCLE_BITMAPS) b.recycle();
        }

		if (RenderConfig.GL_DEBUG && RenderUtil.checkGlError("Bind texture")) return false;
		
		if (config.mipMapped){
			GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
			if (RenderConfig.GL_DEBUG) RenderUtil.checkGlError("Generate mipmaps");
		}
		
		TextureStore.textureCreated(this);
		
		return true;
	}

	/**
	 * Fills the complete texture with the given color. May only
	 * be called after 'create'.
	 */
	public void clear(RenderContext rc){
		// First: Make sure that 'this' texture is changed.
		rc.bindTexture(handle);
		
		int format = config.alphaChannel ? GLES20.GL_RGBA : GLES20.GL_RGB;
		int type = config.alphaChannel ? 
				(highResolutionColor ? GLES20.GL_UNSIGNED_BYTE : GLES20.GL_UNSIGNED_SHORT_4_4_4_4) : 
				GLES20.GL_RGB565;
		
		Buffer b = ByteBuffer.wrap(new byte[width * 3]);
		
		// Stream the empty pixels one line at a time
		for (int i = 0; i < height; i++){
			GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, i, width, 1, format, type, b);
		}
		
		RenderUtil.checkGlError("clear");

	}
	
	public int getHandle(){
		return handle;
	}
	
	@Override
	public String toString() {
		return "Texture ("+width+"x"+height+") ";
	}

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

	/**
	 * Replace the image data of this texture.
	 * <b>NOTE</b> Make sure that this texture is currently bound in the GL - 
	 * this call will *NOT* bind it!
	 */
	public void update(Bitmap updateBitmap, int mipMapLevel) {
		if (config.alphaChannel && updateBitmap.getConfig() != ALPHA_CONFIG){
			updateBitmap = updateBitmap.copy(ALPHA_CONFIG, false);
		} else if (!config.alphaChannel && updateBitmap.getConfig() != OPAQUE_CONFIG){
			updateBitmap = updateBitmap.copy(OPAQUE_CONFIG, false);
		}
		
		int targetSize = width >> mipMapLevel;
		
		if (updateBitmap.getWidth() != targetSize || updateBitmap.getHeight() != targetSize){
			updateBitmap = Bitmap.createScaledBitmap(updateBitmap, targetSize, targetSize, true); 
		}

		GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, mipMapLevel, 0, 0, updateBitmap);
		
		if (RenderConfig.GL_DEBUG) RenderUtil.checkGlError("Updating texture segment");
	}
	
	/**
	 * Update a part of the texture</br>
	 * So far unfortunately only for non-mipmapped textures. </br>
	 * <b>NOTE</b> Make sure that this texture is currently bound in the GL - 
	 * this call will *NOT* bind it!
	 */
	public void update(Bitmap updateBitmap, int xPos, int yPos) {
		if (config.alphaChannel && updateBitmap.getConfig() != ALPHA_CONFIG){
			updateBitmap = updateBitmap.copy(ALPHA_CONFIG, false);
		} else if (!config.alphaChannel && updateBitmap.getConfig() != OPAQUE_CONFIG){
			updateBitmap = updateBitmap.copy(OPAQUE_CONFIG, false);
		}

		GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, xPos, yPos, updateBitmap);
		
		if (RenderConfig.GL_DEBUG) RenderUtil.checkGlError("Updating texture");
	}
	
	public Config getBitmapConfig() {
		return config.alphaChannel ? ALPHA_CONFIG : OPAQUE_CONFIG;
	}

	/**
	 * removes the texture from the GL and frees all resources. GL thread only.
	 */
	public void delete() {
		ObjectsStore.tmpIntBuffer[0] = this.handle;
		GLES20.glDeleteTextures(1, ObjectsStore.tmpIntBuffer, 0);
		
		TextureStore.textureDeleted(this);
	}

	/**
	 * Translates pixel coords into uvCoords. Creates and returns a new RectF.
	 */
	public RectF getUvCoords(Rect r) {
		return getUvCoords(new RectF(), r);
	}
	
	/**
	 * Translates pixel coords into uv coords in the texture. Returns a new RectF
	 * with the uv coords set.
	 */
	public RectF getUvCoords(float left, float top, float right, float bottom) {
		return new RectF(
				(float)left / (float)width, (float)top / (float)height,
				(float)right / (float)width, (float)bottom / (float)height
				);
	}

	/**
	 * translates pixel coords into uv coords. <code>ret</code> may not be <code>null</code>!
	 */
	public RectF getUvCoords(RectF ret, Rect r) {
		ret.set((float)r.left / (float)width, (float)r.top / (float)height,
				(float)r.right / (float)width, (float)r.bottom / (float)height
				);
		return ret;
	}
	
	public static void setHighResolutionColor(boolean hrc){
		highResolutionColor = hrc;
		ALPHA_CONFIG = highResolutionColor ? Config.ARGB_8888 : Config.ARGB_4444;
		OPAQUE_CONFIG = highResolutionColor ? Config.ARGB_8888 : Config.RGB_565;
	}
}
