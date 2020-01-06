package com.komaxx.komaxx_gl;

import java.util.ArrayDeque;

import android.graphics.Rect;

/**
 * Provides a simple way to store, update and restore matrices when traversing 
 * the SceneGraph.
 * 
 * @author Matthias Schicker
 */
public final class RectStack {
	private static final byte MAX_POOL_SIZE = 25;
	
	/**
	 * Stores rects to avoid re-initialization and garbage collection.
	 */
	private ArrayDeque<Rect> rectPool = new ArrayDeque<Rect>();
	
	private ArrayDeque<Rect> stack = new ArrayDeque<Rect>();

	
	/**
	 * Initializes the MatrixStack with the identity matrix.
	 */
	public RectStack(){
		reset(0,0,0,0);
	}
	
	/**
	 * Removes all elements from the stack an re-initializes the stack with
	 * the defined rect.
	 */
	public final void reset(int left, int top, int width, int height){
		int l = Math.min(stack.size(), MAX_POOL_SIZE - rectPool.size());
		for (int i = 0; i < l; i++) rectPool.add(stack.pop());
		stack.clear();
		
		Rect firstRect = getPoolRect();
		firstRect.set(left, top, left+width, top+height);
		
		stack.push(firstRect);
	}

	private final Rect getPoolRect() {
		if (rectPool.isEmpty()) return new Rect();
		return rectPool.pop();
	}
	
	private final void recycleRect(Rect toRecycle){
		if (rectPool.size() < MAX_POOL_SIZE) rectPool.push(toRecycle);
	}

	/**
	 * Duplicates the rect on top, pushes it on the stack and returns it. 
	 * 
	 * This returned matrix should only be used
	 * temporarily and not stored, it might be re-used at some other point, which might
	 * yield undefined results. 
	 * 
	 * @return	a copy of the top matrix (which is already added to the stack) 
	 */
	public final Rect push(){
		Rect src = stack.peek();
		
		Rect ret = getPoolRect();
		ret.set(src);

		stack.push(ret);
		return ret;
	}
	
	/**
	 * Removes the top-element from the stack and returns it.
	 * @return	The (now removed) top rect on the stack. This Rect will be recycled! Do not store.
	 */
	public final Rect pop(){
		Rect ret = stack.pop();
		recycleRect(ret);
		return ret;
	}

	/**
	 * Delivers the topmost matrix on the stack without removing it
	 * @return	the topmost matrix on the stack.
	 */
	public final Rect peek() {
		return stack.peek();
	}
	
	@Override
	public final String toString() {
		return "RectStack ("+stack.size()+"), current: " + stack.peek();
	}
}
