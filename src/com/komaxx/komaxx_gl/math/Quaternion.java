package com.komaxx.komaxx_gl.math;


import android.util.FloatMath;

/**
 * Provides static calls to create and manipulate Quaternions.
 * Quaternions are to represented simply as float[4] arrays. This design decision
 * allows for simpler storage and transport tailored to OpenGL.
 * 
 * <b>NOTE</b> This class uses static temporary fields and is *not* thread safe!
 * 
 * @author Matthias Schicker
 */
public class Quaternion {
	private static final float NORMALIZE_TOLAERANCE = 0.000001f;
	
	private static float[] tmpVector3 = new float[3];
	
	public static final int W = 0;
	public static final int X = 1;
	public static final int Y = 2;
	public static final int Z = 3;
	
	/**
	 * Quaternions are to be represented simply as float[4] arrays.
	 */
	private Quaternion(){}
	
	/**
	 * Sets a quaternion according to the given axis/angle parameters
	 * and , for convenience, returns it.
	 */
	public static float[] setFromAxisAngle(float[] q,
			float angle, float axisX, float axisY, float axisZ){
	
		float sinAngle;
		angle *= 0.5f;

		tmpVector3[0] = axisX;
		tmpVector3[1] = axisY;
		tmpVector3[2] = axisZ;

		Vector.normalize3(tmpVector3);

		sinAngle = (float) Math.sin(angle);

		q[X] = (tmpVector3[0] * sinAngle);
		q[Y] = (tmpVector3[1] * sinAngle);
		q[Z] = (tmpVector3[2] * sinAngle);
		q[W] = (float) Math.cos(angle);

		return q;
	}
	
	/**
	 * Converts the given Quaternion to a rotation matrix.</br>
	 * <b>NOTE</b> The quaternion <b>MUST</b> be normalized, otherwise
	 * the result is gibberish.
	 */
	public static void toRotMatrix(float[] result, float[] q){
		float x2 = q[X] * q[X];
		float y2 = q[Y] * q[Y];
		float z2 = q[Z] * q[Z];
		float xy = q[X] * q[Y];
		float xz = q[X] * q[Z];
		float yz = q[Y] * q[Z];
		float wx = q[W] * q[X];
		float wy = q[W] * q[Y];
		float wz = q[W] * q[Z];
		
		result[0] = 1f - 2f * (y2 + z2);
		result[1] = 2f * (xy - wz);
		result[2] = 2f * (xz + wy);
		result[3] = 0f;
		
		result[4] = 2f * (xy + wz);
		result[5] = 1f - 2f * (x2 + z2);
		result[6] = 2f * (yz - wx);
		result[7] = 0f;

		result[8] = 2f * (xz - wy);
		result[9] = 2f * (yz + wx);
		result[10] = 1f - 2f * (x2 + y2);
		result[11] = 0f;

		result[12] = 0f;
		result[13] = 0f;
		result[14] = 0f;
		result[15] = 1f;
	}
	
	/**
	 * Conjugates the quaternion in place and, as a convenience, returns it.
	 * Think of it as reversal of the vector part of the quaternion.
	 */
	public static float[] conjugate(float[] q){
		q[X] = -q[X];
		q[Y] = -q[Y];
		q[Z] = -q[Z];
		return q;
	}
	
	public static float[] normalize(float[] q){
		// Don't normalize if we don't have to
		float mag2 = q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3];
		if (Math.abs(mag2) > NORMALIZE_TOLAERANCE && Math.abs(mag2 - 1.0f) > NORMALIZE_TOLAERANCE) {
			float mag = FloatMath.sqrt(mag2);
			q[0] /= mag;
			q[1] /= mag;
			q[2] /= mag;
			q[3] /= mag;
		}
		
		return q;
	}
}
