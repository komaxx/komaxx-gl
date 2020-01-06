package com.komaxx.komaxx_gl.bound_meshes;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.primitives.ColoredVertex;
import com.komaxx.komaxx_gl.primitives.TexturedVertex;


public class BoundLine extends ABoundMesh {
	protected final ColoredVertex[] vertices;
	
	protected boolean positionDirty = true;
	protected boolean colorDirty = true;

    public static byte VERTEX1 = 0;
    public static byte VERTEX2 = 1;
	
	protected final FloatBuffer vertexBuffer;

	public BoundLine(){
		vertices = new ColoredVertex[2];
		for (int i = 0; i < 2; i++) vertices[i] = new ColoredVertex();
		
		vertexBuffer = TexturedVertex.allocate(2);
		indexBuffer = new short[2];
	}
	
    public void setColor(byte vertex, int color) {
		vertices[vertex].setColorRGBA(color);
		colorDirty = true;
	}

    public void setColorRGBA(byte vertex, float[] rgba) {
		vertices[vertex].setColorRGBA(rgba);
		colorDirty = true;
	}
    
	public void positionXY(byte vertex, float x, float y) {
		vertices[vertex].setPositionXY(x, y);
		positionDirty = true;
	}

	public void positionXY(byte vertex, float[] pos) {
		vertices[vertex].setPositionXY(pos);
		positionDirty = true;
	}
	
	public void positionXYZ(byte vertex, float x, float y, float z) {
		vertices[vertex].setPosition(x, y, z);
		positionDirty = true;
	}

	@Override
	public int render(RenderContext rc, ShortBuffer frameIndexBuffer){
		if (!visible) return 0;
		
		boolean vboDirty = false;
		
		if (positionDirty){
			int offset = 0;
			vertices[0].writePosition(vertexBuffer, offset);
			vertices[1].writePosition(vertexBuffer, offset);
			offset += 2*TexturedVertex.STRIDE_FLOATS;
			positionDirty = false;
			vboDirty = true;
		}
		
		if (colorDirty){
			int offset = 0;
			vertices[0].writeColor(vertexBuffer, offset);
			vertices[1].writeColor(vertexBuffer, offset);
			offset += 2*TexturedVertex.STRIDE_FLOATS;
			colorDirty = false;
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
		return 2;
	}

	@Override
	public int getMaxVertexCount() {
		return 2;
	}

	@Override
	public int getMaxIndexCount() {
		return 2;
	}
}
