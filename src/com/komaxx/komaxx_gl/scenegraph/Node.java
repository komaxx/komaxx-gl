package com.komaxx.komaxx_gl.scenegraph;

import java.util.ArrayDeque;
import java.util.ArrayList;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.SceneGraphContext;
import com.komaxx.komaxx_gl.bound_meshes.Vbo;
import com.komaxx.komaxx_gl.scenegraph.interaction.InteractionContext;

/**
 * A Node is the central class of our scene graph. It handles it's own state
 * and render context and is responsible of dispatching interaction events.
 * A Node may have any arbitrary number of sub-nodes.
 * 
 * @author Matthias Schicker
 */
public class Node {
	public static final short NO_CLUSTER_INDEX = -1;
	/**
	 * Give this to all nodes that may be rendered in any arbitrary order
	 * (perhaps because they are not actually painted).
	 */
	public static final short CLUSTER_INDEX_INVISIBLE = -2;

	
	/**
	 * Used as value for state changing attributes (like blending or depth test).
	 * Activates a feature.
	 */
	public static final byte ACTIVATE = 0;
	/**
	 * Used as value for state changing attributes (like blending or depth test).
	 * De-activates a feature.
	 */
	public static final byte DEACTIVATE = -1;
	/**
	 * Used as value for state changing attributes (like blending or depth test).
	 * No changes will be issued.
	 */
	public static final byte DONT_CARE = -2;
	
	
	private ArrayList<Node> children = new ArrayList<Node>();
	
	/**
	 * Used for debug purposes.
	 */
	private String name = getClass().getSimpleName();
	
	private Node parent;
	
	/**
	 * Defines, which RenderProgram is to be used with this node. Return
	 * -1 when no specific RenderProgram is necessary.
	 */
	public int renderProgramIndex = -1;
	
	/**
	 * The node's VBO. Leave it null when not used.
	 */
	public Vbo vbo = null;
	
	/**
	 * This value is used to determine the right/optimal order of rendering the nodes.
	 */
	public int zLevel = 0;

	/**
	 * Nodes with the same clusterIndex will be combined to one cluster during linearization.
	 * This speeds up the linearization process GREATLY. Use this for nodes where the painting/
	 * interaction order is not relevant.
	 */
	public short clusterIndex = NO_CLUSTER_INDEX;
	
	/**
	 * Nodes usually only have one texture. The handle to this texture should be
	 * set here, so that the graph linearization can determine a path with as few
	 * switches as possible. 
	 */
	public int textureHandle = -1;
	
	/**
	 * All nodes that actually paint somewhere on the screen should set this to
	 * true, because only then will the current state be bound to the active
	 * RenderProgam and the rendering happens.
	 * CAREFUL: Only set to true when also a RenderingProgram is specified!
	 */
	public boolean draws = false;
	
	/**
	 * Set this to true when the node does anything to the render-state that
	 * has to apply to all nodes further down in the tree. Also, 
	 * <code>onTransform</code> will only be called when this is true!
	 */
	public boolean transforms = false;
	
	/**
	 * When false, this node and the lower branch will not be painted, onRender will
	 * not be called.
	 */
	public boolean visible = true;
	
	/**
	 * When false, depth buffering will be disabled during rendering this node.
	 * Default: ACTIVATE
	 */
	public byte depthTest = DEACTIVATE;
	
	/**
	 * When DEACTIVATE, depth buffering will be disabled during rendering this node.
	 * DONT_CARE disables any state changes. Default: DONT_CARE
	 */
	public byte blending = DONT_CARE;
	
	/**
	 * When false, scissor testing will be disabled during rendering this node.
	 * Default: DEACTIVATE
	 */
	public byte scissorTest = DEACTIVATE;
	
	/**
	 * defines the target function when blending. only active when blending activated.
	 */
	public int blendFunction = GLES20.GL_ONE_MINUS_SRC_ALPHA;
	
	/**
	 * glRunnables are executed when the node is processed in the rendering thread.
	 */
	protected ArrayDeque<IGlRunnable> glRunnables = new ArrayDeque<IGlRunnable>();
	protected ArrayDeque<IGlRunnable> glRunnablesCopy = new ArrayDeque<IGlRunnable>();		// used during rendering.
	
