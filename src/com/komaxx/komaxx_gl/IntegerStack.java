package com.komaxx.komaxx_gl;

import java.util.ArrayDeque;

/**
 * Provides a simple way to store, update and restore integers when traversing 
 * the SceneGraph.
 * 
 * @author Matthias Schicker
 */
public final class IntegerStack {
	private static final byte MAX_INT_POOL_SIZE = 30;
	
	private final int basicValue;
	
	/**
	 * Stores Booleans to avoid re-initialization and garbage collection.
	 */
	private ArrayDeque<Int> intPool = new ArrayDeque<Int>();
	
	private ArrayDeque<Int> stack = new ArrayDeque<Int>();

	/**
	 * Initializes the BooleanStack with the identity matrix.
	 */
	public IntegerStack(int resetValue){
		basicValue = resetValue;
		reset();
	}
	
	/**
	 * Removes all elements from the stack an re-initializes the stack with
	 * the basic value.
	 */
	public void reset(){
		while (!stack.isEmpty()) recycleInt(stack.pop());
		stack.push(getPoolInt(basicValue));
	}

	/**
	 * Duplicates the Bool on top, pushes it on the stack and returns it. 
	 * 
	 * @return	a copy of the top boolean (which is already added to the stack) 
	 */
	public Int push(){
		Int ret = getPoolInt(stack.peek());
		stack.push(ret);
		return ret;
	}
	
	private Int getPoolInt(int i) {
		if (intPool.isEmpty()) return new Int(i);
		Int ret = intPool.pop();
		ret.i = i;
		return ret;
	}
	
	private Int getPoolInt(Int src) {
		if (intPool.isEmpty()) return new Int(src);
		Int ret = intPool.pop();
		ret.i = src.i;
		return ret;
	}
	
	private void recycleInt(Int i){
		if (intPool.size() < MAX_INT_POOL_SIZE) intPool.push(i);
	}

	/**
	 * Removed the top-element from the stack and returns it.
	 * @return	The (now removed) top int on the stack
	 */
	public Int pop(){
		Int ret = stack.pop();
		recycleInt(ret);
		return ret;
	}

	/**
	 * Delivers the topmost Int on the stack without removing it
	 * @return	the topmost Int on the stack.
	 */
	public Int peek() {
		return stack.peek();
	}
	
	@Override
	public String toString() {
		return "IntStack, current: " + peek();
	}
	
	public final static class Int {
		public int i = 0;
		public Int(Int src) { i = src.i; }
		public Int(int i2) { i = i2; }
		public void increment() { i++; }
		public void add(int i2) { i+=i2; }
		@Override
		public String toString() { return Integer.toString(i); }
	}
}
