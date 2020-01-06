package com.komaxx.komaxx_gl.math;



/**
 * Provides static procedures to manipulate vectors defined as float[3].
 * <b>NOT thread safe</b>
 * 
 * @author Matthias Schicker
 */
public class Vector {
	private static float[] distanceTmp = new float[3];
	
	private Vector(){}
	
	public static float[] aMinusB2(float[] vA, float[] vB){
    	vA[0] -= vB[0];
    	vA[1] -= vB[1];
    	return vA;
    }
	
    public static float[] aMinusB3(float[] vA, float[] vB){
    	vA[0] -= vB[0];
    	vA[1] -= vB[1];
    	vA[2] -= vB[2];
    	return vA;
    }
    
    public static float[] aMinusB3(float[] result, float[] a, float[] b){
    	result[0] = a[0] - b[0];
    	result[1] = a[1] - b[1];
    	result[2] = a[2] - b[2];
    	return result;
    }
    
    public static float[] aMinusB2(float[] result, float[] a, float[] b){
    	result[0] = a[0] - b[0];
    	result[1] = a[1] - b[1];
    	return result;
    }
    
    public static float[] aMinusB3(float[] result, float[] a, int aOffset, float[] b){
    	result[0] = a[aOffset] - b[0];
    	result[1] = a[aOffset + 1] - b[1];
    	result[2] = a[aOffset + 2] - b[2];
    	return result;
    }
    
    /**
     * Makes the vector have length 1
     */
    public static float[] normalize3(float[] v){
    	float vLength = length3(v);
    	v[0] /= vLength;
    	v[1] /= vLength;
    	v[2] /= vLength;
    	return v;
    }

    /**
     * Makes the vector have length 1
     */
    public static float[] normalize2(float[] v){
    	float vLength = length2(v);
    	v[0] /= vLength;
    	v[1] /= vLength;
    	return v;
    }

    
    /**
     * Makes the w value 1
     */
    public static float[] normalize4(float[] v){
    	float factor = 1f / v[3];
    	v[0] *= factor;
    	v[1] *= factor;
    	v[2] *= factor;
    	v[3] *= factor;
    	return v;
    }
    
    public static float[] scalarMultiply2(float[] v, float scalar){
    	v[0] *= scalar;
    	v[1] *= scalar;
    	return v;
    }

    public static float[] scalarMultiply3(float[] v, float scalar){
    	v[0] *= scalar;
    	v[1] *= scalar;
    	v[2] *= scalar;
    	return v;
    }
    
    public static float[] addBtoA3(float[] a, float[] b){
    	a[0] += b[0];
    	a[1] += b[1];
    	a[2] += b[2];
    	return a;
    }

    public static float[] addBtoA2(float[] a, float[] b){
    	a[0] += b[0];
    	a[1] += b[1];
    	return a;
    }
    
    public static float[] aPlusB3(float[] result, float[] a, float[] b){
    	result[0] = a[0] + b[0];
    	result[1] = a[1] + b[1];
    	result[2] = a[2] + b[2];
    	return result;
    }

    public static float[] aPlusB2(float[] result, float[] a, float[] b){
    	result[0] = a[0] + b[0];
    	result[1] = a[1] + b[1];
    	return result;
    }

	public static void average3(float[] result, float[] a, int aOffset, float[] b, int bOffset) {
		result[0] = (a[aOffset] + b[bOffset]) / 2;
    	result[1] = (a[aOffset + 1] + b[bOffset + 1]) / 2;
    	result[2] = (a[aOffset + 2] + b[bOffset + 2]) / 2;
	}
    
	public static float[] average3(float[] result, float[] a, float[] b) {
		result[0] = (a[0] + b[0]) / 2;
    	result[1] = (a[1] + b[1]) / 2;
    	result[2] = (a[2] + b[2]) / 2;
    	return result;
	}
	
	public static float[] average2(float[] result, float[] a, float[] b, float[] c) {
		result[0] = (a[0] + b[0] + c[0]) / 3f;
    	result[1] = (a[1] + b[1] + c[1]) / 3f;
    	return result;
	}

	public static float[] aToB3(float[] result, float[] a, float[] b){
		return aMinusB3(result, b, a);
	}
	
	public static float[] aToB3(float[] result, float[] a, float[] b, int bOffset) {
		return aMinusB3(result, b, bOffset, a);
	}

	public static float[] aToB2(float[] result, float[] a, float[] b) {
		if (result == null) result = new float[2];
		return aMinusB2(result, b, a);
	}
	
