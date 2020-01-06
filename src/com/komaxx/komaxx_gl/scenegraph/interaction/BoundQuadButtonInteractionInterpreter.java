package com.komaxx.komaxx_gl.scenegraph.interaction;

import com.komaxx.komaxx_gl.bound_meshes.BoundTexturedQuad;
import com.komaxx.komaxx_gl.scenegraph.interaction.ButtonInteractionInterpreter.IButtonElement;

/**
 * Handles interaction events and interprets them as fit for
 * a button. Visualizes interaction per alpha.
 * 
 * @author Matthias Schicker
 */
public class BoundQuadButtonInteractionInterpreter implements IButtonElement {
	private final ButtonInteractionInterpreter interpreter;
	private final IButtonElement element;
	private final BoundTexturedQuad quad;

	private float disabledAlpha = 0.6f;
	private float normalAlpha = 1f;
	private float downAlpha = 1.2f;
	private float clickAlpha = 1.6f;
	
	private boolean enabled = true;
	
	
	public BoundQuadButtonInteractionInterpreter(IButtonElement element, BoundTexturedQuad quad){
		this.element = element;
		this.quad = quad;
		interpreter = new ButtonInteractionInterpreter(this);
	}

	public boolean onInteraction(InteractionContext ic){
		return interpreter.onInteraction(ic);
	}
	
	@Override
	public boolean inBounds(float[] xy) {
		return enabled && quad.contains(xy[0], xy[1]);
	}

	@Override
	public void down(InteractionContext ic) {
		quad.setAlpha(downAlpha);
		element.down(ic);
	}

	@Override
	public void cancel(InteractionContext ic) {
		quad.setAlpha(normalAlpha);
		element.cancel(ic);
	}

	@Override
	public void click(InteractionContext ic) {
		quad.setAlphaDirect(clickAlpha);
		element.click(ic);
		quad.setAlpha(normalAlpha);
	}

	public void enable(boolean b) {
		this.enabled = b;
		quad.setAlpha(enabled ? normalAlpha : disabledAlpha);
	}
	
	public void setNormalAlpha(float normalAlpha) {
		this.normalAlpha = normalAlpha;
	}
	
	public void setDisabledAlpha(float disabledAlpha) {
		this.disabledAlpha = disabledAlpha;
	}
}
