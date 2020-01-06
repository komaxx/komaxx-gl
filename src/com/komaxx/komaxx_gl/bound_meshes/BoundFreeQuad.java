package com.komaxx.komaxx_gl.bound_meshes;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.RectF;
import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.math.GlRect;
import com.komaxx.komaxx_gl.math.Vector;
import com.komaxx.komaxx_gl.primitives.TexturedQuad;
import com.komaxx.komaxx_gl.primitives.TexturedVertex;

/**
 * This is a simplification of the BoundFreeMesh. It has always four vertices.
 * 
 * @author Matthias Schicker
 */
public class BoundFreeQuad extends ABoundMesh {
	public static final byte VERTEX_UPPER_LEFT = 0;
	public static final byte VERTEX_LOWER_LEFT = 1;
	public static final byte VERTEX_LOWER_RIGHT = 2;
	public static final byte VERTEX_UPPER_RIGHT = 3;
	
	protected final TexturedVertex[] vertices;
	
	protected boolean positionDirty = true;
	protected boolean texCoordsDirty = true;

	protected final FloatBuffer vertexBuffer;
	
	public BoundFreeQuad(){
		vertices = new TexturedVertex[TexturedQuad.VERTEX_COUNT];
		for (int i = 0; i < TexturedQuad.VERTEX_COUNT; i++) vertices[i] = new TexturedVertex();
		
		vertexBuffer = TexturedQuad.allocateQuads(1);
		indexBuffer = TexturedQuad.allocateQuadIndexArray(1);
	}
	
    public void setTexCoordsUv(byte vertex, float u, float v) {
		vertices[vertex].setUvCoords(u, v);
		texCoordsDirty = true;
	}

    public void setTexCoordsUv(RectF r) {
		vertices[VERTEX_UPPER_LEFT].setUvCoords(r.left, r.top);
		vertices[VERTEX_UPPER_RIGHT].setUvCoords(r.right, r.top);
		vertices[VERTEX_LOWER_LEFT].setUvCoords(r.left, r.bottom);
		vertices[VERTEX_LOWER_RIGHT].setUvCoords(r.right, r.bottom);
		
		texCoordsDirty = true;
	}

    
	public void positionXY(byte vertex, float x, float y) {
		vertices[vertex].setPositionXY(x, y);
		positionDirty = true;
	}

	public void positionXYZ(byte vertex, float x, float y, float z) {
		vertices[vertex].setPosition(x, y, z);
		positionDirty = true;
	}

	/**
	 * Use this to place the quad along a vector.
	 */
	private static float[] tmpVector1 = new float[4]; 
	private static float[] tmpVector2 = new float[4]; 
	public void positionAlong2(float[] a, float[] b, float thickness){
		float[] aToB = Vector.aToB2(tmpVector1, a, b);
		float[] normal = Vector.normal2(tmpVector2, aToB);
		Vector.normalize2(normal);
		Vector.scalarMultiply2(normal, thickness*0.5f);
		
		Vector.aPlusB2(vertices[VERTEX_UPPER_LEFT].getPosition(), a, normal);
		Vector.aMinusB2(vertices[VERTEX_UPPER_RIGHT].getPosition(), a, normal);

		Vector.aPlusB2(vertices[VERTEX_LOWER_LEFT].getPosition(), b, normal);
		Vector.aMinusB2(vertices[VERTEX_LOWER_RIGHT].getPosition(), b, normal);
		
		positionDirty = true;
	}
	
	@Override
	public int render(RenderContext rc, ShortBuffer frameIndexBuffer){
		if (!visible) return 0;
		
		boolean vboDirty = false;
		
		if (positionDirty){
			int offset = 0;
			for (int i = 0; i < 4; i++){
				// upper left vertex
				vertices[i].writePosition(vertexBuffer, offset);
				offset += TexturedVertex.STRIDE_FLOATS;
			}
			positionDirty = false;
			vboDirty = true;
		}
		
		if (texCoordsDirty){
			int offset = 0;
			for (int i = 0; i < 4; i++){
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
		return 6;
	}

	@Override
	public int getMaxVertexCount() {
		return TexturedQuad.VERTEX_COUNT;
	}

	@Override
	public int getMaxIndexCount() {
		return TexturedQuad.INDICES_COUNT;
	}

	public boolean contains(float[] xy) {
		return getBoundingBox().contains(xy) 
				//&& fineContains(xy)
				;
	}

	private static GlRect tmpBoundingBox = new GlRect();
	private GlRect getBoundingBox() {
		float[] position = vertices[0].getPosition();
		tmpBoundingBox.set(position[0], position[1], position[0], position[1]);
		tmpBoundingBox.enlarge(vertices[1].getPosition());
		tmpBoundingBox.enlarge(vertices[2].getPosition());
		tmpBoundingBox.enlarge(vertices[3].getPosition());
		return tmpBoundingBox;
	}
}
