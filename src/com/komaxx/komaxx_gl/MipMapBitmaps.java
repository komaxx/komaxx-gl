package com.komaxx.komaxx_gl;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class MipMapBitmaps {
	private static Bitmap[] emptyBitmaps = new Bitmap[0];
	
	private Bitmap[] bitmaps;
	
	public MipMapBitmaps(int basicWidth, int basicHeight, Bitmap.Config config, int mipMapLevels){
		ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
		int i = 0;
		while (i < mipMapLevels && (basicWidth > 1 || basicHeight > 1)){
			bitmaps.add(Bitmap.createBitmap(basicWidth, basicHeight, config));
			basicWidth = Math.max(basicWidth >> 1, 1);
			basicHeight = Math.max(basicHeight >> 1, 1);
			i++;
		}
		this.bitmaps = bitmaps.toArray(emptyBitmaps);
	}
	
	public Bitmap[] getBitmaps() {
		return bitmaps;
	}

	@SuppressWarnings("unused")
	public void recycle() {
		if (bitmaps != null && RenderConfig.RECYCLE_BITMAPS){
			for (int i = 0; i < bitmaps.length; i++) bitmaps[i].recycle();
		}
		bitmaps = null;
	}
}
