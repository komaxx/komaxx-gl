package com.komaxx.komaxx_gl.math;

import android.opengl.Matrix;


/**
 * Contains a set of useful static functions, which are missing in
 * Android's Matrix calss, to create and manipulate matrices as needed in 
 * this Gl Lib.
 * 
 * @author Matthias Schicker
 */
public class MatrixUtil {
	public static final byte MATRIX_SIZE = 16;
	
	private MatrixUtil(){}
	
	public static float[] buildMatrix(){
		float[] ret = new float[MATRIX_SIZE];
		android.opengl.Matrix.setIdentityM(ret, 0);
		return ret;
	}
	
	public static float[] buildCloneMatrix(float[] matrix){
		float[] ret = new float[MATRIX_SIZE];
		setMatrix(ret, matrix);
		return ret;
	}
	
	public static float[] setMatrix(float[] result, float[] setSource) {
		System.arraycopy(setSource, 0, result, 0, MATRIX_SIZE);
		return result;
	}

	/**
	 * Basically the same as Matrix.multiply but with no extra matrix allocation
	 * necessary.
	 */
	private static float[] tmpMultiplyMatrix = buildMatrix();
	public static void multiplyOnto(float[] target, float[] rhs) {
		setMatrix(tmpMultiplyMatrix, target);
		Matrix.multiplyMM(target, 0, tmpMultiplyMatrix, 0, rhs, 0);
	}
}
