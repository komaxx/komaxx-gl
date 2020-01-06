package com.komaxx.komaxx_gl.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.komaxx.komaxx_gl.RenderConfig;
import com.komaxx.komaxx_gl.texturing.Texture;

/**
 * The atlas painter is a helpful tool that takes a series of drawable IDs
 * and a texture to paint all the drawables onto. It then returns the
 * according texture coordinates.
 * 
 * @author Matthias Schicker
 */
public class AtlasPainter {
	/**
	 * Can not be instantiated.
	 */
	private AtlasPainter(){
		// nothing
	}
	
	public static Rect[] drawAtlas(Resources r, int[] drawableIds, Texture t, int padding){
		return drawAtlas(r, drawableIds, t, new Rect(0,0,t.getWidth(), t.getHeight()), padding);
	}
	
	/**
	 * Draws the given drawableIDs to the texture (inside of the given paintBounds)
	 * and returns the coordinates (pixelCoords) where the drawables were painted.
	 * </br>
	 * <b>GLThread only. GL thread *must* have been created before.</b>
	 */
	public static Rect[] drawAtlas(Resources r, int[] drawableIds, Texture t, Rect paintBounds, int padding){
		// first: measure them vertically and horizontally, fit them into the paintBounds and
		// check, which way wastes less space.
		
		int l = drawableIds.length;
		Rect[] ret = new Rect[l];
		Drawable[] drawables = new Drawable[l];
		for (int i = 0; i < l; i++){
			Drawable nowDrawable = r.getDrawable(drawableIds[i]);
			drawables[i] = nowDrawable;
			ret[i] = new Rect(0, 0, nowDrawable.getIntrinsicWidth(), nowDrawable.getIntrinsicHeight());
		}
		
		positionHorizontally(ret, paintBounds.width(), padding);
		Rect horizontalBoundingRect = getBoundingRect(ret);
		
		positionVertically(ret, paintBounds.width(), padding);
		Rect boundingRect = getBoundingRect(ret);
		
		// horizontal was better? Then redo it!
		if (area(horizontalBoundingRect) <= area(boundingRect)){
			positionHorizontally(ret, paintBounds.width(), padding);
			boundingRect = horizontalBoundingRect;
		} 

		int bmpWidth = (int)Math.ceil(boundingRect.width());
		int bmpHeight = (int) Math.ceil(boundingRect.height());
		
		if (bmpWidth > t.getWidth() || bmpHeight > t.getHeight()){
			KoLog.w("[AtlasPainter]", "==== Uh oh ===");
			KoLog.w("[AtlasPainter]", "The given drawables did *NOT* fit into the given texture!");
			KoLog.w("[AtlasPainter]", "Teh bitmap will be truncated, graphic errors are inevitable!");
			
			bmpWidth = Math.min(bmpWidth, t.getWidth());
			bmpHeight = Math.min(bmpHeight, t.getHeight());
		}
		
		// now, let's render it!
		Bitmap tmpBmp = Bitmap.createBitmap(bmpWidth, bmpHeight, t.getBitmapConfig());
		Canvas c = new Canvas(tmpBmp);
		
		for (int i = 0; i < l; i++){
			drawables[i].setBounds(ret[i]);
			drawables[i].draw(c);
			
			ret[i].offset(paintBounds.left, paintBounds.top);
		}
		
		t.update(tmpBmp, paintBounds.left, paintBounds.top);

		if (RenderConfig.RECYCLE_BITMAPS) tmpBmp.recycle();
		
		return ret;
	}

	private static void positionVertically(Rect[] ret, int maxHeight, int padding) {
		int lineWidth = 0;
		
		int x = padding;
		int y = padding;
		Rect r;
		for (int i = 0; i < ret.length; i++){
			r = ret[i];
			if (y + r.height() > maxHeight){
				y = padding;
				x += lineWidth + padding;
				
				lineWidth = r.width()+padding;
			}
			
			r.offsetTo(x, y);
			y += r.height() + padding;
			
			lineWidth = Math.max(lineWidth, r.width());
		}
	}
	
	private static void positionHorizontally(Rect[] ret, int maxWidth, int padding) {
		int lineHeight = 0;
		
		int x = padding;
		int y = padding;
		Rect r;
		for (int i = 0; i < ret.length; i++){
			r = ret[i];
			if (x + r.width() > maxWidth){
				x = padding;
				y += lineHeight + padding;
				
				lineHeight = r.height()+padding;
			}
			
			r.offsetTo(x, y);
			x += r.width() + padding;
			
			lineHeight = Math.max(lineHeight, r.height());
		}
	}

	/**
	 * Computes the bounding box for all the given rects.
	 */
	public static Rect getBoundingRect(Rect[] r){
		if (r==null || r.length < 1) return new Rect();
		Rect ret = new Rect(r[0]);
		int l = r.length;
		for (int i = 1; i < l; i++){
			ret.union(r[i]);
		}
		return ret;
	}
	
	public static int area(Rect r) {
		return r.width() * r.height();
	}

	/**
	 * Convenience method that transforms the output of the AtlasPainter
	 * (which are pixel rects) into newly created uv-RectFs
	 */
	public static RectF[] convertPxToUv(Texture t, Rect[] pxRects) {
		RectF[] ret = new RectF[pxRects.length];
		
		for (int i = 0; i < ret.length; i++){
			ret[i] = t.getUvCoords(pxRects[i]);
		}
		
		return ret;
	}
	
	public static void runTest(){
		Rect[] r = new Rect[]{
			new Rect(0,0,30,30), new Rect(0,0,30,30), new Rect(0,0,40,40)
		};
		KoLog.i("TEST", "pre bounding rect " + getBoundingRect(r));
		
		positionHorizontally(r, 10000, 1);
		KoLog.i("TEST", "1 horizontal bounding rect " + getBoundingRect(r));

		positionVertically(r, 10000, 1);
		KoLog.i("TEST", "1 vertical bounding rect " + getBoundingRect(r));
		
		// with line break
		r = new Rect[]{
				new Rect(0,0,30,30), new Rect(0,0,30,30), new Rect(0,0,40,40)
			};

		positionVertically(r, 70, 1);
		KoLog.i("TEST", "2 vertical bounding rect " + getBoundingRect(r));

		positionHorizontally(r, 70, 1);
		KoLog.i("TEST", "2 horizontal bounding rect " + getBoundingRect(r));

		
		
		KoLog.i("TEST", "TEST DONE");
	}
}
