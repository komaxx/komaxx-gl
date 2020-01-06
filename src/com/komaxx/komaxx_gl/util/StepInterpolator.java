package com.komaxx.komaxx_gl.util;

import com.komaxx.komaxx_gl.util.Interpolators.Interpolator;

/**
 * Takes an array of steps in ]0;1[ and interpolates linearly between
 * them.
 */
public class StepInterpolator implements Interpolator {
	private final float basicY;
    
    private long startX;
    private long endX;

    private float xDelta;
    
    private float scaling;
    
    private final int l;
    
    private Step[] steps;
    
    
    public StepInterpolator(Step[] steps, long startX, long endX, float basicY, float scaling){
    	this.steps = new Step[steps.length+1];
    	System.arraycopy(steps, 0, this.steps, 0, steps.length);
    	this.steps[steps.length] = new Step(1.01f, steps[steps.length-1].height);
    	
    	this.startX = startX;
    	this.endX = endX;
    	
    	this.basicY = basicY;
    	this.scaling = scaling;
    	
    	l = this.steps.length;
    	xDelta = endX-startX;
    }
    
	@Override
	public float getValue(long timeX) {
		if (timeX <= startX) return basicY;
		else if (timeX >= endX) return getEndY();
		
		float x = (float)(timeX-startX) / xDelta;
		
		for (int i = 0; i < l; i++){
			if (x < steps[i].stepTime){
				float intervalX = (x-steps[i-1].stepTime) / (steps[i].stepTime - steps[i-1].stepTime);
				return basicY + 
					(steps[i-1].height*(1f-intervalX) + steps[i].height*intervalX) * scaling;
			}
		}

		// before the first step
		return basicY + (steps[0].height*x) * scaling;

	}
	
	@Override
	public long getStartX() {
		return startX;
	}
	
	@Override
	public long getEndX() {
		return endX;
	}
	
	@Override
	public float getEndY() {
		return basicY + steps[l-1].height * scaling;
	}
	
	@Override
	public void translateX(long delta) {
		startX += delta;
		endX += delta;
	}
	
	
	public static class Step {
		public float stepTime;
		public float height;

		public Step(float time, float height) {
			this.stepTime = time;
			this.height = height;
		}
	}
	
	public static Step[] BUMPER_STEPS = new Step[]{
		new Step(0, 0),
		new Step(0.1f, 0.1f),
		new Step(0.2f, 0.3f),
		new Step(0.4f, 1),
		new Step(0.9f, -0.1f),
		new Step(1, 0)
	};
	
    public static final Step[] FLAT_STEPS = new Step[]{
    	new Step(0, 0),
    	new Step(1, 1)
    };
    
    private static int SIN_STEPS = 32;
    public static final Step[] SINUS_STEPS = new Step[SIN_STEPS];
    public static final Step[] QUATTRO_SINUS_STEPS = new Step[SIN_STEPS];
    
    static {
    	fillSinus(SINUS_STEPS, 1);
    	fillSinus(QUATTRO_SINUS_STEPS, 4);
    }

	private static void fillSinus(Step[] steps, int sini) {
    	float deltaStepX = 1f / SIN_STEPS;
    	float stepX = 0;
    	for (int i = 0; i < SIN_STEPS; i++){
    		steps[i] = new Step(stepX, (float) Math.sin(stepX * (sini*2*Math.PI)));
    		
    		stepX += deltaStepX;
    	}
	}
}