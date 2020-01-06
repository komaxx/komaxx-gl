package com.komaxx.komaxx_gl;

import java.util.Hashtable;

import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.komaxx.komaxx_gl.math.MatrixUtil;
import com.komaxx.komaxx_gl.math.GlRect;
import com.komaxx.komaxx_gl.math.GlTrapezoid;
import com.komaxx.komaxx_gl.math.Vector;
import com.komaxx.komaxx_gl.scenegraph.SceneGraph;
import com.komaxx.komaxx_gl.util.ObjectsStore;

/**
 * The specializations of the SceneGraphContext are used when traversing the
 * SceneGraph. It is used to store a current state, which is the cumulation
 * of all state changes of nodes in the path that lead to the node.
 * 
 * @author Matthias Schicker
 */
public abstract class SceneGraphContext {
	public SceneGraph sceneGraph;
	
	/**
	 * Indicates whether the world has changed somehow (pv or model matrix) since 
	 * the last frame, so re-layouting is necessary.
	 */
	public IntegerStack worldIdStack = new IntegerStack(0);
	
	/**
	 * If this is turned false, rendering and transformation will be suspended
	 * for all subsequent nodes.
	 */
	public BooleanStack visibilityStack = new BooleanStack(true);
	
	private boolean mvpMatrixDirty = true;

	public MatrixStack modelMatrixStack = new MatrixStack();
	public MatrixStack projectionViewMatrixStack = new MatrixStack();
	
	private float[] currentMvpMatrix = new float[MatrixUtil.MATRIX_SIZE];
	
	protected boolean scissorRectDirty = true;
	public RectStack scissorStack = new RectStack();
	
	/**
	 * The [0;0  /  surfaceWidth;surfaceHeight] rect.
	 * Will be used instead of the scissor box when scissor test is deactivated.
	 */
	private Rect surfaceBox = new Rect();
	
	/**
	 * In here, apps using the scenegraph can store any kind of data as a means
	 * of very loosely coupled data transfer. Will not be reset each frame. </br>
	 * Very useful for layouting info.
	 */
	public Hashtable<String, Float> floatExtras = new Hashtable<String, Float>();
	
	/**
	 * MUST be called after the scissor Rect was changed. Otherwise, the change
	 * will have no effect.
	 */
	public void setScissorRectDirty(){
		scissorRectDirty = true;
		z0TrapezoidDirty = true;
	}
	
	/**
	 * This should stay true for a frame that does not contain larger animations.
	 * When a frame is considered idle, more background tasks will be executed.
	 * So, e.g., when the view scrolls, idleFrame should be set to false!
	 */
	protected boolean idleFrame;
	/**
	 * Useful for nodes to check whether the UI is currently in a idle phase.
	 */
	protected boolean lastFrameWasIdle;
	
	public final void basicReset(SceneGraphContext basicContext){
		this.surfaceWidth = basicContext.surfaceWidth;
		this.surfaceHeight = basicContext.surfaceHeight;

		sceneGraph = basicContext.sceneGraph;

		worldIdStack.reset();
		visibilityStack.reset();
		modelMatrixStack.reset();
		eyePointStack.reset();
		projectionViewMatrixStack.reset();
		
		scissorStack.reset(0, 0, surfaceWidth, surfaceHeight);
		z0TrapezoidDirty = true;
		scissorTestActivated = true;
		activateScissorTest(false);
		
		lastFrameWasIdle = idleFrame;
		idleFrame = true;
	}
	
	public void setMvpMatrixDirty(){
		mvpMatrixDirty = true;
		z0TrapezoidDirty = true;
	}
	
	public float[] getMvpMatrix() {
		if (mvpMatrixDirty){
			Matrix.multiplyMM(currentMvpMatrix, 0, 
				projectionViewMatrixStack.peek(), 0, 
				modelMatrixStack.peek(), 0);
			mvpMatrixDirty = false;
		}
		return currentMvpMatrix;
	}
	
	public void activateScissorTest(boolean b) {
		if (b != scissorTestActivated){
			if (b) GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
			else GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
			scissorTestActivated = b;
			scissorRectDirty = true;
			z0TrapezoidDirty = true;
		}
		
		if (scissorRectDirty){
			if (scissorTestActivated){
				Rect scissorRect = scissorStack.peek();
				GLES20.glScissor(scissorRect.left, surfaceHeight - scissorRect.bottom, scissorRect.width(), scissorRect.height());
			} else {
				GLES20.glScissor(0, 0, surfaceWidth, surfaceHeight);
			}
			scissorRectDirty = false;
		}
	}
	
