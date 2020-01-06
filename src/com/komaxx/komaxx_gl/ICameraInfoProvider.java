package com.komaxx.komaxx_gl;

/**
 * Important interface to process interaction.
 * 
 * @author Matthias Schicker
 */
public interface ICameraInfoProvider {
	/**
	 * Translates a point on the screen to world coordinates on the z==0 plane.
	 * 
	 * @param result	the result in world coordinates ([x,y,0]). 
	 * <b>MUST NOT</b> be the inputvector pixelXY.
	 * @param pixelXY	the point on the screen.
	 */
	void pixel2ZeroPlaneCoord(float[] result, float[] pixelXY);
}
