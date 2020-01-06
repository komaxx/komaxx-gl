package com.komaxx.komaxx_gl.math;



/**
 * Three-dimensional equivalent to GlRect
 *   
 * @author Matthias Schicker
 */
public class GlCube {
	/**
	 * The upper left front coordinate
	 */
	public float[] ulf = new float[]{0,0,0,1};

	/**
	 * The lower right back coordinate
	 */
	public float[] lrb = new float[]{0,0,0,1};
	
	public GlCube(){ /*nothing to do*/}
	
	public GlCube(float left, float top, float front, float right, float bottom, float back){
		this.ulf[0] = left;
		this.ulf[1] = top;
		this.ulf[2] = front;
		
		this.lrb[0] = right;
		this.lrb[1] = bottom;
		this.lrb[2] = back;
	}
	
	public final boolean contains(float x, float y, float z){
		return 
			x > ulf[0] && x < lrb[0] && 
			y < ulf[1] && y > lrb[1] &&
			z < ulf[2] && z > lrb[2]; 
	}
	
	/**
	 * Computes if containing without considering the z value. So, basically it checks
	 * containing in the projection onto the z=0 plane.
	 */
	public boolean containsXY(float x, float y) {
		return 
		x > ulf[0] && x < lrb[0] && 
		y < ulf[1] && y > lrb[1]; 
	}

	/**
	 * Computes if containing when projected onto the y-axis.
	 */
	public boolean containsY(float y) {
		return y < ulf[1] && y > lrb[1]; 
	}

	
//    public boolean intersects(GlCube other) {
//        return contains(other.left, other.top) || contains(other.left, other.bottom) ||
//                contains(other.right, other.top) || contains(other.right, other.bottom) ||
//                other.contains(left, top) || other.contains(left, bottom) ||
//                other.contains(right, top) || other.contains(right, bottom);
//    }
	
	public final float width() {
		return lrb[0]-ulf[0];
	}

	public final float height() {
		return ulf[1] - lrb[1];
	}

	public final float depth() {
		return ulf[2] - lrb[2];
	}

	public final void set(float left, float top, float front, float right, float bottom, float back) {
		this.ulf[0] = left;
		this.ulf[1] = top;
		this.ulf[2] = front;
		
		this.lrb[0] = right;
		this.lrb[1] = bottom;
		this.lrb[2] = back;
	}
	
	@Override
	public final String toString() {
		return "|‾"+ulf[0]+","+ulf[1]+","+ulf[2]+" / "+lrb[0]+","+lrb[1]+","+lrb[2]+"_|";
	}
	
	public final void set(GlCube c) {
		Vector.set3(ulf, c.ulf);
		Vector.set3(lrb, c.lrb);
	}

	public final float centerX() {
		return ulf[0] + (lrb[0]-ulf[0])/2;
	}

	public final float centerY() {
		return lrb[1] + (ulf[1]-lrb[1])/2;
	}

	public final float centerZ() {
		return lrb[2] + (ulf[2]-lrb[2])/2;
	}

	public final void moveY(float f) {
		ulf[1] += f;
		lrb[1] += f;
	}

	public void setXY(float left, float top, float right, float bottom) {
		this.ulf[0] = left;
		this.ulf[1] = top;
		
		this.lrb[0] = right;
		this.lrb[1] = bottom;
	}

	public void setXY(GlRect nuPosition) {
		this.ulf[0] = nuPosition.left;
		this.ulf[1] = nuPosition.top;
		
		this.lrb[0] = nuPosition.right;
		this.lrb[1] = nuPosition.bottom;
	}

	public void setZ(float front, float back) {
		this.ulf[2] = front;
		this.lrb[2] = back;
	}

	public void setX(float left, float right) {
		this.ulf[0] = left;
		this.lrb[0] = right;
	}
	
	public void setY(float top, float bottom) {
		this.ulf[1] = top;
		this.lrb[1] = bottom;
	}

	public void getCenter2(float[] v) {
		v[0] = centerX();
		v[1] = centerY();
	}
}
