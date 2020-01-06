package com.komaxx.komaxx_gl.bound_meshes;

import java.nio.ShortBuffer;

import com.komaxx.komaxx_gl.RenderContext;

/**
 * Dummy implementation of the IBoundMesh. Will draw nothing.
 * 
 * @author Matthias Schicker
 */
public class BoundDummy extends ABoundMesh {
	@Override
	public int render(RenderContext rc, ShortBuffer frameIndexBuffer) {
		return 0;
	}

	@Override
	public int getMaxVertexCount() {
		return 0;
	}

	@Override
	public int getMaxIndexCount() {
		return 0;
	}
}
