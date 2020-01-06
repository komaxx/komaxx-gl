package com.komaxx.komaxx_gl.traversing;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.scenegraph.Node;

/**
 * Used to visit the SceneGraph when the surface was created (newly).
 * 
 * @author Matthias Schicker
 */
public class SurfaceCreatedVisitor implements ISceneGraphVisitor {
	private RenderContext renderContext;

	@Override
	public boolean visitNode(Node node) {
		node.surfaceCreated(renderContext);
		return true;
	}

	public void setRenderContext(RenderContext frameRenderContext) {
		this.renderContext = frameRenderContext;
	}

}
