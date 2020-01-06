package com.komaxx.komaxx_gl.scenegraph.basic_nodes;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.SceneGraphContext;
import com.komaxx.komaxx_gl.scenegraph.Node;

public class RootNode extends Node {
	public RootNode(){
		setName("ROOT");
		zLevel = Integer.MAX_VALUE - 1;
	}
	
	@Override
	protected void applyStateChangeRendering(RenderContext renderContext) {
		// do nothing!
	}
	
	@Override
	protected void applyStateChangeTransform(SceneGraphContext scContext) {
		// do nothing!
	}
}
