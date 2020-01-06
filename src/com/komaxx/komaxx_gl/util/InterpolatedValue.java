package com.komaxx.komaxx_gl.util;

import com.komaxx.komaxx_gl.util.StepInterpolator.Step;

public class InterpolatedValue {
	
	public static enum AnimationType {
		LINEAR, SQUARED, OVERBUMP, INVERSE_SQUARED, 
		/**
		 * Remember to also set a steps profile when using this type!
		 */
		STEPPED
	}

	public static final long ANIMATION_DURATION_QUICK =  100000000;
	public static final long ANIMATION_DURATION_NORMAL = 200000000;
	public static final long ANIMATION_DURATION_SLOW =   500000000;
	
	private final AnimationType animationType;
	private long animationDuration;
	/**
	 * Only used when using type STEPPED
	 */
	private Step[] steps = StepInterpolator.FLAT_STEPS;
	
	private float currentValue = 0;
	private float targetValue = 0;
	private Interpolators.Interpolator interpolator;
	
	/**
	 * Creates an AnimatedValue with a normal duration (ANIMATION_DURATION_NORMAL).
	 */
	public InterpolatedValue(AnimationType type, float startValue){
		this(type, startValue, ANIMATION_DURATION_NORMAL);
	}
	
	public InterpolatedValue(AnimationType type, float startValue, long animationDuration){
		this.animationType = type;
		this.animationDuration = animationDuration;
		currentValue = targetValue = startValue;
	}
	
	public void set(float nuTarget){
		targetValue = nuTarget;
	}
	
	/**
	 * The StepInterpolator contains already a set of adequate step-profiles.
	 * ONLY use step profiles that end with <code>1</code>. Undefined behavior otherwise.
	 */
	public void setSteps(Step[] steps) {
		this.steps = steps;
	}
	
	/**
	 * @param time  
	 */
	public boolean isDone(long time){
		return currentValue==targetValue;
	}
	
	public void setDirect(float nuValue){
		interpolator = null;
		currentValue = targetValue = nuValue;
	}
	
	public float get(long time){
		if (currentValue == targetValue){
			interpolator = null;
			return targetValue;
		}

		if (interpolator == null || interpolator.getEndY() != targetValue){
			buildInterpolator(time);
		}
		currentValue = interpolator.getValue(time);
		return currentValue;
	}
	
	private void buildInterpolator(long time) {
		switch (animationType){
		case LINEAR:
			interpolator = new Interpolators.LinearInterpolator(currentValue, targetValue, time, time + animationDuration);
			return;
		case OVERBUMP:
			interpolator = new Interpolators.OverBumpInterpolator(currentValue, targetValue, time, time + animationDuration);
			return;
		case SQUARED:
			interpolator = new Interpolators.PowerInterpolator(2, currentValue, targetValue, time, time + animationDuration);
			return;
		case INVERSE_SQUARED:
			interpolator = new Interpolators.InverseSquareInterpolator(currentValue, targetValue, time, time + animationDuration);
			return;
		case STEPPED:
			interpolator = new StepInterpolator(steps, time, time + animationDuration, currentValue, targetValue);
			return;
		}
		throw new RuntimeException("unknown animation type! Fix your code, dummy, ._.");
	}

	public float getLast(){
		return currentValue;
	}
	
	public void shortcut(){
		interpolator = null;
		currentValue = targetValue;
	}

	public float getTarget() {
		return targetValue;
	}

	/**
	 * Modifies the speed of the animation. Will only take effect with
	 * the next set call.
	 */
	public void setDuration(long nuDuration) {
		animationDuration = nuDuration;
	}

	public static long msToNs(int ms) {
		return (long)ms * 1000000;
	}
}