	/**
	 * Only this many runnables will be executed per frame. Reduce for smoother animation where necessary!
	 * Values < 0 mean that all will be executed.
	 */
	protected int maxGlRunnablesPerFrame = -1;
	
	/**
	 * Stores, for which surface 'surfaceCreated' was already called. This should
	 * prevent multiple (costly) 'onSurfaceCreated' calls
	 */
	private int lastCreatedSurfaceId = -2;
	
	/**
	 * Set true, when the node is supposed to react on user interaction.
	 */
	public boolean handlesInteraction = false;
	
	/**
	 * Set to false when the inbuilt VBO of the node should not be used/bound.
	 */
	public boolean useVboPainting = true;

	/**
	 * Stores, whether this node was attached to the SceneGraph
	 */
	private boolean attached;

	/**
	 * Only of interest when the node does transformations.
	 * If yes, the node did transformations in onTransform which
	 * need to be undone when traveling up the tree again.
	 */
	private boolean contextTransformed = false;
	
	/**
	 * When the Node is currently attached to a SceneGraph, this SceneGraph
	 * is stored in here. If no longer (or not yet) attached, this will be null.
	 */
	protected SceneGraph sceneGraph;
	
	
	protected final void setParent(Node parent) {
		this.parent = parent;
	}
	
	protected final Node getParent(){
		return parent;
	}

	protected void addChild(Node nuChild, SceneGraph sg){
		if (children.contains(nuChild)) return;
		children.add(nuChild);
		nuChild.attach(sg);
	}
	
	protected void removeChild(Node childToRemove){
		if (children.remove(childToRemove)){
			childToRemove.detach();
			childToRemove.destroy();
		}
	}
	
	/**
	 * Adds a runnable that is to be executed in the next frame. Thus, it is valid
	 * to queue a new Runnable in a IGlRunnable without causing a StackOverflowException.
	 * <b>NOTE:</b> Currently only executed for nodes with <code>draws==true</code>
	 */
	public final void queueInGlThread(IGlRunnable r){
		synchronized (glRunnables) {
			glRunnables.addLast(r);
		}
	}
	
	/**
	 * Same as queueInGlThread but the runnable will not be added if it's already queued.
	 * <b>NOTE:</b> Currently only executed for nodes with <code>draws==true</code>
	 */
	public final void queueOnceInGlThread(IGlRunnable r){
		synchronized (glRunnables) {
			if (!glRunnables.contains(r)){
				glRunnables.addLast(r);
			}
		}
	}
	
	/**
	 * Internal method. Overwrite *onTransform* and *onRender* to do your own rendering code.
	 * 
	 * This will *only* be called when "dumb rendering" for debugging. Real rendering will
	 * never call this! z-buffer and z-sorting artifacts possible and to be expected!
	 */
	public boolean dumbRender(RenderContext renderContext){
		boolean ret = false;

		if (transforms) applyStateChangeTransform(renderContext);
		contextTransformed = onTransform(renderContext);
		
		if (visible){
			if (draws){
				applyStateChangeRendering(renderContext);
			}
			processGlRunnables(renderContext);
			ret |= onRender(renderContext);
			for (Node child : children) {
				child.dumbRender(renderContext);
			}
		}
		
		if (contextTransformed) onUnTransform(renderContext);
		return ret && visible;
	}
	
	// //////////////////////
	// these are the actual render functions when rendering an optimized linearization
	public void traversalDown(SceneGraphContext sc){
		applyStateChangeTransform(sc);
		contextTransformed = transforms && onTransform(sc);
		
		sc.visibilityStack.push().b &= visible;
	}
	
	public void renderSelf(RenderContext rc){
		boolean parentVisible = rc.visibilityStack.peek().b;
		if (parentVisible && draws) applyStateChangeRendering(rc);
		processGlRunnables(rc);
		if (parentVisible && visible) onRender(rc);
	}

	public final boolean handleInteraction(InteractionContext interactionContext) {
		return 
			visible &&
			interactionContext.visibilityStack.peek().b &&
			onInteraction(interactionContext);
	}
	
