package com.komaxx.komaxx_gl.texturing;

import java.nio.FloatBuffer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.komaxx.komaxx_gl.MipMapBitmaps;
import com.komaxx.komaxx_gl.RenderConfig;
import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.primitives.TexturedQuad;
import com.komaxx.komaxx_gl.util.KoLog;
import com.komaxx.komaxx_gl.util.RenderUtil;

public class TextureSegment {
	private Texture texture;
	
	private final short id;
	protected int ownerId;
	protected int lastUsedFrame = -1;
	protected final Rect pixelCoords = new Rect();
	protected final RectF uvCoords;
	protected final boolean vertical;
	
	private static Matrix bitmapRotMatrix = new Matrix();
	
	private static RectF tmpRect = new RectF();
	
	public TextureSegment(Texture t, short id, Rect pixelCoords, boolean rotated){
		this.texture = t;
		this.id = id;
		this.vertical = rotated;
		this.pixelCoords.set(pixelCoords);
		
		this.uvCoords = t.getUvCoords(pixelCoords);
//		if (vertical) {
//			this.uvCoords.set(
//					uvCoords.left, uvCoords.top,
//					uvCoords.left + uvCoords.height(), uvCoords.top + uvCoords.width()
//					);
//		}
	}
	
	public short getId() {
		return id;
	}
	
	public int getOwnerId() {
		return ownerId;
	}
	
	/**
	 * Call this to mark the segment as still used by the current owner.
	 */
	public void finger(int frame) {
		lastUsedFrame = frame;
	}
	
	public int getPixelWidth(){
		return pixelCoords.width();
	}
	
	public int getPixelHeight(){
		return pixelCoords.height();
	}
	
	public Rect getPixelCoords() {
		return pixelCoords;
	}
	
	public RectF getUvCoords(){
		return uvCoords;
	}
	
	/**
	 * Takes pixel coords inside of the Segment and returns texture-wide uvCoords
	 */
	public RectF getUvCoords(Rect r) {
		return texture.getUvCoords(
				r.left + pixelCoords.left, 
				r.top + pixelCoords.top,
				r.right + pixelCoords.left,
				r.bottom + pixelCoords.top);
	}
	
	public boolean update(RenderContext rc, Bitmap updateBitmap, int mipMapLevel){
		TextureConfig config = texture.getConfig();
		
		int sWidth = vertical ? pixelCoords.height() : pixelCoords.width();
		int sHeight = vertical ? pixelCoords.width() : pixelCoords.height();
		for (int i = 0; i < mipMapLevel; i++){
			sWidth = sWidth/2;
			sHeight = sHeight/2;
		}
		sWidth = Math.max(sWidth, 1);
		sHeight = Math.max(sHeight, 1);

		// does the configuration of the update image fit the texture?
		if (	
				(config.alphaChannel && updateBitmap.getConfig() != Texture.ALPHA_CONFIG ) ||
				(!config.alphaChannel && updateBitmap.getConfig() != Texture.OPAQUE_CONFIG) ||
				(updateBitmap.getWidth() != sWidth || updateBitmap.getHeight() != sHeight)
		){
			Bitmap tmpBitmap = getTmpBitmap(sWidth, sHeight);
			Canvas c = new Canvas(tmpBitmap);
			tmpRect.left = 0; tmpRect.right = sWidth;
			tmpRect.top = 0; tmpRect.bottom = sHeight; 
			c.drawBitmap(updateBitmap, null, tmpRect, null);
			updateBitmap = tmpBitmap;
			
			if (RenderConfig.GL_DEBUG){
				KoLog.w(this, "Warning, incorrect update bitmap. Was adapted (slow)");
			}
		}		
		
		if (vertical){
			bitmapRotMatrix.reset();
			bitmapRotMatrix.postRotate(90);
			bitmapRotMatrix.postTranslate(sHeight, 0);
			Bitmap rotBitmap = getTmpBitmap(sHeight, sWidth);
			rotBitmap.eraseColor(Color.TRANSPARENT);
			Canvas c = new Canvas(rotBitmap);
			c.drawBitmap(updateBitmap, bitmapRotMatrix, null);
			updateBitmap = rotBitmap;
		}
		
		if (rc.boundTexture != texture.getHandle()){
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getHandle());
		}
		
		int format = config.alphaChannel ? GLES20.GL_RGBA : GLES20.GL_RGB;
		int type = Texture.highResolutionColor ? GLES20.GL_UNSIGNED_BYTE : 
			(config.alphaChannel ? GLES20.GL_UNSIGNED_SHORT_4_4_4_4 : GLES20.GL_UNSIGNED_SHORT_5_6_5);

		GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, mipMapLevel, 
				pixelCoords.left >> mipMapLevel, 
				pixelCoords.top >> mipMapLevel, 
				updateBitmap, format, type);

