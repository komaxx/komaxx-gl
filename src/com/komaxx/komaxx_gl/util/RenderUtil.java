package com.komaxx.komaxx_gl.util;

import java.nio.FloatBuffer;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLU;
import android.util.Log;

import com.komaxx.komaxx_gl.texturing.Texture;

/**
 * Contains all sorts of utility constants and functions useful
 * when rendering.
 * 
 * @author Matthias Schicker
 */
public class RenderUtil {
	private static final Random r = new Random(System.currentTimeMillis());
	
	public static final int SHORT_SIZE_BYTES = 2;
	public static final int FLOAT_SIZE_BYTES = 4;
    
	public static final short[] indexArrayDummy = new short[0];

	private static final float PI = (float) Math.PI;

	
	public static Bitmap emptyBmp = Bitmap.createBitmap(1, 1, Config.ARGB_4444);
	static {
		emptyBmp.eraseColor(Color.TRANSPARENT);
	}
	
    public static boolean checkGlError(String op) {
        int error;
        boolean errorFound = false;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
        	KoLog.e("RenderUtil", op + ": glError " + Integer.toHexString(error) + ", " + GLU.gluErrorString(error));
        	errorFound = true;
        }
        return errorFound;
    }
    
    /**
     * Computes a smooth transition from an arbitrary input space into [0|1]
     */
    public static float smoothStep(float value, float min, float max){
    	value = clamp(value, min, max) - min;
    	return value / (max-min);
    }
    
    public static float degreesToRadians(float degrees){
    	return (degrees / 180f) * PI;
    }
    
    /**
     * Prints the content of the buffer without changing the mark or pos values.
     */
    public static String printBuffer(FloatBuffer buffer, int start, int length){
    	StringBuffer ret = new StringBuffer();
    	ret.append('(');
    	
    	if (length > 0){
    		ret.append(buffer.get(start));
    	}
    	for (int i = start+1; i < start+length; i++){
    		ret.append(" | ").append(buffer.get(start));
    	}
    	
    	ret.append(')');
    	return ret.toString();
    }
    
    public static String toString(PointF p, int digits) {
		float digitFactor = 1;
		for (int i = 0; i < digits; i++) digitFactor *= 10;
		StringBuffer ret = new StringBuffer();
		ret.append('(');
		ret.append( (int)(p.x*digitFactor) / digitFactor );
		ret.append(" | ");
		ret.append( (int)(p.y*digitFactor) / digitFactor );
		ret.append(')');
		return ret.toString();
	}
    
    public static String printFloat(float f, int maxDigits){
		float digitFactor = (float) Math.pow(10, maxDigits);
		return "" + ((int)(f*digitFactor) / digitFactor);
    }
    
	private static float[] tmpHsv = new float[3];
	public static int getRandomColor() {
		tmpHsv[0] = r.nextFloat() * 360f;
		tmpHsv[1] = 1;
		tmpHsv[2] = 1;
		return Color.HSVToColor(tmpHsv);
	}
	
	public static float getRandom(float min, float max){
		return min + r.nextFloat() * (max-min);
	}

	/**
	 * Delivers a rescaled version of the "src"-Bitmap. The result has the same aspect ratio as the input image and
	 * is smaller-or-equal than maxSizeX and maxSizeY
	 */
	public static Bitmap getRescaledBitmapWithSameAspectRatio(Bitmap src, float maxWidth, float maxHeight){
		try {
			float inputAspectRatio = (float)src.getWidth() / (float)src.getHeight();
			if (inputAspectRatio > (float)maxWidth / (float)maxHeight){
				return Bitmap.createScaledBitmap(src, 
						clamp((int)maxWidth, 1, 10000), 
						clamp((int)(maxWidth / inputAspectRatio), 1, 10000), true);
			}
			return Bitmap.createScaledBitmap(src, 
					clamp((int)(maxHeight * inputAspectRatio), 1, 10000),
					clamp((int)maxHeight,1, 10000), true);
		} catch (OutOfMemoryError error){
			KoLog.e("RenderUtil", "[getRescaledBitmap] Caught an OutOfMemory-error!");
			System.gc();
			return emptyBmp;
		}
	}
	

    /**
     * Converts a color defined as HSV to a rgb color array, one float [0,1] for each color,
     * Input outside of their respective ranges will be clamped to a valid value.
     * @param result	an array to fill. Must have at least length=3. May be null.
     * @param h			Hue in [0 .. 360]
     * @param s			Saturation [0...1]
     * @param v			Value [0...1]
     * @return			If result!=null: result, else: new array of length 4.
     */
	public static float[] hsv2rgb(float[] result, int h, int s, int v) {
		tmpHsv[0] = h;
		tmpHsv[1] = s;
		tmpHsv[2] = v;
		return color2floatsRGB(result, Color.HSVToColor(tmpHsv));
	}

	
	/**
	 * Converts a color to a float array, one float [0,1] for each color
	 * @param ret		an array to fill. Must have at least length=3. May be null.
	 * @param color		the color to convert
	 * @return			If ret!=null: ret, else: new array of length 4.
	 */
	public static float[] color2floatsRGB(float[] ret, int color) {
		if (ret == null){
			ret = new float[4];
			ret[3] = 1;
		}
		ret[0] = (float)Color.red(color)/255f;
		ret[1] = (float)Color.green(color)/255f;
		ret[2] = (float)Color.blue(color)/255f;
		return ret;
	}
	
	/**
	 * Converts a color to a float array, one float [0,1] for each color
	 * @param ret		an array to fill. Must have at least length=4. May be null.
	 * @param color		the color to convert
	 * @return			If ret!=null: ret, else: new array of length 4.
	 */
	public static float[] color2floatsRGBA(float[] ret, int color) {
		if (ret == null){
			ret = new float[4];
		}
		ret[0] = (float)Color.red(color)/255f;
		ret[1] = (float)Color.green(color)/255f;
		ret[2] = (float)Color.blue(color)/255f;
		ret[3] = (float)Color.alpha(color)/255f;
		return ret;
	}
	
	/**
	 * Interpolates a sin-value meandering between min and max. Valuable
	 * for test purposes.
	 */
	public static float sinStep(long frameNanoTime, float intervalLengthMs, float min, float max){
		float phase = (float) ((float)(frameNanoTime / 1000000) / intervalLengthMs * Math.PI);
		
		float ret = (float) Math.sin(phase);
		ret = 
			// normalize to [0;1]
			(ret+1)/2f
			// stretch to result area
			* (max-min);
		// translate to result area
		ret += min;

		return ret;
	}
	
	/**
	 * Clamps <code>value</code> to the specified interval. Note: There are
	 * no sanity checks in here, so you should make sure yourself that 
	 * minIncl &lt;= maxIncl
	 * @param value
	 * 		The value to be clamped
	 * @param minIncl
	 * 		The smallest allowed value
	 * @param maxIncl
	 * 		The biggest allowed value
	 * @return
	 * 		value clamped to [minIncl;maxIncl]
	 */
	public static final int clamp(int value, int minIncl, int maxIncl){
		if (value > maxIncl) value = maxIncl;
		if (value < minIncl) value = minIncl;
		return value;
	}
	
	/**
	 * Clamps <code>value</code> to the specified interval. Note: There are
	 * no sanity checks in here, so you should make sure yourself that 
	 * minIncl &lt;= maxIncl
	 * @param value
	 * 		The value to be clamped
	 * @param minIncl
	 * 		The smallest allowed value
	 * @param maxIncl
	 * 		The biggest allowed value
	 * @return
	 * 		value clamped to [minIncl;maxIncl]
	 */
	public static final float clamp(float value, float minIncl, float maxIncl){
		if (value > maxIncl) value = maxIncl;
		if (value < minIncl) value = minIncl;
		return value;
	}

	/**
	 * Clamps <code>value</code> to the specified interval. Note: There are
	 * no sanity checks in here, so you should make sure yourself that 
	 * minIncl &lt;= maxIncl
	 * @param value
	 * 		The value to be clamped
	 * @param minIncl
	 * 		The smallest allowed value
	 * @param maxIncl
	 * 		The biggest allowed value
	 * @return
	 * 		value clamped to [minIncl;maxIncl]
	 */
	public static final double clamp(double value, double minIncl, double maxIncl){
		if (value > maxIncl) value = maxIncl;
		if (value < minIncl) value = minIncl;
		return value;
	}
	
	public static final byte max(byte a, byte b) {
		return (a>b) ? a : b;
	}

	public static boolean isNullOrEmpty(String s) {
		return s == null || s.length() < 1;
	}
	
	/**
     * Finds the minimum of three <code>int</code> values
     *
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     */
    public static int min(int a, int b, int c) {
        if (b < a) a = b;
        if (c < a) a = c;
        return a;
    }
    
    private static final Rect sourceRect = new Rect();
    private static final Rect paintRect = new Rect();
    private static final Paint bmpPaint = new Paint();
    static { 
    	bmpPaint.setAntiAlias(true); 
    	bmpPaint.setFilterBitmap(true);
    }
    public static Bitmap getRescaledBitmapToFill(Bitmap src, float fillWidth, float fillHeight){
	    try {
	        Bitmap ret = Bitmap.createBitmap((int)fillWidth, (int)fillHeight, src.getConfig());
            float sourceAspectRatio = (float)src.getWidth() / (float)src.getHeight();
            float targetAspectRatio = (float)fillWidth / (float)fillHeight;
            Canvas c = new Canvas(ret);
            
            float sourceWidth = src.getWidth();
            float sourceHeight = src.getHeight();
            if (sourceAspectRatio > targetAspectRatio){
                sourceRect.top = 0;
                sourceRect.bottom = src.getHeight();
                int snip = (int) ((sourceWidth - (targetAspectRatio * sourceHeight)) / 2f);
                sourceRect.left = snip;
                sourceRect.right = src.getWidth() - snip;
            } else {
                sourceRect.left = 0;
                sourceRect.right = (int) sourceWidth;
                int snip = (int) ((sourceHeight - (sourceWidth / targetAspectRatio)) / 2f);
                sourceRect.top = snip;
                sourceRect.bottom = src.getHeight()-snip;
            }
            paintRect.left = 0;
            paintRect.top = 0;
            paintRect.right = (int) fillWidth;
            paintRect.bottom = (int) fillHeight;
            c.drawBitmap(src, sourceRect, paintRect, bmpPaint);
//            c.drawBitmap(src, 0, 0, bmpPaint);
            return ret;
        } catch (OutOfMemoryError error){
            Log.e("Aloqa","[getFillBitmap] Caught an OutOfMemory-error!");
            return null;
        }
	}

	/**
	 * Shrinks the given uvCoords by one pixel. This is valuable to avoid "bleeding"
	 * of one texture into another.
	 * @return	The given rect 'uvCoords'
	 */
	public static RectF inlayOnePixel(Texture t, RectF uvCoords) {
		uvCoords.inset(1f / (float)t.getWidth(), 1f / (float)t.getHeight());
		
		return uvCoords;
	}
}
