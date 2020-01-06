package com.komaxx.komaxx_gl;

import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.view.MotionEvent;

import com.komaxx.komaxx_gl.GlConfig.ColorDepth;
import com.komaxx.komaxx_gl.scenegraph.ARenderProgramStore;
import com.komaxx.komaxx_gl.scenegraph.SceneGraph;
import com.komaxx.komaxx_gl.texturing.Texture;
import com.komaxx.komaxx_gl.util.KoLog;

/**
 * This is a basic implementation of a Renderer that handles a single SceneGraph
 * and automatically issues SceneGraph traversals to recreate surfaces and draw.
 * 
 * @author Matthias Schicker
 */
public class BasicSceneGraphRenderView extends GLSurfaceView implements Renderer {
//	private static final int LONG_CLICK_DELAY_MS = 500;
	
	private static final int MAX_EVENT_POOL_SIZE = 15;

	protected SceneGraph sceneGraph;
	
	private ArrayList<InteractionEventRunnable> interactionEventPool = new ArrayList<InteractionEventRunnable>();

	@SuppressWarnings("deprecation")
	public BasicSceneGraphRenderView(Context context, GlConfig glConfig, ARenderProgramStore renderProgramStore) {
		super(context);

		setPreserveGlContextIfPossible();
		
		setBackgroundDrawable(null);

		setEGLContextClientVersion(2);
		
		setEGLConfigChooser(new GlConfigChooser(glConfig));
		
		getHolder().setFormat(
				glConfig.colorDepth == ColorDepth.Color8888 ? 
						PixelFormat.RGBA_8888 : PixelFormat.RGB_565);
		Texture.setHighResolutionColor(glConfig.colorDepth==ColorDepth.Color8888);
		
		sceneGraph = new SceneGraph(context, context.getResources(), renderProgramStore);
		setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
	}
	
	private void setPreserveGlContextIfPossible() {
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO){
			Method[] declaredMethods = GLSurfaceView.class.getDeclaredMethods();
			for (Method m : declaredMethods){
				if (m.getName().equals("setPreserveEGLContextOnPause")){
					try {
						m.invoke(this, true);
						KoLog.v(this, "setPreserveEGLContextOnPause is set.");
					} catch (Exception ex) {
						KoLog.e(this, "Could not setPreserveEGLContextOnPause");
						ex.printStackTrace();
					}
				}
			}
		}
	}

	// /////////////////////////////////////////////////////////////////
	// renderer
	@Override
	public void onDrawFrame(GL10 gl) {
	    // one more yield to make the rendering as atomar as possible
//	    Thread.yield();			// no measurable gain in consistency. Eats frames.
		try {
			sceneGraph.render();
		} catch (Exception e){
			KoLog.e(this, "Exception in onDrawFrame: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		
//		GLES20.glFinish();		// supposedly avoids freezes in eglSwapBuffers. Inconclusive. Eats ~10 FPS!
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		sceneGraph.surfaceChanged(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		sceneGraph.surfaceCreated();
	}
	
	public SceneGraph getSceneGraph() {
		return sceneGraph;
	}
	
	@Override
	public void onResume() {
		sceneGraph.onResume();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		sceneGraph.onPause();
		super.onPause();
	}
	
	public void onDestroy() {
		sceneGraph.onDestroy();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Runnable eventFromPool = getEventFromPool(event);
		queueEvent(eventFromPool);

//		int action = event.getAction();
//		switch (action){
//		case MotionEvent.ACTION_UP:
//		case MotionEvent.ACTION_CANCEL:
//		case MotionEvent.ACTION_OUTSIDE:
//		case MotionEvent.ACTION_POINTER_DOWN:
//			// can no longer be a long-click
//			removeCallbacks(longClickTrigger);
//			break;
//		case MotionEvent.ACTION_DOWN:
//			postDelayed(longClickTrigger, LONG_CLICK_DELAY_MS);
//		}
		
		return true;
	}
//	private Runnable longClickTrigger = new Runnable() {
//		@Override
//		public void run() {
//			queueEvent(new Runnable() {
//				@Override
//				public void run() {
//					sceneGraph.handleLongClick();
//				}
//			});
//		}
//	};
	
	private Runnable getEventFromPool(MotionEvent me) {
		InteractionEventRunnable ret = null;
		synchronized (interactionEventPool){
			if (interactionEventPool.size() <= 0){
				ret = new InteractionEventRunnable();
			} else {
				ret = interactionEventPool.remove(interactionEventPool.size()-1);
			}
		}
		ret.setMotionEvent(MotionEvent.obtain(me));
		return ret;
	}
	
	private void recycleInteractionEvent(InteractionEventRunnable toRecycle){
		synchronized (interactionEventPool) {
			if (interactionEventPool.size() < MAX_EVENT_POOL_SIZE){
				interactionEventPool.add(toRecycle);
			}
			toRecycle.me.recycle();
			toRecycle.me = null;
		}
	}
	
	private class InteractionEventRunnable implements Runnable {
		private MotionEvent me;

		@Override
		public void run() {
			sceneGraph.handleInteraction(me);
			recycleInteractionEvent(this);
		}
		public void setMotionEvent(MotionEvent me) {
			this.me = me;
		}
		@Override
		public String toString() {
			int pointerCount = me.getPointerCount();
			StringBuffer ret = new StringBuffer(pointerCount + ": ");
			for (int i = 0; i < pointerCount; i++){
				ret.append(me.getX(i)).append('|').append(me.getY(i)).append(", ");
			}
			return ret.toString();
		}
	}
}
