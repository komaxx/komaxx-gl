package com.komaxx.komaxx_gl.texturing;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.komaxx.komaxx_gl.RenderConfig;
import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.util.KoLog;
import com.komaxx.komaxx_gl.util.RenderUtil;

public class TextureStore {
	private static final boolean DEBUG = false;
	
	private Hashtable<Integer, ResourceTexture> resourceTextures = new Hashtable<Integer, TextureStore.ResourceTexture>();

	private static int[] tmpTextureHandle = new int[1];

	private static int nextOwnerID = 1;

	/**
	 * Whenever requesting a texture (or texture segment), obtain one
	 * of these global IDs first and use it to own the texture (segment).
	 */
	public static int createOwnerID(){
		return nextOwnerID++;
	}
	
	public TextureStore(){
	}

	public void reset(){
		// discard all old textures
		Enumeration<ResourceTexture> elements = resourceTextures.elements();
		while (elements.hasMoreElements()) elements.nextElement().delete();
		resourceTextures.clear();
		clearAllocationTracking();
	}
	
	public TextureStrip getTextureStrip(TextureStripConfig config){
		// TODO: check in pool whether still one stored in a cache.
		
		TextureStrip ret = new TextureStrip(config);
		
		return ret;
	}
	
	/**
	 * Delivers the texture (possibly creates it first). In case of new creation, the bound
	 * texture may change! No need to extra "create" like TextureStrips!
	 */
	public ResourceTexture getResourceTexture(RenderContext rc, int rawId, boolean mipMapped){
		ResourceTexture ret = resourceTextures.get(rawId);
		if (ret != null) return ret;

		ret = new ResourceTexture();
		ret.resourceId = rawId;
		ret.mipMapped = mipMapped;
		GLES20.glGenTextures(1, tmpTextureHandle, 0);

		ret.handle = tmpTextureHandle[0];
		rc.bindTexture(tmpTextureHandle[0]);

		if (mipMapped){
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
		} else {
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		}

		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		Bitmap bitmap = getBitmap(rc, rawId);
		ret.width = bitmap.getWidth();
		ret.height = bitmap.getWidth();
		
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		RenderUtil.checkGlError("Bind resource texture");
		if (RenderConfig.RECYCLE_BITMAPS) bitmap.recycle();
		
		if (mipMapped){
			GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
			RenderUtil.checkGlError("Generate mipmaps");
		}

		textureCreated(ret);
		
		resourceTextures.put(rawId, ret);
		return ret;
	}

	public static class ResourceTexture {
		private int resourceId;

		private int handle;

		private int width;
		private int height;

		private boolean mipMapped = false;

		private ResourceTexture(){
			// not publicly constructible
		}
		
		public int getHandle() {
			return handle;
		}
		
		public void delete() {
			tmpTextureHandle[0] = this.handle;
			GLES20.glDeleteTextures(1, tmpTextureHandle, 0);

			TextureStore.textureDeleted(this);
		}

		public int getWidth() {
			return width;
		}
		
		public int getHeight() {
			return height;
		}
		
		public int getResourceId() {
			return resourceId;
		}
		
		public boolean isMipMapped() {
			return mipMapped;
		}
	}

	/**
	 * Reads a BitmapDrawable from the application package and returns
	 * the encapsulated Bitmap.
	 */
	public static Bitmap getBitmap(RenderContext rc, int id){
		return getBitmap(rc.resources, id);
	}
	
	public static Bitmap getBitmap(Resources res, int id) {
		InputStream is = res.openRawResource(id);
		Bitmap bitmap;
		try {
			bitmap = BitmapFactory.decodeStream(is);
		} finally {
			try {
				is.close();
			} catch(Exception e) {
				KoLog.e("RenderUtil", "Error when loading bitmap " + id + ": " + e.toString());
			}
		}
		return bitmap;
	}

	
	// //////////////////////////////////////////////////////////////////
	// profiling
	
	private static int textureAllocations = 0;
	private static int usedTextureMemory = 0;
	
	public static void clearAllocationTracking(){
		textureAllocations = 0;
		usedTextureMemory = 0;
	}
	
	public static void textureCreated(Texture tex){
		TextureConfig config = tex.getConfig();
		textureMemoryAllocated(estimateSizeBytes(
				tex.getWidth(), tex.getHeight(), config.alphaChannel, config.mipMapped));
	}

	public static void textureCreated(ResourceTexture tex) {
		textureMemoryAllocated(
				estimateSizeBytes(tex.getWidth(), tex.getHeight(), true, tex.mipMapped));
	}
	
	public static void textureDeleted(Texture tex) {
		TextureConfig config = tex.getConfig();
		textureMemoryFreed(
				estimateSizeBytes(tex.getWidth(), tex.getHeight(), config.alphaChannel, config.mipMapped));
	}
	
	public static void textureDeleted(ResourceTexture tex) {
		textureMemoryFreed(
				estimateSizeBytes(tex.getWidth(), tex.getHeight(), true, tex.mipMapped));
	}
	
	private static void textureMemoryAllocated(int size){
		usedTextureMemory += size;
		textureAllocations++;
		if (DEBUG) KoLog.i("TextureStore", "Texture memory allocated! Allocations: "+textureAllocations
				+", overall size: "+usedTextureMemory);
	}
	
	private static void textureMemoryFreed(int size){
		usedTextureMemory -= size;
		textureAllocations--;
		
		if (DEBUG) KoLog.i("TextureStore", "Texture memory freed! Allocations: "+textureAllocations
				+", overall size: "+usedTextureMemory);
	}

	private static int estimateSizeBytes
		(int width, int height, boolean alpha, boolean mipMapped) {
		int pixelSize = (alpha ? 4 : 2);
		int ret = width * height * pixelSize;
		if (mipMapped){
			int tmpWidth = width/2;
			int tmpHeight = height/2;
			while (tmpWidth > 1 && tmpHeight > 1){
				ret += tmpWidth * tmpHeight * pixelSize; 
				tmpWidth /= 2;
				tmpHeight /= 2;
			}
		}
		return ret;
	}
}