//		GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, mipMapLevel,
//				pixelCoords.left >> mipMapLevel, pixelCoords.top >> mipMapLevel,
//				sWidth, sHeight, format, type, updateBitmap.);
		
		if (RenderConfig.GL_DEBUG) return !RenderUtil.checkGlError("Updating texture segment");
		return true;
	}
	
	/**
	 * GL THREAD ONLY!
	 * @param sHeight 
	 */
	protected Bitmap getTmpBitmap(int sWidth, int sHeight){
		
		// TODO: A pooling/caching might be in order here!
		
//		if (vertical && segmentTmpBmpVertical==null){
//			segmentTmpBmpVertical = new MipMapBitmaps(config.segmentHeight, config.segmentWidth, 
//					getBitmapConfig(), 1000);
//		} else if (!vertical && segmentTmpBmpHorizontal==null){
//			segmentTmpBmpHorizontal = new MipMapBitmaps(config.segmentWidth, config.segmentHeight, 
//					getBitmapConfig(), 1000);
//		}
		
//		return vertical 
//					? segmentTmpBmpVertical.getBitmaps()[mipMapLevel] 
//					: segmentTmpBmpHorizontal.getBitmaps()[mipMapLevel];
		
		// TODO: MipMap levels!
		
		return Bitmap.createBitmap(sWidth, sHeight, texture.getBitmapConfig());
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append(id).append(": owner ").append(ownerId).append('(').append(lastUsedFrame).append("): ");
		ret.append(vertical ? "|| " : "== ");
		ret.append(pixelCoords.toShortString()).append(" -> ").append(uvCoords.toString());
		
		return ret.toString();
	}

	/**
	 * Call this, when not the full segment is to be used as texture but only a horizontal part.
	 * Vertical, the full height will be used.
	 */
	public void applyUvMappingToQuad(FloatBuffer quadsData, int currentOffset, float widthPercentage){
		tmpRect.set(uvCoords);
		if (vertical){
			tmpRect.bottom = uvCoords.top + uvCoords.height()*widthPercentage;
			TexturedQuad.setUVMappingRotated(quadsData, currentOffset, tmpRect);
		} else {
			tmpRect.right = uvCoords.left + uvCoords.width()*widthPercentage;
			TexturedQuad.setUVMapping(quadsData, currentOffset, tmpRect);
		}
	}
	
	/**
	 * Call this, when not the full segment is to be used as texture but only a horizontal part.
	 * Vertical, the full height will be used.
	 */
	public void applyUvMappingToQuad(FloatBuffer quadsData, int currentOffset, 
			float hZoomFactor, float vZoomFactor){
		tmpRect.set(uvCoords);
		if (vertical){
			tmpRect.bottom = uvCoords.top + uvCoords.height()*hZoomFactor;
			tmpRect.left = uvCoords.right - uvCoords.width()*vZoomFactor;
			TexturedQuad.setUVMappingRotated(quadsData, currentOffset, tmpRect);
		} else {
			tmpRect.right = uvCoords.left + uvCoords.width()*hZoomFactor;
			tmpRect.bottom = uvCoords.top + uvCoords.height()*vZoomFactor;
			TexturedQuad.setUVMapping(quadsData, currentOffset, tmpRect);
		}
	}
	
	public void applyUvMappingToQuad(FloatBuffer quadsData, int currentOffset) {
		if (vertical){
			TexturedQuad.setUVMappingRotated(quadsData, currentOffset, uvCoords);
		} else {
			TexturedQuad.setUVMapping(quadsData, currentOffset, uvCoords);
		}
	}

	public void update(RenderContext rc, MipMapBitmaps updateBitmaps, int startLevel) {
		Bitmap[] bitmaps = updateBitmaps.getBitmaps();
		int l = bitmaps.length;
		for (int i = 0; i < l; i++){
			update(rc, bitmaps[i], startLevel + i);
		}
	}

	/**
	 * Builds a bitmap that fits the segment regarding size and config. The segment will
	 * not hold any references. Should be recycled as soon as the caller doesn't need it
	 * anymore.
	 */
	public Bitmap createTmpBmp() {
		return Bitmap.createBitmap(
				vertical ? pixelCoords.height() : pixelCoords.width(), 
				vertical ? pixelCoords.width() : pixelCoords.height(),
						texture.getBitmapConfig());
	}
	
	public boolean isVertical() {
		return vertical;
	}

	public void own(int nuOwnerId, int frame) {
		ownerId = nuOwnerId;
		lastUsedFrame = frame;
	}

	public int getLastUsedFrame() {
		return lastUsedFrame;
	}
	
	public Texture getTexture() {
		return texture;
	}
}
