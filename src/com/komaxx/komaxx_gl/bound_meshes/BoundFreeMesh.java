package com.komaxx.komaxx_gl.bound_meshes;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.primitives.TexturedVertex;


public class BoundFreeMesh extends ABoundMesh {
	protected final int vertexCount;
	protected final int indexCount;
	
	protected final TexturedVertex[] vertices;
	
	protected boolean positionDirty = true;
	protected boolean texCoordsDirty = true;
	protected boolean alphaDirty = true;

	protected final FloatBuffer vertexBuffer;

	public BoundFreeMesh(int vertexCount, int indexCount){
		this.vertexCount = vertexCount;
		this.indexCount = indexCount;
		
		vertices = new TexturedVertex[vertexCount];
		for (int i = 0; i < vertexCount; i++) vertices[i] = new TexturedVertex();
		
		vertexBuffer = TexturedVertex.allocate(vertexCount);
		indexBuffer = new short[indexCount];
	}
	
	public void setIndices(short[] indices){
		for (int i = 0; i < indexCount; i++){
			indexBuffer[i] = indices[i];
		}
	}

    public void setTexCoordsUv(int index, float u, float v) {
		vertices[index].setUvCoords(u, v);
		texCoordsDirty = true;
	}
	
	public void positionXY(int index, float x, float y) {
		vertices[index].setPositionXY(x, y);
		positionDirty = true;
	}

	public void positionXY(int index, float[] pos) {
		vertices[index].setPositionXY(pos);
		positionDirty = true;
	}
	
	public void positionXYZ(int index, float x, float y, float z) {
		vertices[index].setPosition(x, y, z);
		positionDirty = true;
	}

	@Override
	public int render(RenderContext rc, ShortBuffer frameIndexBuffer){
		if (!visible) return 0;
		
		boolean vboDirty = false;
		
		if (positionDirty){
			int offset = 0;
			for (int i = 0; i < vertexCount; i++){
				// upper left vertex
				vertices[i].writePosition(vertexBuffer, offset);
				offset += TexturedVertex.STRIDE_FLOATS;
			}
			positionDirty = false;
			vboDirty = true;
		}
		
		if (alphaDirty){
			int offset = 0;
			for (int i = 0; i < vertexCount; i++){
				// upper left vertex
				vertices[i].writeAlpha(vertexBuffer, offset);
				offset += TexturedVertex.STRIDE_FLOATS;
			}
			alphaDirty = false;
			vboDirty = true;
		}
		
		if (texCoordsDirty){
			int offset = 0;
			for (int i = 0; i < vertexCount; i++){
				// upper left vertex
				vertices[i].writeUvCoords(vertexBuffer, offset);
				offset += TexturedVertex.STRIDE_FLOATS;
			}
			texCoordsDirty = false;
			vboDirty = true;
		}
		
		if (vboDirty){
			vertexBuffer.position(0);
			GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 
					firstByteIndex, 
					vertexBuffer.capacity()*4, 
					vertexBuffer);
			
			vboDirty = false;
		}
		
		frameIndexBuffer.put(indexBuffer);
		return indexCount;
	}

	@Override
	public int getMaxVertexCount() {
		return vertexCount;
	}

	@Override
	public int getMaxIndexCount() {
		return indexCount;
	}

	public void setAlpha(int index, float nuAlpha) {
		vertices[index].setAlpha(nuAlpha);
		alphaDirty = true;
	}
}
