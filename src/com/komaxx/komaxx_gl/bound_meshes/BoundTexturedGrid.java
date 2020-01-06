package com.komaxx.komaxx_gl.bound_meshes;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.RectF;
import android.opengl.GLES20;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.math.GlRect;
import com.komaxx.komaxx_gl.primitives.Vertex;

public class BoundTexturedGrid extends ABoundMesh {
	private final int vCols;
	private final int vRows;
	private final int verticesCount;
	
	private GlRect position = new GlRect();
	private boolean positionDirty = true;

	private RectF uvBounds = new RectF(0,0,1,1);
	private boolean uvBoundsDirty = true;
	
	private FloatBuffer vertexBuffer;
	
	// tiles: (r-1)*(c-1)
	// triangles: 2*tiles = 2* (r-1)*(c-1)
	// indices : 3*triangles = 6 * (r-1)*(c-1)

	// /////////////////////////////////////////////
	// caches, short use stuff
	private final float[] tmpX;
	private final float[] tmpY;
	
	
	public BoundTexturedGrid(int vCols, int vRows){
		this.vCols = vCols;
		this.vRows = vRows;
		this.verticesCount = vCols * vRows;
		
		tmpX = new float[vCols];
		tmpY = new float[vRows];
		
		vertexBuffer = Vertex.allocateColorVertices(vRows * vCols);
		
		createIndices();
	}
	
	private void createIndices() {
		indexBuffer = new short[6 * (vRows-1) * (vCols-1)];
		setIndices();
	}

	public static final int getIndexCount(int vColumns, int vRows){
		return 6 * (vRows-1) * (vColumns-1);
	}
	
	private void setIndices() {
		int index = 0;
		for (short r = 0; r < (vRows-1); r++){
			for (short c = 0; c < (vCols-1); c++){
				// first triangle
				indexBuffer[index++] = (short)( (r*vCols) + c);
				indexBuffer[index++] = (short)( ((r+1)*vCols) + c);
				indexBuffer[index++] = (short)( (r*vCols) + c + 1);
				// second triangle
				indexBuffer[index++] = (short)( (r*vCols) + c + 1);
				indexBuffer[index++] = (short)( ((r+1)*vCols) + c);
				indexBuffer[index++] = (short)( ((r+1)*vCols) + c + 1);
			}
		}
	}
	
	public short[] getIndices() {
		return indexBuffer;
	}

	public int getVboVertexIndex(){
		return firstVertexIndex;
	}
	
	public void positionXY(float left, float top, float right, float bottom) {
		position.set(left, top, right, bottom);
		positionDirty = true;
	}
	
	public void setUvBounds(RectF nuUvBounds){
		uvBounds.set(nuUvBounds);
		uvBoundsDirty = true;
	}
	
	public void setUvBounds(float ulU, float ulV, float lrU, float lrV){
		uvBounds.set(ulU, ulV, lrU, lrV);
		uvBoundsDirty = true;
	}

	@Override
	public int render(RenderContext rc, ShortBuffer frameIndexBuffer){
		if (!visible) return 0;
		
		boolean vboDirty = false;
		
		if (positionDirty){
			applyPosition();
			positionDirty = false;
			vboDirty = true;
		}
		
		if (uvBoundsDirty){
			applyUvBounds();
			uvBoundsDirty = false;
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
		return indexBuffer.length;
	}

	private void applyUvBounds() {
		int offset = Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET;
		float[] tmpUvCoords = new float[]{ uvBounds.left, uvBounds.top };
		float uDelta = uvBounds.width() / vCols;
		float vDelta = uvBounds.height() / vRows;
		for (int y = 0; y < vRows; y++){
			tmpUvCoords[0] = uvBounds.left;
			for (int x = 0; x < vCols; x++){
				vertexBuffer.position(offset);
				vertexBuffer.put(tmpUvCoords);
				offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
				
				tmpUvCoords[0] += uDelta;
			}
			tmpUvCoords[1] += vDelta;
		}
	}

	private void applyPosition() {
		// precompute x-Values
		tmpX[0] = position.left;
		int tiles = vCols-1;
		float delta = position.width() / (float)tiles;
		
		float now = position.left;
		for(int i = 1; i < tiles; i++){
			now += delta;
			tmpX[i] = now;
		}
		tmpX[tiles] = position.right;
		
		// precompute yValues
		now = tmpY[0] = position.top;
		tiles = vRows-1;
		delta = position.height() / (float)tiles;
		
		for(int i = 1; i < tiles; i++){
			now -= delta;
			tmpY[i] = now;
		}
		tmpY[tiles] = position.bottom;

		// and assign to the vertexBuffer
		int offset = 0;
		for (int y = 0; y < vRows; y++){
			for (int x = 0; x < vCols; x++){
				vertexBuffer.position(offset);
				vertexBuffer.put(tmpX[x]);
				vertexBuffer.put(tmpY[y]);
				vertexBuffer.put(0);
				offset += Vertex.COLOR_VERTEX_DATA_STRIDE_FLOATS;
			}
		}
	}

	public boolean contains(float x, float y) {
		return position.contains(x, y);
	}

	@Override
	public int getMaxVertexCount() {
		return verticesCount;
	}

	@Override
	public int getMaxIndexCount() {
		return indexBuffer.length;
	}
}
