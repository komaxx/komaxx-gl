package com.komaxx.komaxx_gl.texturing;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * This class takes a rectangle of a texture and splits it into
 * TextureSegments. With that, a texture may be used as a manual
 * texture and a TextureStrip at the same time. If the manual part
 * is not necessary, it is advised to use a TextureStrip directly
 * as it is easier to use (and internally nothing different than
 * a texture with a managed TextureStripper).
 * 
 * @author Matthias Schicker
 */
public class TextureStripper {
	private final Texture texture;
	private final Rect bounds = new Rect();
	private final int segmentWidth;
	private final int segmentHeight;

	private int segmentCount;

	private TextureSegment[] segments;


	/**
	 * @param mayRotate If true, segments may actually be turned by 90 degrees, to
	 * use as much of the available space as possible. May have unexpected results when
	 * operating directly on texture coords.
	 */
	public TextureStripper(Texture texture, Rect bounds, int segmentWidth, int segmentHeight, boolean mayRotate) {
		this.texture = texture;
		this.bounds.set(bounds);
		this.segmentWidth = segmentWidth;
		this.segmentHeight = segmentHeight;
		createSegments(mayRotate);
	}

	public Texture getTexture() {
		return texture;
	}

	private void createSegments(boolean mayRotate) {
		// let's find out how many segments we can fit in there
		segmentCount = computeSegmentCount(mayRotate, false);
		int segmentCountRotated = mayRotate?computeSegmentCount(mayRotate, true) : 0;

		int sWidthPx = segmentWidth;
		int sHeightPx = segmentHeight;
		boolean rotateAll = false;

		if (segmentCountRotated > segmentCount){
			segmentCount = segmentCountRotated;
			sWidthPx = segmentHeight;
			sHeightPx = segmentWidth;
			rotateAll = true;
		}

		// create the segments
		segments = new TextureSegment[segmentCount];

		// assing the segments
		int boundsWidth = bounds.width();
		int boundsHeight = bounds.height();

		int horizontalSegmentsX = boundsWidth / sWidthPx;
		int verticalSegmentsX = mayRotate ? (boundsWidth % sWidthPx) / sHeightPx : 0;
		int xElements = horizontalSegmentsX + verticalSegmentsX;
		int xPadding = (xElements>1)   ? (int)(
				(float)(boundsWidth - (horizontalSegmentsX*sWidthPx + verticalSegmentsX*sHeightPx))
				/ (float)(xElements - 1) )
				: 0;

		int horizontalSegmentsY = boundsHeight / sHeightPx;
		int horizontalYPadding = horizontalSegmentsY>1  ?  
						(int)( (float)(boundsHeight%sHeightPx) / (float)(horizontalSegmentsY-1) )
						: 0;

		short i = 0;
		Rect tmpRect = new Rect(bounds.left, bounds.top, 
				bounds.left + sWidthPx, bounds.top + sHeightPx);
		TextureSegment nowSegment;

		// first, let's do the horizontal aligned segments
		for (int x = 0; x < horizontalSegmentsX; x++){
			tmpRect.offsetTo(tmpRect.left, bounds.top);
			for (int y = 0; y < horizontalSegmentsY; y++){
				nowSegment = new TextureSegment(texture, i, tmpRect, rotateAll);

				segments[i] = nowSegment;
				tmpRect.offset(0, sHeightPx + horizontalYPadding);
				i++;
			}
			tmpRect.offset(sWidthPx + xPadding, 0);
		}

		// now, the vertical aligned segments
		tmpRect.right = tmpRect.left + sHeightPx;
		tmpRect.bottom = tmpRect.top + sWidthPx;
		int verticalSegmentsY = boundsHeight / sWidthPx;
		int verticalYPadding = (verticalSegmentsY>1)  ?  
						(int)( (float)(boundsHeight%sWidthPx) / (float)(verticalSegmentsY-1) )
						:  0;
		for (;tmpRect.left < bounds.right; tmpRect.offset(sHeightPx + xPadding, 0)){
			tmpRect.offsetTo(tmpRect.left, bounds.top);
			for (int y = 0; y < verticalSegmentsY && i < segmentCount; y++){
				nowSegment = new TextureSegment(texture, i, tmpRect, !rotateAll);

				segments[i] = nowSegment;
				tmpRect.offset(0, sWidthPx + verticalYPadding);
				i++;
			}
		}
	}

	private int computeSegmentCount(boolean mayRotate, boolean rotated) {
		int maxWidth = bounds.width();
		int maxHeight = bounds.height();

		int sWidth = rotated?segmentHeight : segmentWidth;
		int sHeight = rotated?segmentWidth : segmentHeight;

		if (sWidth > maxWidth || sHeight > maxHeight) return 0;

		// horizontal
		int segmentsPerColumn = maxHeight/sHeight;
		int columns = maxWidth/sWidth;

		int count = segmentsPerColumn * columns;

		// vertical (rotated segments)
		if (mayRotate){
			int restX = maxWidth - (columns*sWidth);
			segmentsPerColumn = maxHeight/sWidth;
			columns = restX / sHeight;
			count += columns*segmentsPerColumn;
		}

		return count;
	}

	public int getSegmentCount() {
		return segmentCount;
	}

	/**
	 * Delivers the segment with the given id without side effects.
	 */
	public TextureSegment getSegment(short segmentId) {
		return segments[segmentId];
	}

	/**
	 * Just delivers the segment in the strip whose last usage frame is longest ago.
	 */
	public TextureSegment getOldestSegment(){
		// find least used segment
		int oldestTime = segments[0].lastUsedFrame;
		int oldestSegmentIndex = 0;
		int tmpTime;
		for (int i = 1; i < segmentCount; i++){
			if ((tmpTime = segments[i].lastUsedFrame) < oldestTime){
				oldestTime = tmpTime;
				oldestSegmentIndex = i;
			}
		}
		return segments[oldestSegmentIndex];
	}

	/**
	 * @param nuOwnerId		the unique identifier of the class that wants to own
	 * the texture segment. Best obtained by calling TextureStore.createOwnerId()
	 * @param frame		The frame when the request is called (normally 
	 * renderContex.frame). Used for LRU estimates.
	 */
	public TextureSegment getSegment(int nuOwnerId, int frame){
		TextureSegment ret = getOldestSegment();
		ret.own(nuOwnerId, frame);
		return ret;
	}

	@Override
	public String toString() {
		return "TextureStrip ("+bounds.toShortString()+") " + segmentCount + " segments ("
		+segmentWidth+"x"+segmentHeight+")";
	}

	public Bitmap createSegmentTmpBitmap() {
		return Bitmap.createBitmap(segmentWidth, segmentHeight, texture.getBitmapConfig());
	}
	
	public int getSegmentWidth() {
		return segmentWidth;
	}
	
	public int getSegmentHeight() {
		return segmentHeight;
	}
}
