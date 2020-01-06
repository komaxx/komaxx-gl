package com.komaxx.komaxx_gl;

import java.util.ArrayDeque;
import java.util.Arrays;

import com.komaxx.komaxx_gl.math.MatrixUtil;

/**
 * Provides a simple way to store, update and restore matrices when traversing 
 * the SceneGraph.
 * 
 * @author Matthias Schicker
 */
public final class MatrixStack {
	private static final byte MAX_MATRIX_POOL_SIZE = 24;
	
	private final float[] basicMatrix;
	
	/**
	 * Stores matrices to avoid re-initialization and garbage collection.
	 */
	private ArrayDeque<float[]> matrixPool = new ArrayDeque<float[]>();
	
	private ArrayDeque<float[]> stack = new ArrayDeque<float[]>();

	private int matrixSize = 0;
	
	/**
	 * Initializes the MatrixStack with the identity matrix.
	 */
	public MatrixStack(){
		matrixSize = MatrixUtil.MATRIX_SIZE;
		this.basicMatrix = MatrixUtil.buildMatrix();
		reset();
	}
	
	public MatrixStack(int elementSize){
		matrixSize = elementSize;
		this.basicMatrix = new float[elementSize];
	}
	
	/**
	 * Initializes the matrix stack with a basic matrix.
	 * @param basicMatrix	The new basic matrix. The stack will be initialized and
	 * reseted to this matrix. It itself will never be modified.
	 */
	public MatrixStack(float[] basicMatrix){
		matrixSize = basicMatrix.length;
		this.basicMatrix = Arrays.copyOf(basicMatrix, matrixSize);
	}
	
	/**
	 * Removes all elements from the stack an re-initializes the stack with
	 * the basic Matrix.
	 */
	public final void reset(){
		int l = Math.min(stack.size(), MAX_MATRIX_POOL_SIZE - matrixPool.size());
		for (int i = 0; i < l; i++) matrixPool.add(stack.pop());
		stack.clear();
		float[] baseMatrixCopy = getPoolMatrix();
		System.arraycopy(basicMatrix, 0, baseMatrixCopy, 0, matrixSize);
		stack.push(baseMatrixCopy);
	}

	private final float[] getPoolMatrix() {
		if (matrixPool.isEmpty()) return new float[matrixSize];
		return matrixPool.pop();
	}
	
	private final void recycleMatrix(float[] matrix){
		if (matrixPool.size() < MAX_MATRIX_POOL_SIZE) matrixPool.push(matrix);
	}

	/**
	 * Duplicates the matrix on top, pushes it on the stack and returns it. 
	 * 
	 * This returned matrix should only be used
	 * temporarily and not stored, it might be re-used at some other point, which might
	 * yield undefined results. 
	 * 
	 * @return	a copy of the top matrix (which is already added to the stack) 
	 */
	public final float[] push(){
		float[] src = stack.peek();
		
		float[] ret = getPoolMatrix();
		System.arraycopy(src, 0, ret, 0, matrixSize);
		stack.push(ret);
		
		return ret;
	}
	
	/**
	 * Removed the top-element from the stack and returns it.
	 * @return	The (now removed) top matrix on the stack
	 */
	public final float[] pop(){
		float[] ret = stack.pop();
		recycleMatrix(ret);
		return ret;
	}

	/**
	 * Delivers the topmost matrix on the stack without removing it
	 * @return	the topmost matrix on the stack.
	 */
	public final float[] peek() {
		return stack.peek();
	}
	
	@Override
	public final String toString() {
		return "MatrixStack ("+matrixSize+"), current: " + Arrays.toString(peek());
	}
}
