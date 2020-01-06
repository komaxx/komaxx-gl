package com.komaxx.komaxx_gl;

import java.util.ArrayDeque;

/**
 * Provides a simple way to store, update and restore booleans when traversing 
 * the SceneGraph.
 * 
 * @author Matthias Schicker
 */
public final class BooleanStack {
	private static final byte MAX_BOOL_POOL_SIZE = 30;
	
	private final boolean basicValue;
	
	/**
	 * Stores Booleans to avoid re-initialization and garbage collection.
	 */
	private ArrayDeque<Bool> boolPool = new ArrayDeque<Bool>();
	
	private ArrayDeque<Bool> stack = new ArrayDeque<Bool>();

	/**
	 * Initializes the BooleanStack with the identity matrix.
	 */
	public BooleanStack(boolean resetValue){
		basicValue = resetValue;
		reset();
	}
	
	/**
	 * Removes all elements from the stack an re-initializes the stack with
	 * the basic value.
	 */
	public void reset(){
		while (!stack.isEmpty()) recycleBool(stack.pop());
		stack.push(getPoolBool(basicValue));
	}

	/**
	 * Duplicates the Bool on top, pushes it on the stack and returns it. 
	 * 
	 * @return	a copy of the top boolean (which is already added to the stack) 
	 */
	public Bool push(){
		Bool ret = getPoolBool(stack.peek());
		stack.push(ret);
		return ret;
	}
	
	private Bool getPoolBool(boolean b) {
		if (boolPool.isEmpty()) return new Bool(b);
		Bool ret = boolPool.pop();
		ret.b = b;
		return ret;
	}
	
	private Bool getPoolBool(Bool src) {
		if (boolPool.isEmpty()) return new Bool(src);
		Bool ret = boolPool.pop();
		ret.b = src.b;
		return ret;
	}
	
	private void recycleBool(Bool b){
		if (boolPool.size() < MAX_BOOL_POOL_SIZE) boolPool.push(b);
	}

	/**
	 * Removed the top-element from the stack and returns it.
	 * @return	The (now removed) top int on the stack
	 */
	public Bool pop(){
		Bool ret = stack.pop();
		recycleBool(ret);
		return ret;
	}

	/**
	 * Delivers the topmost Int on the stack without removing it
	 * @return	the topmost Int on the stack.
	 */
	public Bool peek() {
		return stack.peek();
	}
	
	@Override
	public String toString() {
		return "BooleanStack, current: " + peek();
	}
	
	public final static class Bool {
		public boolean b = false;
		public Bool(Bool src) { b = src.b; }
		public Bool(boolean b2) { b = b2; }
		@Override
		public String toString() { return Boolean.toString(b); }
	}
}
