package com.komaxx.komaxx_gl.math;

import com.komaxx.komaxx_gl.util.KoLog;

/**
 * The geometric, two-dimensional object.
 * CAREFUL: For speedups, the vertices of the trapezoid are directly
 * accessible. NEVER forget to call setDirty() after one of the vertices
 * was changed. Otherwise errors will occur whenever using mehtods
 * like intersect, getBoundingBox, ...  
 * 
 * @author Matthias Schicker
 */
public class GlTrapezoid {
	/**
	 * The "upper left" edge. ALWAYS call setDirty after any changes were made!
	 */
	public final float[] ul;
	/**
	 * The "lower left" edge. ALWAYS call setDirty after any changes were made!
	 */
	public final float[] ll;
	/**
	 * The "lower right" edge. ALWAYS call setDirty after any changes were made!
	 */
	public final float[] lr;
	/**
	 * The "upper right" edge. ALWAYS call setDirty after any changes were made!
	 */
	public final float[] ur;

	
	private boolean dirty = true;
	
	private GlRect bounds = new GlRect();
	/**
	 * Order: left, top, right, bottom
	 */
	private float[][] normals = new float[4][];
	
	// /////////////////////////////////////////////////
	// temps
	private static float[] tmpAtoB = new float[2];
	
	/**
	 * Use this constructor to define edge points with more than the
	 * necessary two dimensions. NOTE that while extra dimensions are
	 * valuable for storage purposes, many methods will not work properly
	 * or make no sense (boundingBox, contains, ...). These methods
	 * will only operate on the first two dimensions.
	 */
	public GlTrapezoid(int vectorDimensionality){
		if (vectorDimensionality < 2) throw new IllegalArgumentException("Only a dimensionality >1 is acceptable");

		ul = new float[vectorDimensionality];
		ll = new float[vectorDimensionality];
		lr = new float[vectorDimensionality];
		ur = new float[vectorDimensionality];
		
		for (int i = 0; i < 4; i++){
			normals[i] = new float[vectorDimensionality];
		}
	}
	
	public GlTrapezoid(){
		this(2);
	}
	
	/**
	 * Computes the bounding box of the trapezoid. Regardless whether the Trapezoid
	 * is <code>sort</code>ed first.
	 * @param result	may be null. Use this to avoid memory allocation.
	 * @return	Either the result parameter or a new GlRect.
	 */
	public GlRect getBoundingBox(GlRect result){
		if (result == null) result = new GlRect();
		
		if (dirty){
			recomputeTmps();
		}
		
		result.set(bounds);
		
		return result;
	}
	
	private void recomputeTmps() {
		bounds.set(
				Math.min(Math.min(Math.min(ul[0], ll[0]), ur[0]), lr[0]),
				Math.max(Math.max(Math.max(ul[1], ll[1]), ur[1]), lr[1]),
				Math.max(Math.max(Math.max(ul[0], ll[0]), ur[0]), lr[0]),
				Math.min(Math.min(Math.min(ul[1], ll[1]), ur[1]), lr[1])
				);
		
		// build normals
		buildNormal(normals[0], ul, ll, ur);		// left
		buildNormal(normals[1], ul, ur, ll);		// top
		buildNormal(normals[2], ur, lr, ul);		// right
		buildNormal(normals[3], ll, lr, ul);		// bottom
		
		dirty = false;
	}

	public void sort() {
		if (ur[0] < ul[0]){
			float tmp = ur[0];
			ur[0] = ul[0];
			ul[0] = tmp;
		}
		if (lr[0] < ll[0]){
			float tmp = lr[0];
			lr[0] = ll[0];
			ll[0] = tmp;
		}
		
		if (ul[1] < ll[1]){
			float tmp = ul[1];
			ul[1] = ll[1];
			ll[1] = tmp;
		}
		if (ur[1] < lr[1]){
			float tmp = ur[1];
			ur[1] = lr[1];
			lr[1] = tmp;
		}
		
		dirty = true;
	}

	private static void buildNormal(float[] result, float[] a, float[] b, float[] direction) {
		Vector.aToB2(tmpAtoB, a, b);
		Vector.normal2(result, tmpAtoB);
		// need to invert the normal
		Vector.aToB2(tmpAtoB, a, direction);
		if (Vector.dotProduct2(result, tmpAtoB) < 0) Vector.invert2(result);
	}

	/**
	 * Call this, when the trapezoid was changed. Otherwise, some calls
	 * like 'contains' will deliver incorrect results.
	 */
	public void invalidate(){
		dirty = true;
	}
	
	/**
	 * Correct results only for convex (true) trapezoids guaranteed.
	 */
	public boolean contains(float[] v){
		if (dirty) recomputeTmps();
		
		// quick-check
		if (!bounds.contains(v[0], v[1])) return false;
		
		// precise check
		return 
			Vector.dotProduct2(normals[0], Vector.aToB2(tmpAtoB, ul, v)) > 0 && 	// left
			Vector.dotProduct2(normals[1], Vector.aToB2(tmpAtoB, ul, v)) > 0 && 	// top
			Vector.dotProduct2(normals[2], Vector.aToB2(tmpAtoB, ur, v)) > 0 && 	// right
			Vector.dotProduct2(normals[3], Vector.aToB2(tmpAtoB, lr, v)) > 0;	 	// bottom
	}
	
	public static void runContainsTest(){
		KoLog.i("TST", "Running Trapezoid.Contains Test ...");
		
		GlTrapezoid t = new GlTrapezoid();

		// simple: test rect case
		t.ul[0] = -10;	t.ul[1] = 10;
		t.ur[0] = 10;	t.ur[1] = 10;
		t.ll[0] = -10;	t.ll[1] = -10;
		t.lr[0] = 10;	t.lr[1] = -10;
		
		if (t.contains(new float[]{0,0}) &&
			!t.contains(new float[]{15,15}) &&
			!t.contains(new float[]{-15,-15}) &&
			!t.contains(new float[]{0,15})){
			
			KoLog.i("TST", "Trapezoid.Contains RECT Test case complete");
		} else {
			KoLog.i("TST", "Trapezoid.Contains RECT Test case FAILED");
		}
	}

	public void move2(float[] delta) {
		// unfolded for speed
		ll[0] += delta[0];
		lr[0] += delta[0];
		ul[0] += delta[0];
		ur[0] += delta[0];
		
		ll[1] += delta[1];
		lr[1] += delta[1];
		ul[1] += delta[1];
		ur[1] += delta[1];
		
		dirty = true;
	}

	public void setDirty() {
		dirty = true;
	}
}