	public static float length3(float[] v){
		return (float) Math.sqrt( v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
	}
	
	public static float length2(float[] v){
		return (float) Math.sqrt( v[0]*v[0] + v[1]*v[1] );
	}
	
	public static float sqrLength2(float[] v){
		return v[0]*v[0] + v[1]*v[1];
	}
	
	public static float distance3(float[] a, float[] b) {
		return length3(aMinusB3(distanceTmp, a, b));
	}

	public static float dotProduct2(float[] v1, float[] v2){
		return v1[0]*v2[0] + v1[1]*v2[1];
	}
	
	public static float distance2(float[] a, float[] b) {
		return length2(aMinusB2(distanceTmp, a, b));
	}
	
	/**
	 * Delivers the square of the distance between the two vectors
	 */
	public static float sqrDistance2(float[] a, float[] b) {
		return sqrLength2(aMinusB2(distanceTmp, a, b));
	}

	public static String toString(float[] v) {
		StringBuffer ret = new StringBuffer();
		ret.append('(').append(v[0]);
		for (int i = 1; i < v.length; i++){
			ret.append(" | ").append(v[i]);
		}
		ret.append(')');
		return ret.toString();
	}
	
	public static String toString(float[] v, int digits) {
		float digitFactor = 1;
		for (int i = 0; i < digits; i++) digitFactor *= 10;
		StringBuffer ret = new StringBuffer();
		ret.append('(').append( (int)(v[0]*digitFactor) / digitFactor );
		for (int i = 1; i < v.length; i++){
			ret.append(" | ").append( (int)(v[i]*digitFactor) / digitFactor );
		}
		ret.append(')');
		return ret.toString();
	}

	public static float[] invert2(float[] v) {
		v[0] = -v[0];
		v[1] = -v[1];
		return v;
	}
	
	public static float[] invert3(float[] v) {
		v[0] = -v[0];
		v[1] = -v[1];
		v[2] = -v[2];
		return v;
	}
	
	public static float[] invert4(float[] v) {
		v[0] = -v[0];
		v[1] = -v[1];
		v[2] = -v[2];
		v[3] = -v[3];
		return v;
	}

	public static float[] set3(float[] v, float[] nuValues) {
		v[0] = nuValues[0];
		v[1] = nuValues[1];
		v[2] = nuValues[2];
		return v;
	}

	public static float[] set2(float[] v, float[] nuValues) {
		v[0] = nuValues[0];
		v[1] = nuValues[1];
		return v;
	}

	public static float[] set2(float[] v, float x, float y) {
		v[0] = x;
		v[1] = y;
		return v;
	}
	
	public static void minus3(float[] v) {
		v[0] = -v[0];
		v[1] = -v[1];
		v[2] = -v[2];
	}

	public static void set3(float[] v, float[] src, int srcOffset) {
		v[0] = src[srcOffset];
		v[1] = src[srcOffset + 1];
		v[2] = src[srcOffset + 2];
	}

	public static void set3(float[] v, float x, float y, float z) {
		v[0] = x;
		v[1] = y;
		v[2] = z;
	}
	
	public static void set4(float[] v, float x, float y, float z, float w) {
		v[0] = x;
		v[1] = y;
		v[2] = z;
		v[3] = w;
	}
	
	public static void set4(float[] v, int dstOffset, float[] src, int srcOffset) {
		v[dstOffset] = src[srcOffset];
		v[dstOffset + 1] = src[srcOffset + 1];
		v[dstOffset + 2] = src[srcOffset + 2];
		v[dstOffset + 3] = src[srcOffset + 3];
	}

	public static void set4(float[] v, float[] src) {
		v[0] = src[0];
		v[1] = src[1];
		v[2] = src[2];
		v[3] = src[3];
	}

	
	public static void moveY(float[] vectors, int offset, float yMovement) {
		vectors[offset + 1] += yMovement;
	}

	public static void moveXY(float[] vectors, int offset, float x, float y) {
		vectors[offset] += x;
		vectors[offset + 1] += y;
	}

	public static float[] normal2(float[] result, float[] v) {
		result[0] = -v[1];
		result[1] = v[0];
		return result;
	}

	public static boolean isNormalized2(float[] v) {
		return v[0] <= 1 && v[0] >= -1 && v[1] <= 1 && v[1] >= -1 &&	// quick fail checks
			   (v[0]*v[0] + v[1]*v[1] == 1);		// TODO: Use a more robust equal check that considers float imprecisions!!
	}
}
