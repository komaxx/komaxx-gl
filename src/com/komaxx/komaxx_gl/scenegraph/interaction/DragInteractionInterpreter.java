package com.komaxx.komaxx_gl.scenegraph.interaction;

import com.komaxx.komaxx_gl.math.Vector;
import com.komaxx.komaxx_gl.util.KoLog;


/**
 * Handles interaction events and interprets them as fit for
 * an object that can be dragged.
 * 
 * @author Matthias Schicker
 */
public class DragInteractionInterpreter {
	private final IDragElement element;

	private float[] offsetXY = new float[2];
	private int boundPointerIndex = -1;
	
	private static final float[] dragPoint = new float[2];
	
	public DragInteractionInterpreter(IDragElement element){
		this.element = element;
	}

	/**
	 * True, when this interpreter is currently bound to a pointer index. 
	 */
	public boolean isBound() {
		return boundPointerIndex!=-1;
	}
	
	/**
	 * A string which describes the state of the interpreter as of the current frame.
	 */
	public String stringState(InteractionContext ic) {
		if (boundPointerIndex < 0){
			return "(unbound) " + actionToString(ic.getAction());
		}

		Pointer pointer = ic.getPointers()[boundPointerIndex];
		int action = pointer.getAction();
		pointer.moveRaypointsToZ0plane(ic);

		return "(" + boundPointerIndex + ") " + actionToString(action) + Vector.toString(pointer.getRayPoint(), 1);
	}

	private static String actionToString(int action) {
		switch(action){
		case Pointer.CANCEL:
			return "CNC";
		case Pointer.DOWN:
			return "DWN";
		case Pointer.INACTIVE:
			return "INC";
		case Pointer.MOVE:
			return "MOV";
		case Pointer.UP:
			return "UP";
		}
		return "???";
	}

	public boolean onInteraction(InteractionContext ic){
		if (boundPointerIndex == -1 && ic.getAction() != Pointer.DOWN){
			return false;
		}
		
		float[] rayPoint = null;
		if (boundPointerIndex == -1){		// obviously: action is DOWN
			Pointer pointer = ic.getActionPointer();
			pointer.moveRaypointsToZ0plane(ic);
			rayPoint = pointer.getRayPoint();
			if (element.inBounds(rayPoint)){
				element.getOffset(offsetXY, rayPoint);
				boundPointerIndex = ic.getActionIndex();
				element.down(ic);
				return true;
			}
		} else {		// bound to a pointer
			Pointer pointer = ic.getPointers()[boundPointerIndex];
			int action = pointer.getAction();
			pointer.moveRaypointsToZ0plane(ic);
			if (action == Pointer.MOVE){
				Vector.set2(dragPoint, pointer.getRayPoint());
				Vector.addBtoA2(dragPoint, offsetXY);
				
				if (dragPoint[1] > 0){
					KoLog.w(this, "WTF!!");
				}
				
				element.drag(ic, pointer.getRayPoint());
			} else if (action == Pointer.UP){
				if (pointer.tapRangeLeft()){
					abort(ic);
				} else {		// yay, trigger
					element.click(ic);
					boundPointerIndex = -1;
					return true;
				}
			} else {		// some other freaky action
				abort(ic);
			}
		}
		
		return false;
	}
	
	private void abort(InteractionContext ic) {
		if (boundPointerIndex != -1) element.cancel(ic);
		boundPointerIndex = -1;
	}


	public static interface IDragElement {
		boolean inBounds(float[] xy);
		
		/**
		 * Called after successful inBounds check. Should deliver the offset between
		 * the point that's later used as anchor for dragging and the given rayPoint. 
		 */
		void getOffset(float[] offsetXY, float[] rayPoint);

		void down(InteractionContext ic);
		
		void cancel(InteractionContext ic);
		
		void drag(InteractionContext ic, float[] nuPos);
		
		void click(InteractionContext ic);
	}
	
	public static abstract class ADragElement implements IDragElement {
		@Override
		public void down(InteractionContext ic) {}
		@Override
		public void cancel(InteractionContext ic) {}
		@Override
		public void click(InteractionContext ic) {}
		@Override
		public void getOffset(float[] offsetXY, float[] rayPoint) {
			offsetXY[0] = 0;
			offsetXY[1] = 0;
		}
	}
}
