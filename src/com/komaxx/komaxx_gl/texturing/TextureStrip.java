package com.komaxx.komaxx_gl.texturing;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.komaxx.komaxx_gl.MipMapBitmaps;


/**
 * A TextureStrip is a texture that is automatically split up in
 * separate rectangle chunks. Contains also logic to manage these
 * "segments".
 * 
 * @author Matthias Schicker
 */
public class TextureStrip extends Texture {
	private final TextureStripConfig config;

	private TextureStripper textureStripper;
	
	// to be used during updates.
	private MipMapBitmaps segmentTmpBmpHorizontal;
	private MipMapBitmaps segmentTmpBmpVertical;
	
	
	public TextureStrip(TextureStripConfig config) {
		super(config);
		this.config = config.clone();
		computeTextureSize();
		textureStripper = new TextureStripper(
				this, new Rect(0,0,width,height), config.segmentWidth, config.segmentHeight, config.mayRotate);
	}
	
	@Override
	public TextureStripConfig getConfig() {
		return config;
	}
	
	private void computeTextureSize() {
		switch (config.proportionsType){
		case TextureStripConfig.PROPORTIONS_QUADRATIC:
			computeQuadraticTextureSize();
			break;
		case TextureStripConfig.PROPORTIONS_COLUMN:
			computeColumnTextureSize();
			break;
		case TextureStripConfig.PROPORTIONS_ROW:
			computeRowTextureSize();
			break;
		default:
			throw new RuntimeException("unknown texture strip proportions: " + config.proportionsType);
		}		
	}

	private void computeRowTextureSize() {
		int nuSegmentHeight = findNextPowerOfTwo(config.segmentHeight);
		height = config.segmentHeight = nuSegmentHeight;
		
		int minWidth = config.segmentWidth * config.minSegmentCount;
		if (minWidth > 2048) throw new RuntimeException("Can't create row texture with width > 2048");
		width = findNextPowerOfTwo(minWidth);
	}

	private void computeColumnTextureSize() {
		int nuSegmentWidth = findNextPowerOfTwo(config.segmentWidth);
		width = config.segmentWidth = nuSegmentWidth;
		
		int minHeight = config.segmentHeight * config.minSegmentCount;
		if (minHeight > 2048) throw new RuntimeException("Can't create column texture with height > 2048");
		height = findNextPowerOfTwo(minHeight);
	}

	private void computeQuadraticTextureSize() {
		int tmpSize = 
			Math.max(findNextPowerOfTwo(config.minWidth),
					 findNextPowerOfTwo(config.minHeight));
		while (computeSegmentCountQuadratic(tmpSize) < config.minSegmentCount){
			tmpSize *=2;
		}
		width = height = tmpSize;
	}

	private int computeSegmentCountQuadratic(int tmpSize) {
		int sWidth = config.segmentWidth;
		int sHeight = config.segmentHeight;
		
		if (sWidth > tmpSize || sHeight > tmpSize) return 0;
		
		// horizontal
		int segmentsPerColumn = tmpSize/sHeight;
		int columns = tmpSize/sWidth;
		
		int count = segmentsPerColumn * columns;
		
		// vertical
		int restX = tmpSize - (columns*sWidth);
		segmentsPerColumn = tmpSize/sWidth;
		columns = restX / sHeight;
		count += columns*segmentsPerColumn;
		
		return count;
	}

	public int getSegmentCount() {
		return textureStripper.getSegmentCount();
	}
	
	/**
	 * Delivers the segment with the given id without side effects.
	 */
	public TextureSegment getSegment(short segmentId) {
		return textureStripper.getSegment(segmentId);
	}
	
	/**
	 * Just delivers the segment in the strip whose last usage frame is longest ago.
	 */
	public TextureSegment getOldestSegment(){
		return textureStripper.getOldestSegment();
	}
	
	public TextureSegment getSegment(int nuOwnerId, int frame){
		TextureSegment ret = getOldestSegment();
		ret.own(nuOwnerId, frame);
		return ret;
	}
	
	@Override
	public String toString() {
		return "TextureStrip ("+width+"x"+height+") " + getSegmentCount() + " segments ("
				+config.segmentWidth+"x"+config.segmentHeight+")";
	}

	/**
	 * removes the texture from the GL and frees all resources. GL thread only.
	 */
	@Override
	public void delete() {
		super.delete();
		this.textureStripper = null;
		if (segmentTmpBmpHorizontal != null){
			segmentTmpBmpHorizontal.recycle();
			segmentTmpBmpHorizontal = null;
		}
		if (segmentTmpBmpVertical != null){
			segmentTmpBmpVertical.recycle();
			segmentTmpBmpVertical= null;
		}
	}

	public Bitmap createSegmentTmpBitmap() {
		return Bitmap.createBitmap(config.segmentWidth, config.segmentHeight, getBitmapConfig());
	}
}
