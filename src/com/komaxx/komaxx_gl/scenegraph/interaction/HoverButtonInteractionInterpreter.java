package com.komaxx.komaxx_gl.scenegraph.interaction;

import com.komaxx.komaxx_gl.scenegraph.interaction.ButtonInteractionInterpreter.IButtonElement;

/**
 * Handles interaction events and interprets them similar to how
 * a button would interact. However, does not bind the interaction
 * to the element.
 * 
 * @author Matthias Schicker
 */
public class HoverButtonInteractionInterpreter {
	private final IButtonElement element;

	private boolean longClickAborts = false;

	private boolean longClicked = false;
	
	private int boundPointerIndex = -1;
	
	public HoverButtonInteractionInterpreter(IButtonElement element){
		this.element = element;
	}
	
	public boolean onInteraction(InteractionContext ic){
		if (boundPointerIndex == -1 && ic.getAction() != Pointer.DOWN){
			return false;
		}
		
		float[] rayPoint = null;
		if (boundPointerIndex == -1){		// obviously: action is DOWN
			Pointer pointer = ic.getPointers()[ic.getActionIndex()];
			pointer.moveRaypointsToZ0plane(ic);
			rayPoint = pointer.getRayPoint();
			if (element.inBounds(rayPoint)){
				longClicked = false;
				boundPointerIndex = ic.getActionIndex();
				element.down(ic);
			}
		} else {
			Pointer pointer = ic.getPointers()[boundPointerIndex];
			int action = pointer.getAction();
			pointer.moveRaypointsToZ0plane(ic);
			if (action == Pointer.MOVE && (!element.inBounds(rayPoint) || pointer.tapRangeLeft())){
				abort(ic);
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
		longClicked = false;
	}
	
	/**
	 * If set, the interpreter will not trigger a "clicked" action when the element
	 * is no longer pressed, but "cancel". However, the interaction will not be canceled
	 * immediately when the long click happens.
	 */
	public void setLongClickAborts(boolean longClickAborts) {
		this.longClickAborts = longClickAborts;
		
		throw new RuntimeException("not implemented yet");
	}
}
