package com.komaxx.komaxx_gl.primitives;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.komaxx.komaxx_gl.util.RenderUtil;

public class TexturedCircleSegment {
	/*
	 * 		1
	 * 		 2
	 * 0	 3
	 * 		 4
	 * 		5
	 */
	
	// trivial: triangle_count == segment_count
	
	public static int vertexCount(int segmentCount){
		return 
			1 + 					// the center hub
			segmentCount-1 +		// each two neighbor-segments share one vertex
			2;						// start and end vertex
		
	}
	
	public static final int indicesCount(int segmentCount){
		return segmentCount * 3;	// one triangle  per segment
	}

	public static ShortBuffer allocateIndices(int totalSegmentCount) {
		ShortBuffer ret = ByteBuffer.allocateDirect(
				indicesCount(totalSegmentCount) * RenderUtil.SHORT_SIZE_BYTES
                ).order(ByteOrder.nativeOrder()).asShortBuffer();
		
		return ret;
	}

	/**
	 * Creates an index array for one circle segment with <code>segments</code>
	 * parts
	 */
	public static short[] allocateIndexArray(int segments) {
		short[] ret = new short[indicesCount(segments)];
		
		int rimVertex = 1;
		for (int i = 0; i < segments; i++){
			ret[i*3] = 0;
			ret[i*3 + 1] = (short) rimVertex++;
			ret[i*3 + 2] = (short) rimVertex;
		}
		
		return ret;
	}

	/**
	 * Creates the vertexBuffer for *one* circle segment
	 */
	public static FloatBuffer allocate(int segments) {
		return Vertex.allocateTexturedVertices(vertexCount(segments));
	}
	
	public static final void position(FloatBuffer data, int offset, 
			float centerX, float centerY, float z,
			float radius, float startAngle, float endAngle, 
			int segments
	){
		// first: the central hub
		data.position(offset);
		data.put(centerX);
		data.put(centerY);
		data.put(z);
		offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;

		// now: the segment points
		float deltaAngle = (float) Math.toRadians( (endAngle-startAngle) / (float)segments );
		
		float currentAngle = (float) Math.toRadians( startAngle );
		for (int i = 0; i <= segments; i++){
			data.position(offset);
			data.put(centerX + (float) (Math.cos(currentAngle) * radius));
			data.put(centerY + (float) (Math.sin(currentAngle) * radius));
			data.put(z);

			offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
			currentAngle += deltaAngle;
		}
	}
	
	public static final void setAlpha(
			FloatBuffer data, int offset, float hubAlpha, float rimAlpha, int segments){
		
		offset += Vertex.TEXTURED_VERTEX_DATA_ALPHA_OFFSET;
		data.position(offset);
		data.put(hubAlpha);
		offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;

		for (int i = 0; i <= segments; i++){
			data.position(offset);
			data.put(rimAlpha);
			offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
		}
	}
	
	/**
	 * Simple mapping method that expects that the patches go exactly to the center of the map.
	 */
	public static final void setUVMapping(FloatBuffer data, int offset, 
			float hubU, float hubV, float rimU, float rimV, int segments){

		offset += Vertex.TEXTURED_VERTEX_DATA_UV_OFFSET;
		data.position(offset);
		data.put(hubU);
		data.put(hubV);
		offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;

		for (int i = 0; i <= segments; i++){
			data.position(offset);
			data.put(rimU);
			data.put(rimV);
			offset += Vertex.TEXTURED_VERTEX_DATA_STRIDE_FLOATS;
		}
	}
}