	/**
	 * This is the *overall* width of the surface in px. This area may be scissored to a
	 * smaller area. See therefore the scissorStack!
	 */
	public int surfaceWidth = 1;
	/**
	 * This is the *overall* height of the surface in px. This area may be scissored to a
	 * smaller area. See therefore the scissorStack!
	 */
	public int surfaceHeight = 1;

	/**
	 * When a new frame rendering (or other traversal) starts, this value is
	 * incremented. Starts with 0;
	 */
	public int frame;
	/**
	 * When rendering/traversal begins, System.nanoTime() is read out. this value
	 * should then be used by all nodes for animations to ensure synchronized animation.
	 */
	public long frameNanoTime = 0;
	
	/**
	 * Initially set by camera nodes. Written by camera nodes. Useful to align stuff
	 * according to the view direction. </br>
	 * When a node is transforming, it <b>must</b> process this point as well to ensure
	 * that the eyePoint is always correct in the current coordinate system (->model matrix)
	 */
	public MatrixStack eyePointStack = new MatrixStack(4);
	
	/**
	 * Specifies whether the scissor test is currently active or not. Will be set by nodes
	 * during rendering.
	 */
	public boolean scissorTestActivated;
	
	/**
	 * Used for quick hit-tests. Bounding box of the visible edges on z==0 plane.
	 * NOTE: The distance between camera and this point is stored at index 2! So
	 * this may not be used as real z value!
	 */
	private GlRect z0BoundingBox = new GlRect();
	private GlTrapezoid z0VisibleTrapezoid = new GlTrapezoid(3);
	private boolean z0TrapezoidDirty = true;

	public GlRect getZBoundingBox(){
		if (z0TrapezoidDirty) recomputeZTrapezoid();
		return z0BoundingBox;
	}
	
	public GlTrapezoid getZ0VisibleTrapezoid(){
		if (z0TrapezoidDirty) 
			recomputeZTrapezoid();
		return z0VisibleTrapezoid;
	}
	
	private float[] tmpInput = new float[2];
	private void recomputeZTrapezoid() {
		if (cameraInfoProvider == null) return;
		
		Rect boundsRect;
		if (scissorTestActivated){
			boundsRect = scissorStack.peek();
		} else {
			boundsRect = surfaceBox;
			boundsRect.right = surfaceWidth;
			boundsRect.bottom = surfaceHeight;
		}
		
		tmpInput[0] = boundsRect.left;
		tmpInput[1] = boundsRect.top;
		cameraInfoProvider.pixel2ZeroPlaneCoord(z0VisibleTrapezoid.ul, tmpInput);

		tmpInput[1] = boundsRect.bottom;
		cameraInfoProvider.pixel2ZeroPlaneCoord(z0VisibleTrapezoid.ll, tmpInput);

		tmpInput[0] = boundsRect.right;
		cameraInfoProvider.pixel2ZeroPlaneCoord(z0VisibleTrapezoid.lr, tmpInput);

		tmpInput[1] = boundsRect.top;
		cameraInfoProvider.pixel2ZeroPlaneCoord(z0VisibleTrapezoid.ur, tmpInput);
		
		z0VisibleTrapezoid.getBoundingBox(z0BoundingBox);
		z0TrapezoidDirty = false;
	}
	
	public boolean isIdle(){
		return idleFrame && lastFrameWasIdle;
	}
	
	public void setNotIdle() {
		idleFrame = false;
	}
	
	public ICameraInfoProvider cameraInfoProvider;
	public void setCameraInfoProvider(ICameraInfoProvider nuInfoProvider){
		this.cameraInfoProvider = nuInfoProvider;
		z0TrapezoidDirty = true;
	}
	
	public float pixelYtoWorldY(float pixelY) {
		return (pixelY/(float)scissorStack.peek().height()) * getZBoundingBox().height();
	}
	
	public float pixelXtoWorldX(float pixelX) {
		return (pixelX/(float)scissorStack.peek().width()) * getZBoundingBox().width();
	}

	/**
	 * Does essentially what the hardware does: Projecting a world-coord into
	 * pixel coords.
	 */
	public void worldToScreen(float[] resultPx, float worldX, float worldY, float worldZ) {
		float[] world = ObjectsStore.getVector();
		Vector.set4(world, worldX, worldY, worldZ, 1);
		
		float[] mvpMatrix = getMvpMatrix();
		Matrix.multiplyMV(resultPx, 0, mvpMatrix, 0, world, 0);
		
		Vector.normalize4(resultPx);
		resultPx[0] = (resultPx[0]+1)/2 *surfaceWidth;
		resultPx[1] = -(resultPx[1]-1)/2 *surfaceHeight;
		
		ObjectsStore.recycleVector(world);
	}
}
