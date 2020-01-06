package com.komaxx.komaxx_gl.math;

import com.komaxx.komaxx_gl.util.RenderUtil;

import android.graphics.PointF;


/**
 * Like a normal RectF but with an y-axis used in GL (more up == bigger value, topY > bottomY)
 *   
 * @author Matthias Schicker
 */
public class GlRect {
	public float left = 0f;
	public float top = 0;
	public float right = 0;
	public float bottom = 0;
	
	public GlRect(){ /*nothing to do*/}
	
	public GlRect(float left, float top, float right, float bottom){
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public static GlRect create(float left, float top, float width, float height){
		GlRect ret = new GlRect();
		ret.left = left;
		ret.top = top;
		ret.right = left + width;
		ret.bottom = top - height;
		return ret;
	}
	
	public final boolean contains(float x, float y){
		return x > left && x < right && y < top && y > bottom; 
	}
	
	public boolean containsY(float y) {
		return y < top && y > bottom; 
	}
	
	public boolean containsX(float x) {
		return x > left && x < right; 
	}

    public boolean intersects(GlRect other) {
        return contains(other.left, other.top) || contains(other.left, other.bottom) ||
                contains(other.right, other.top) || contains(other.right, other.bottom) ||
                other.contains(left, top) || other.contains(left, bottom) ||
                other.contains(right, top) || other.contains(right, bottom);
    }
	
	public final float width() {
		return right-left;
	}

	public final float height() {
		return top-bottom;
	}

	public final void set(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	@Override
	public final String toString() {
		return "|'"+left+","+top+" / "+right+","+bottom+",|";
	}

	public final float centerX() {
		return left + (right-left)/2;
	}

	public final void set(GlRect r) {
		this.left = r.left;
		this.top = r.top;
		this.right = r.right;
		this.bottom = r.bottom;
	}

	public final float centerY() {
		return top - (top-bottom)/2;
	}
	
	public void moveX(float f) {
		left += f;
		right += f;
	}

	/**
	 * Moves the rect (adds the value to 'top' and 'bottom') along the yAxis.
	 */
	public final void moveY(float f) {
		top += f;
		bottom += f;
	}

	public boolean contains(PointF p) {
		return contains(p.x, p.y);
	}
	
	public boolean contains(float[] v) {
		return contains(v[0], v[1]);
	}

	public void union(GlRect r) {
		if (left > r.left) left = r.left;
		if (right < r.right) right = r.right;
		if (top < r.top) top = r.top;
		if (bottom > r.bottom) bottom = r.bottom;
	}

	public static boolean intersects(GlRect a, GlRect b) {
		return 	 !(b.left > a.right || 
		           b.right < a.left || 
		           b.top < a.bottom ||
		           b.bottom > a.top);
	}

	/**
	 * Ensures that left < right and bottom < top, switching values if necessary.
	 */
	public void sort() {
		if (left > right){
			float tmp = right;
			right = left;
			left = tmp;
		}
		
		if (top < bottom){
			float tmp = top;
			top = bottom;
			bottom = tmp;
		}
	}

	public void scale(float f) {
		left *= f;
		right *= f;
		top *= f;
		bottom *= f;
	}

	/**
	 * Makes the GlRect grow to contain this point. If already
	 * inside, does nothing.
	 */
	public void enlarge(float x, float y) {
		if (x < left) left = x;
		else if (x > right) right = x;
		
		if (y > top) top = y;
		if (y < bottom) bottom = y;
	}

	/**
	 * Makes the GlRect grow to contain this point. If already
	 * inside, does nothing.
	 */
	public void enlarge(float[] v) {
		enlarge(v[0], v[1]);
	}

	/**
	 * Move the rect to be completely inside of the the 'outer' Rect.
	 * If the outer rect is smaller than the inner one, the result is undefined
	 * but no exception will be thrown.
	 */
	public void moveInside(GlRect outer) {
		if (left < outer.left) moveX(outer.left - left);
		if (right > outer.right) moveX(outer.right - right);
		if (top > outer.top) moveY(outer.top - top);
		if (bottom < outer.bottom) moveY(outer.bottom - bottom);
	}

	/**
	 * Changes the values of the point array to lay inside of the rect.
	 * Expects the <code>point</code> array to be an array with at least
	 * 2 elements, which are interpreted as x and y values. </br>
	 * Make sure the rect is "sorted". Otherwise, results are undefined.
	 */
	public void clampInside(float[] point) {
		point[0] = RenderUtil.clamp(point[0], left, right); 
		point[1] = RenderUtil.clamp(point[1], bottom, top); 
	}

	/**
	 * Computes the movement necessary to move this GlRect completely
	 * inside of the <code>outer</code> GlRect. (0,0) when no movement
	 * is necessary. Undefined when this GlRect is smaller than the given
	 * outer GlRect.
	 */
	public void getMoveInsideDelta(float[] delta, GlRect outer) {
		if (left < outer.left) delta[0] = outer.left - left;
		else if (right > outer.right) delta[0] = outer.right - right;
		else delta[0] = 0;
		
		if (top > outer.top) delta[1] = outer.top - top;
		else if (bottom < outer.bottom) delta[1] = outer.bottom - bottom;
		else delta[1] = 0;
	}
}
