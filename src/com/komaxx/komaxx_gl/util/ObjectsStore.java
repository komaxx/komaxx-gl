package com.komaxx.komaxx_gl.util;

import java.util.ArrayDeque;

import com.komaxx.komaxx_gl.math.MatrixUtil;
import com.komaxx.komaxx_gl.math.Vector;

import android.opengl.Matrix;

/**
 * This class holds often used objects in 3D graphics, such as Matrices
 * and Vectors.
 * 
 * @author Matthias Schicker
 */
public class ObjectsStore {
	private static final int MAX_SHARED_MATRICES = 60;
	private static final int MAX_SHARED_VECTORS = 50;
	
	/**
	 * A temporary matrix for short time use (one method) in the GL thread only.
	 * Should be set to identity before usage!
	 */
	public static float[] tmpMatrix1 = MatrixUtil.buildMatrix();
	/**
	 * A temporary matrix for short time use (one method) in the GL thread only.
	 * Should be set to identity before usage!
	 */
	public static float[] tmpMatrix2 = MatrixUtil.buildMatrix();

	/**
	 * A temporary vector for short time use (one method) in the GL thread only.
	 */
	public static float[] tmpVector1 = new float[4];
	/**
	 * A temporary vector for short time use (one method) in the GL thread only.
	 */
	public static float[] tmpVector2 = new float[4];
	
	/**
	 * A array of ints; useful for generating handles in the GL
	 */
	public static int[] tmpIntBuffer = new int[5];
	
	private static ArrayDeque<float[]> matrixPool = new ArrayDeque<float[]>();
	private static ArrayDeque<float[]> vectorPool = new ArrayDeque<float[]>();
	
	// //////////////////////////////
	// monitoring
	private static int nowNonRecycledMatrices = 0;
	
	/**
	 * Fetch a shared matrix. Recycle the matrix when you are done!
	 * <b>NOTE:</b> The matrix will be set to identity before handing it out.
	 */
	public static float[] getMatrix(){
		nowNonRecycledMatrices++;
		
		if (nowNonRecycledMatrices == MAX_SHARED_MATRICES){
			KoLog.w("ObjectStore", 
					"There were more matrices requested than can be buffered. Forgot to recycle something?");
		}
		
		if (matrixPool.isEmpty()){
			return MatrixUtil.buildMatrix();
		} 
		float[] ret = matrixPool.pop();
		Matrix.setIdentityM(ret, 0);
		return ret;
	}
	
	/**
	 * Similar to getMatrix, but already presets the returned matrix
	 * with the given matrix. This is cheaper than getMatrix and setting
	 * it afterwards. </br>
	 * You still have to recycle it, though!
	 */
	public static float[] getCloneMatrix(float[] matrix){
		nowNonRecycledMatrices++;
		
		if (nowNonRecycledMatrices == MAX_SHARED_MATRICES){
			KoLog.w("ObjectStore", 
					"There were more matrices requested than can be buffered. Forgot to recycle something?");
		}
		
		if (matrixPool.isEmpty()){
			return MatrixUtil.buildCloneMatrix(matrix);
		} 
		float[] ret = matrixPool.pop();
		MatrixUtil.setMatrix(ret, matrix);
		return ret;
	}

	/**
	 * Recycle a shared matrix.
	 */
	public static void recycleMatrix(float[] toRecycle){
		if (matrixPool.size() < MAX_SHARED_MATRICES){
			matrixPool.push(toRecycle);
		}
		nowNonRecycledMatrices--;
	}
	
	/**
	 * Fetch a shared vector of length 4. Recycle the vector when you are done!
	 * <b>NOTE:</b> The vector will be set to (0,0,0,1) before handing it out.
	 */
	public static float[] getVector(){
		float[] ret;
		if (vectorPool.isEmpty()){
			ret = new float[4];
		} else {
			ret = vectorPool.pop();
		}
		
		Vector.set4(ret, 0, 0, 0, 1);
		
		return ret;
	}

	/**
	 * Recycle a shared matrix.
	 */
	public static void recycleVector(float[] toRecycle){
		if (vectorPool.size() < MAX_SHARED_VECTORS){
			vectorPool.push(toRecycle);
		} else {
			KoLog.w("ObjectsStore", "The shared vector stack contains more than its max size. Investigate!");
		}
	}
}
