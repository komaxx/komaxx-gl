package com.komaxx.komaxx_gl.bound_meshes;

import com.komaxx.komaxx_gl.primitives.Vertex;
import com.komaxx.komaxx_gl.util.KoLog;

public abstract class ABoundMesh implements IBoundMesh {
	protected Vbo boundVbo = null;
	protected int bytesPerVertex = Vertex.TEXTURED_VERTEX_DATA_STRIDE_BYTES;

	protected boolean visible = true;
	protected int firstByteIndex;
	protected int firstVertexIndex;
	
	protected short[] indexBuffer;

	public ABoundMesh(){
	}

	@Override
	public void bindToVbo(Vbo vbo) {
		if (vbo.getBytesPerVertex() != this.bytesPerVertex){
			KoLog.w(this, "VBO and mesh vertex formats do not match!");
		}
		
		boundVbo = vbo;
		boolean success = vbo.bind(this);
		if (!success){
			KoLog.e(this, "Could not bind to VBO!");
		}
	}

	@Override
	public void unbind() {
		boundVbo.unbind(this);
	}

	@Override
	public int getFirstIndex() {
		return firstVertexIndex;
	}
	
	@Override
	public final void setFirstIndex(int firstFreeIndex) {
		firstVertexIndex = firstFreeIndex;
		firstByteIndex = firstFreeIndex*bytesPerVertex;

		offsetIndexBuffer();
	}
	
	/**
	 * Called when the first index was set.
	 */
	protected void offsetIndexBuffer(){
		int l = indexBuffer.length;
		for(int i = 0; i < l; i++){
			indexBuffer[i] += firstVertexIndex;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	@Override
	public boolean isVisible() {
		return visible;
	}
}
