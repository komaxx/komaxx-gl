package com.komaxx.komaxx_gl.scenegraph.basic_nodes;

import android.graphics.Rect;

import com.komaxx.komaxx_gl.SceneGraphContext;
import com.komaxx.komaxx_gl.scenegraph.Node;

/**
 * Simple node that intersects the scissor rect of the current context with
 * its own scissor Rect.
 * 
 * @author Matthias Schicker
 */
public class ScissorNode extends Node {
	protected Rect scissorRect = new Rect();
	
	public ScissorNode(){
		draws = false;
		transforms = true;
		scissorTest = ACTIVATE;
	}
	
	@Override
	public boolean onTransform(SceneGraphContext sc) {
		Rect push = sc.scissorStack.push();
		boolean ret = push.intersect(scissorRect);
		sc.setScissorRectDirty();
		
//		AqLog.e(this, sc.frame +"("+(sc instanceof RenderContext ? "R" : "I")+"): " + sc.eyePoint[0]);
		
		return ret;
	}
	
	@Override
	public void onUnTransform(SceneGraphContext sc) {
		sc.scissorStack.pop();
		sc.setScissorRectDirty();
	}

	public void setScissorRect(int left, int top, int right, int bottom) {
		scissorRect.set(left, top, right, bottom);
	}
}