	public void traversalUp(SceneGraphContext rc){
		rc.visibilityStack.pop();
		if (transforms && contextTransformed) onUnTransform(rc);
	}

	public void transformIntercation(InteractionContext interactionContext) {
		if (scissorTest != DONT_CARE){
			interactionContext.activateScissorTest(scissorTest==ACTIVATE);
		}
		if (visible && contextTransformed){
			onTransformInteraction(interactionContext);
		}
	}
	
	public void untransformIntercation(InteractionContext interactionContext) {
		if (visible && contextTransformed) onUnTransformInteraction(interactionContext);
	}

	// /////////////////////
	
	private final void processGlRunnables(RenderContext rc) {
		int toExecute = 
			(maxGlRunnablesPerFrame < 0) ? Integer.MAX_VALUE : maxGlRunnablesPerFrame;

		synchronized (glRunnables) {
			int l = glRunnables.size();
			if (l > toExecute) l = toExecute;
			for (int i = 0; i < l; i++){
				glRunnablesCopy.addLast(glRunnables.pollFirst());
			}
		}

		int l = glRunnablesCopy.size();
		for (int i = 0; i < l; i++) glRunnablesCopy.pollFirst().run(rc);
	}

	protected void applyStateChangeTransform(SceneGraphContext scContext){
		if (scissorTest != DONT_CARE){
			scContext.activateScissorTest(scissorTest==ACTIVATE);
		}
	}
	
	protected void applyStateChangeRendering(RenderContext renderContext) {
		if (textureHandle != -1) renderContext.bindTexture(textureHandle);
		if (draws && (vbo == null || !useVboPainting)) renderContext.bindVBO(0);
		else if (draws && vbo != null) renderContext.bindVBO(vbo.getHandle()); 

		if (renderProgramIndex !=-1){
			renderContext.switchRenderProgram(renderProgramIndex);
		}
		
		if (blending != DONT_CARE){
			if (blending==ACTIVATE){
				renderContext.activateBlending(true);
				renderContext.setBlendFunction(blendFunction);
			} else {
				renderContext.activateBlending(false);
			}
		}
		if (depthTest != DONT_CARE){
			renderContext.activateDepthTest(depthTest==ACTIVATE);
		}
		
		if (!renderContext.isMvpMatrixInShader() && renderProgramIndex != -1){
	    	GLES20.glUniformMatrix4fv(
	    			renderContext.currentRenderProgram.matrixMVPHandle, 1, false, renderContext.getMvpMatrix(), 0);
	    	renderContext.setMvpMatrixInShader();
		}
	}

	/**
	 * Called, when the rendering jumps back up from this node. Undo
	 * all state changes in here! If the node is the last one rendered
	 * this may not be called.
	 * @param sc	RenderContext or InteractionContext, depending on traversal type
	 */
	public void onUnTransform(SceneGraphContext sc) {
		// extension point. No code to execute in the basic class.
	}

	/**
	 * Called when processing interaction. Modify the interaction points,
	 * stored in current/last InteractionPointXyCoords, according to the 
	 * local transformations.
	 * Called after onTransform but before handleInteraction.
	 * @param interactionContext 	The current context state
	 */
	public void onTransformInteraction(InteractionContext interactionContext) {
		// extension point. No code to execute in the basic class.
	}
	
	/**
	 * Called when processing interaction. Reverse the changes made to the interaction points,
	 * stored in current/last InteractionPointXyCoords, applies in onTransformInteraction.  
	 * Called after handleInteraction but before onUnTransform.
	 * @param interactionContext 	The current context state
	 */
	public void onUnTransformInteraction(InteractionContext interactionContext) {
		// extension point. No code to execute in the basic class.
	}
	
	/**
	 * Apply any transformations to the various matrices in this step.
	 * Note that the object may not be actually rendered in this frame!<br>
	 * Will be called in render- and interaction traversal<br><br>
	 * Do not forget to call sc.setMVPMatrixDirty()! Changes to the metrices
	 * will otherwise have no effect.
	 * 
	 * @return	<code>true</code> means that the context was actually changed.
	 * Only then will onUnTransform get called. Useful, e.g., for speedups when not 
	 * visible.<br><br>
	 * 
	 * <b>Note</b> This will be called even when the node is set to visible==false.
	 * You may return immediately in this case for speed-ups!
	 * @param sc	RenderContext or InteractionContext, depending on traversal type
	 */
	public boolean onTransform(SceneGraphContext sc){
		// extension point. No code to execute in the basic class.
		return true;
	}
	
