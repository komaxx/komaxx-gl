package com.komaxx.komaxx_gl.util;

public class Interpolators {
	private Interpolators(){}
    
    /**
     * Extra simple Interpolator: linear
     */
    public static class LinearInterpolator implements Interpolator{
        private final float startY;
        private final float endY;
        private long startX;
        private long endX;

        private float yDelta;
        private float xDelta;
        
        public LinearInterpolator(float startY, float endY, long startX, long endX){
            this.startY = startY;
            this.endY = endY;
            this.startX = startX;
            this.endX = endX;
            yDelta = endY - startY;
            xDelta = endX - startX;
        }
        
        @Override
		public float getValue(long x) {
            if (x >= endX) return endY;
            if (x <= startX) return startY;
            
            float pos = (float)(x-startX) / xDelta;
            return startY + pos*yDelta;
        }

        @Override
		public long getStartX() {return startX;}

        @Override
		public long getEndX() {return endX;}


        @Override
		public float getEndY() { return endY;}

		@Override
		public void translateX(long deltaX) {
			startX += deltaX;
			endX += deltaX;
		}
    }

    /**
     * different behavior depending on <code>power</code>:<br/>
     * > 1: smooth start, fastening, abrupt ending<br/>
     * ==1: linear interpolation<br/>
     * < 1: quick start, slowing down<br/>
     * <b>NOTE</b>: Values <= 0 for "power" are not permitted. Will then interpolate linearly!
     * @author Matthias Schicker
     */
    public static class PowerInterpolator implements Interpolator{
        protected final double startY;
        protected final double endY;
        protected long startX;
        protected long endX;

        protected double yDelta;
        protected double xDelta;
        protected final double power;
        
        public PowerInterpolator(double power, double startY, double endY, long startX, long endX){
            if (power < 0) power = 1;
            this.power = power;
            this.startY = startY;
            this.endY = endY;
            this.startX = startX;
            this.endX = endX;
            yDelta = endY - startY;
            xDelta = endX - startX;
        }
        
        @Override
		public float getValue(long x) {
            if (x <= startX) return (float) startY;
            if (x >= endX) return (float) endY;
            
            double pos = (double)(x-startX) / xDelta;
            
            return (float)(startY + Math.pow(pos, power) * yDelta);
        }
        
       
        @Override
		public long getStartX() {return startX;}
        
        @Override
		public long getEndX() {return endX;}
        
        @Override
		public float getEndY() {return (float) endY;}
		
		@Override
		public void translateX(long deltaX) {
			startX += deltaX;
			endX += deltaX;
		}
    }
    
    /**
     * Creates the inverse effect of a PowerInterpolator: Starts quick, then slows down.
     */
	public static class InverseSquareInterpolator implements Interpolator {
        protected final double startY;
        protected final double endY;
        protected long startX;
        protected long endX;

        protected double yDelta;
        protected double xDelta;
        
        public InverseSquareInterpolator(double startY, double endY, long startX, long endX){
            this.startY = startY;
            this.endY = endY;
            this.startX = startX;
            this.endX = endX;
            yDelta = endY - startY;
            xDelta = endX - startX;
        }
        
        @Override
		public float getValue(long x) {
            if (x <= startX) return (float) startY;
            if (x >= endX) return (float) endY;
            
            double pos = (double)(x-startX) / xDelta;	// normalize to [0|1]
            
            return (float)(startY + (1-(1-(pos*pos))) * yDelta);
        }
        
       
        @Override
		public long getStartX() {return startX;}
        
        @Override
		public long getEndX() {return endX;}
        
        @Override
		public float getEndY() {return (float) endY;}
		
		@Override
		public void translateX(long deltaX) {
			startX += deltaX;
			endX += deltaX;
		}
	}
    
    
    public static class BumpInterpolator implements Interpolator {
        protected final double startY;
        protected final double maxY;
        protected long startX;
        protected long endX;
        
        protected double yDelta;
        protected double xDelta;


		public BumpInterpolator(float startY, float maxY, long startX, long endX) {
			this.startY = startY;
			this.maxY = maxY;
			this.startX = startX;
			this.endX = endX;

			yDelta = maxY - startY;
            xDelta = endX - startX;
		}

		@Override
		public float getValue(long x) {
			if (x >= endX) return (float) startY;
			
			double pos = ((double)(x-startX) / xDelta) * 2.0 - 1.0;  // transformed x to [-1|1]
            return (float)(startY + (1.0 - Math.pow(pos, 2)) * yDelta);
		}

		@Override
		public long getStartX() { return startX; }

		@Override
		public long getEndX() { return endX; }

		@Override
		public float getEndY() { return (float) maxY; }

		@Override
		public void translateX(long deltaX) {
			startX += deltaX;
			endX += deltaX;
		}
	}

    
    public static class OverBumpInterpolator extends PowerInterpolator {
        public OverBumpInterpolator(double startY, double endY, long startX, long endX) {
            super(0, startY, endY, startX, endX);
        }

        @Override
        public float getValue(long x) {
            if (x >= endX) return (float) endY;
            if (x <= startX) return (float) startY;
            
            double pos = (double)(x-startX) / xDelta;
            return (float) (startY + (1.2 * Math.sin(2*pos))*yDelta);
        }
    }

    public static interface Interpolator {
        float getValue(long x);
        long getStartX();
        long getEndX();
        float getEndY();
		void translateX(long delta);
    }
    
    
    /**
     * Not really a hyperbel ;) Starts quick, smooth ending. 
     * @author Matthias Schicker
     */
    public static class HyperbelInterpolator extends PowerInterpolator{
        public HyperbelInterpolator(double startY, double endY, long startX, long endX){
            super(1, startY, endY, startX, endX);
        }
        
        @Override
        public float getValue(long x) {
            if (x >= endX) return (float) endY;
            if (x <= startX) return (float) startY;
            
            double pos = (double)(x-startX) / xDelta;
            
            return (float)(startY + (1 + Math.pow(pos-1, 3)) * yDelta);
        }
    }

    /**
     * This interpolator is created with a current speed and an attenuation-strength
     */
    public static class AttenuationInterpolator implements Interpolator {
        private final double startY;
        private long startX;
        private long endX;
        private final double endY;
        
        private final double startSpeedPerX;
        private final double linearEndY;
        private final double xSpan;

        public AttenuationInterpolator(double startY, long startX, long endX, double startSpeedPerX){
            this.startY = startY;
            this.startX = startX;
            this.endX = endX;
            this.startSpeedPerX = startSpeedPerX;
            
            xSpan = endX - startX;
            linearEndY = 0.5 * startSpeedPerX*(double)xSpan;
            endY = doGetValue(endX);
        }

        @Override
		public long getEndX() {
            return endX;
        }

        @Override
		public float getEndY() {
            return getValue(endX);
        }

        @Override
		public long getStartX() {
            return startX;
        }

        @Override
		public float getValue(long x) {
            if (x >= endX) return (float) endY;
            if (x <= startX) return (float) startY;

            return doGetValue(x);
        }
        
        private float doGetValue(long x){
            double deltaX = x-startX;
            return (float) (startY + startSpeedPerX * (double)deltaX 
                    - Math.pow(deltaX / xSpan, 2) * linearEndY
                    );
        }
        
		@Override
		public void translateX(long deltaX) {
			startX += deltaX;
			endX += deltaX;
		}
    }
}
