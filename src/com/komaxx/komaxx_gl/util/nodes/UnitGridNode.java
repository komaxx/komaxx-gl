package com.komaxx.komaxx_gl.util.nodes;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.primitives.ColorQuad;
import com.komaxx.komaxx_gl.scenegraph.ARenderProgramStore;
import com.komaxx.komaxx_gl.scenegraph.Node;
import com.komaxx.komaxx_gl.util.RenderUtil;

/**
 * A node that contains random quads in the 0-plane.
 * 
 * @author Matthias Schicker
 */
public class UnitGridNode extends Node{
	private static final int INTERACTION_QUADS = 10;
	
	private FloatBuffer quadsData;
	private ShortBuffer quadsIndices;
	private final float centerDistance;

	private final float interactionStepWidth;
	
	public UnitGridNode(float centerDistance, float interactionStepWidth){
		this.centerDistance = centerDistance;
		this.interactionStepWidth = interactionStepWidth;
		draws = true;
		blending = ACTIVATE;
		useVboPainting = false;
		handlesInteraction = false;
		zLevel = 500;
	}
	
	@Override
	public void onSurfaceChanged(RenderContext renderContext) {
		recreateQuads(renderContext);
		setRenderProgramIndex(ARenderProgramStore.SIMPLE_COLORED);
	}
	
	private void recreateQuads(RenderContext renderContext){
		quadsData = ColorQuad.allocateColorQuads(8 + INTERACTION_QUADS);
		quadsIndices = ColorQuad.allocateQuadIndices(8 + INTERACTION_QUADS);
		
		float size = 1f;
		
		float centerDistanceX = renderContext.surfaceWidth/4;
		float centerDistanceY = renderContext.surfaceHeight/4;
		
		// top horizontal: red
		ColorQuad.position(quadsData, 0 * ColorQuad.COLOR_QUAD_FLOATS, 
				-10000, centerDistanceY + size/2, 0, 10000, centerDistanceY-size/2, 0);
		ColorQuad.color(quadsData, 0 * ColorQuad.COLOR_QUAD_FLOATS, 1, 0, 0, 1);
		
		// bottom horizontal: green
		ColorQuad.position(quadsData, 1 * ColorQuad.COLOR_QUAD_FLOATS, 
				-10000, -centerDistanceY + size/2, 0, 10000, -centerDistanceY-size/2, 0);
		ColorQuad.color(quadsData, 1 * ColorQuad.COLOR_QUAD_FLOATS, 0, 1, 0, 1);
		
		// left vertical: blue
		ColorQuad.position(quadsData, 2 * ColorQuad.COLOR_QUAD_FLOATS, 
				-centerDistanceX - size/2, 10000, 0, -centerDistanceX + size/2, -10000, 0);
		ColorQuad.color(quadsData, 2 * ColorQuad.COLOR_QUAD_FLOATS, 0, 0, 1, 1);
		
		// right vertical: cyan
		ColorQuad.position(quadsData, 3 * ColorQuad.COLOR_QUAD_FLOATS, 
				centerDistanceX - size/2, 10000, 0, centerDistanceX + size/2, -10000, 0);
		ColorQuad.color(quadsData, 3 * ColorQuad.COLOR_QUAD_FLOATS, 0, 1, 1, 1);
		
		
		float z = interactionStepWidth;
		ColorQuad.position(quadsData, 4 * ColorQuad.COLOR_QUAD_FLOATS,
				-10000, centerDistance + size/2, z, 10000, centerDistance-size/2, z);
		ColorQuad.color(quadsData, 4 * ColorQuad.COLOR_QUAD_FLOATS, 0.4f, 0, 0, 1);
		
		ColorQuad.position(quadsData, 5 * ColorQuad.COLOR_QUAD_FLOATS, 
				-10000, -centerDistance + size/2, z, 10000, -centerDistance-size/2, z);
		ColorQuad.color(quadsData, 5 * ColorQuad.COLOR_QUAD_FLOATS, 0, 0.4f, 0, 1);
		
		ColorQuad.position(quadsData, 6 * ColorQuad.COLOR_QUAD_FLOATS, 
				-centerDistance - size/2, 10000, z, -centerDistance + size/2, -10000, z);
		ColorQuad.color(quadsData, 6 * ColorQuad.COLOR_QUAD_FLOATS, 0, 0, 0.4f, 1);
		
		ColorQuad.position(quadsData, 7 * ColorQuad.COLOR_QUAD_FLOATS, 
				centerDistance - size/2, 10000, z, centerDistance + size/2, -10000, z);
		ColorQuad.color(quadsData, 7 * ColorQuad.COLOR_QUAD_FLOATS, 0, 0.4f, 0.4f, 1);

	
		// interaction pos
		float[] color = new float[4];
		color[3] = 1;
		for (int i = 8; i < 8+INTERACTION_QUADS; i++){
			RenderUtil.hsv2rgb(color, (i-8) * 20, 1, 1);
			
			ColorQuad.position(quadsData, i * ColorQuad.COLOR_QUAD_FLOATS, 0,0,0,0,0,0);
			ColorQuad.color(quadsData, i * ColorQuad.COLOR_QUAD_FLOATS, color);
		}
	}
	
	@Override
	public boolean onRender(RenderContext renderContext) {
		RenderProgram currentRenderProgram = renderContext.currentRenderProgram;
        ColorQuad.renderColored(currentRenderProgram, 0, 8 + INTERACTION_QUADS, quadsData, quadsIndices);

		return true;
	}
}
