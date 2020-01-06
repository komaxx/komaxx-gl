package com.komaxx.komaxx_gl.bound_meshes;

import java.nio.ShortBuffer;

import com.komaxx.komaxx_gl.RenderContext;

/**
 * Bound meshes are objects of some sort (in the easiest case, a triangle)
 * consisting of vertices. The vertices are part of a VBO (thus, the mesh is
 * <i>bound</i> to this object). The IBoundMesh is itself responsible to
 * update this vbo.
 * 
 * @author Matthias Schicker
 */
public interface IBoundMesh {
	/**
	 * Called when rendering the VBO. All updating and writing to the VBO should happen
	 * in here.
	 * 
	 * @param rc	the current RenderContext
	 * @param frameIndexBuffer	the buffer containing the indices in the VBO to be rendered.
	 * @return	the amount of vertices written to the frameIndexBuffer.
	 */
	int render(RenderContext rc, ShortBuffer frameIndexBuffer);
	
	/**
	 * Called to bind the IBoundMesh to a vbo.
	 */
	void bindToVbo(Vbo vbo);
	
	/**
	 * Frees the space taken in the VBO. Should always be called for dynamic VBOs!
	 */
	void unbind();

	/**
	 * @return	The number of vertices that this mesh will require in a VBO.
	 */
	int getMaxVertexCount();
	
	/**
	 * @return	The maximum number of indices needed during drawing.
	 */
	int getMaxIndexCount();
	
	/**
	 * Whether or not this mesh will be painted.
	 */
	boolean isVisible();
	
	void setVisible(boolean visible);

	/**
	 * Internal use only.
	 */
	void setFirstIndex(int firstFreeIndex);

	/**
	 * The index of the first vertex in the mesh.
	 */
	int getFirstIndex();
}