	/**
	 * Overwrite this to make some actual rendering happening!
	 * @return	Not used for now.
	 * @param renderContext	Contains the current state of the render environment
	 */
	public boolean onRender(RenderContext renderContext) {
		// extension point. No code to execute in the basic class.
		return true;
	}

	/**
	 * Overwrite this to react to user interaction. When this returns true,
	 * the interaction is then locked to this node, no other nodes will then
	 * be checked to react on this interaction incident.
	 * @param interactionContext	Contains the current state of the render environment
	 */
	protected boolean onInteraction(InteractionContext interactionContext) {
		return false;
	}

	/**
	 * Internal method. Override onSurfaceCreated instead!
	 * @param renderContext 
	 */
	public final void surfaceCreated(RenderContext renderContext){
		if (renderContext.surfaceId == lastCreatedSurfaceId) return;
		recreateVbo();
		onSurfaceCreated(renderContext);
		onSurfaceChanged(renderContext);
		lastCreatedSurfaceId = renderContext.surfaceId;
	}
	
	private void recreateVbo() {
		if (vbo != null) vbo.create();
	}

	/**
	 * Called when the GL surface was (re-)created. A good place to reserve memory
	 * and create buffers and textures. No need to call the super-method.
	 * @param renderContext 
	 */
	protected void onSurfaceCreated(RenderContext renderContext) {
		// extension point. No code to execute in the basic class.
	}

	/**
	 * Internal method. Override onSurfaceChanged!
	 */
	public final void surfaceChanged(RenderContext renderContext) {
		onSurfaceChanged(renderContext);
	}

	/**
	 * This is called when the surface changed
	 * @param renderContext	contains information about the surface changes
	 */
	public void onSurfaceChanged(RenderContext renderContext){
		// extension point. No code to execute in the basic class.
	}

	public final ArrayList<Node> getChildren() {
		return children;
	}
	
	protected final void attach(SceneGraph sg){
		if (attached) return;
		this.sceneGraph = sg;
		attached = true;
		onAttached();
		for (Node child : children) {
			child.attach(sg);
		}
	}
	
	/**
	 * This is called when the Node was added to the Scene Graph. The node will thus be 
	 * included in the next rendering cycle. This is a good place to register listeners.
	 */
	public void onAttached() {
		// extension point. No code to execute in the basic class.
	}

	protected final void detach(){
		if (!attached) return;
		attached = false;
		onDetach();
		for (Node child : children) {
			child.detach();
		}
	}

	/**
	 * This is called when the node was removed from the Scene Graph or when the whole rendering 
	 * surface was paused (e.g., when moved to the background). This is the best spot to remove 
	 * any listeners. From here, the node may re-enter the the "onAttach" state 
	 * (e.g., when the rendering view is moved in the foreground again). It will also always 
	 * be called before "onDestroy"
	 */
	public void onDetach() {
		// extension point. No code to execute in the basic class.
	}
	
	protected final void destroy(){
		onDestroy();
		for (Node child : children) {
			child.destroy();
		}
	}

	/**
	 * This will be called when the Rendering Context is destroyed. After this, all references to 
	 * this Node *must* be removed to make sure that the garbage collector can collect the node. 
	 * This is the end-point of the Node's lifecycle, it will not be used again.
	 */
	public void onDestroy() {
		// extension point. No code to execute in the basic class.
	}

	/**
	 * Used for visibility and culling optimizations. All nodes will be sorted also according
	 * to their z value to also ensure correct transparent drawings. The positive z-axis points
	 * here into the scene, the eye point has thus mostly a negative z value. <br><br>
	 * In simple circumstances, the node will just have a static z-value that is returned here. 
	 */
	public int getZLevel() {
		return zLevel;
	}

	public void setRenderProgramIndex(int renderProgramIndex) {
		this.renderProgramIndex = renderProgramIndex;
	}

	public int getRenderProgramIndex() {
		return renderProgramIndex;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
