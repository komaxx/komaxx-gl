package com.komaxx.komaxx_gl.util;

import com.komaxx.komaxx_gl.util.StepInterpolator.Step;

public class AnimatedValue {
	public static final long ANIMATION_DURATION_QUICK =  100000000;
	public static final long ANIMATION_DURATION_NORMAL = 200000000;
	public static final long ANIMATION_DURATION_SLOW =   500000000;
	
	private long animationDuration;
	private Step[] steps = StepInterpolator.FLAT_STEPS;
	
	private final float startValue;
	private float currentValue = 0;
	private float scale = 0;
	private Interpolators.Interpolator interpolator;
	
	private boolean running = false;

	
	/**
	 * Creates an AnimatedValue with a normal duration (ANIMATION_DURATION_NORMAL).
	 */
	public AnimatedValue(Step[] steps, float startValue, float scale){
		this(steps, startValue, scale, ANIMATION_DURATION_NORMAL);
	}
	
	public AnimatedValue(Step[] steps, float startValue, float scale, long animationDuration){
		this.steps = steps;
		this.startValue = startValue;
		this.scale = scale;
		this.animationDuration = animationDuration;
		currentValue = startValue;
	}
	
	public void setScale(float nuTarget){
		scale = nuTarget;
	}
	
	/**
	 * The StepInterpolator contains already a set of adequate step-profiles.
	 */
	public void setSteps(Step[] steps) {
		this.steps = steps;
	}
	
	public void reset(){
		interpolator = null;
		currentValue = startValue;
		running = false;
	}
	
	/**
	 * Starts the Animation. Calling again when currently running does nothing.
	 */
	public void start(){
		running = true;
	}
	
	public float get(long time){
		if (!running) return currentValue;
		
		if (interpolator == null){
			buildInterpolator(time);
		}

		currentValue = interpolator.getValue(time);

		if (interpolator != null && time > interpolator.getEndX()){
			running = false;
			interpolator = null;
		}
		
		return currentValue;
	}
	
	private void buildInterpolator(long time) {
		interpolator = new StepInterpolator(steps, time, time + animationDuration, currentValue, scale);
	}

	public float getLast(){
		return currentValue;
	}
	
	public void shortcut(){
		if (interpolator != null){
			currentValue = interpolator.getEndY();
			interpolator = null;
			running = false;
		}
	}

	public float getScale() {
		return scale;
	}

	/**
	 * Modifies the speed of the animation. Will only take effect with
	 * the next start call.
	 */
	public void setDuration(long nuDuration) {
		animationDuration = nuDuration;
	}

	public boolean isRunning() {
		return running;
	}
}
