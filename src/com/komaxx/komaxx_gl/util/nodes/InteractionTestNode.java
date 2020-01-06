package com.komaxx.komaxx_gl.util.nodes;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.Matrix;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.math.GlRect;
import com.komaxx.komaxx_gl.math.Vector;
import com.komaxx.komaxx_gl.primitives.ColorQuad;
import com.komaxx.komaxx_gl.scenegraph.ARenderProgramStore;
import com.komaxx.komaxx_gl.scenegraph.Node;
import com.komaxx.komaxx_gl.scenegraph.interaction.InteractionContext;
import com.komaxx.komaxx_gl.scenegraph.interaction.Pointer;
import com.komaxx.komaxx_gl.util.KoLog;
import com.komaxx.komaxx_gl.util.ObjectsStore;
import com.komaxx.komaxx_gl.util.RenderUtil;

/**
 * A node that helps debugging interaction. It simply paints
 * the given (transformed) input points in its local coordinate
 * system. That way, you can see whether interaction transformations
 * before were faulty.
 * 
 * @author Matthias Schicker
 */
public class InteractionTestNode extends Node{
	private static final int INTERACTION_QUADS = 10;
	
	private FloatBuffer quadsData;
	private ShortBuffer quadsIndices;
	private final float centerDistance;

	private final float interactionStepWidth;
	
	private GlRect testRect = new GlRect(300, -300, 600, -600);
	
	
	public InteractionTestNode(float centerDistance, float interactionStepWidth){
		this.centerDistance = centerDistance;
		this.interactionStepWidth = interactionStepWidth;
		draws = true;
		blending = ACTIVATE;
		useVboPainting = false;
		handlesInteraction = true;
		zLevel = 1;
	}
	
	@Override
	public void onSurfaceCreated(RenderContext renderContext) {
		recreateQuads(renderContext);
		setRenderProgramIndex(ARenderProgramStore.SIMPLE_COLORED);
	}
	
	@Override
	protected boolean onInteraction(InteractionContext interactionContext) {
		Pointer firstPointer = interactionContext.getPointers()[0];
		
		if (firstPointer.isActive()){
			float[] rayPoint = firstPointer.getRayPoint();
			float x = rayPoint[0];
			float y = rayPoint[1];
			float z = rayPoint[2];
			
			
			KoLog.i(this, "interaction at " + 
					firstPointer.getScreenCoords()[0] + "x" + firstPointer.getScreenCoords()[1]
					);
			// //////////////////////////////////////////////////////////////
			// reproject for debug
			float[] vector = new float[]{ x, y, z, 1 };
			float[] projectedResult = new float[4];
			float[] mvpMatrix = interactionContext.getMvpMatrix();
			Matrix.multiplyMV(projectedResult, 0, mvpMatrix, 0, vector, 0);
			Vector.normalize4(projectedResult);
			projectedResult[0] *= interactionContext.surfaceWidth;
			projectedResult[1] *= interactionContext.surfaceHeight;
			KoLog.e(this, "1. mvp: " + Vector.toString(vector) + " -> " +
					Vector.toString(projectedResult));
			// //////////////////////////////////////////////////////////////
			float[] peek = interactionContext.modelMatrixStack.peek();
			Matrix.multiplyMV(projectedResult, 0, peek, 0, vector, 0);
			KoLog.e(this, "2. MV : " + Vector.toString(vector) + " -> " +
					Vector.toString(  Vector.normalize4(  projectedResult)  ));
			// //////////////////////////////////////////////////////////////
			
//		ColorQuad.position(quadsData, 12 * ColorQuad.COLOR_QUAD_FLOATS, 
//				x-12.5f, y+12.5f, z, x, y-12.5f, z);
			ColorQuad.positionXY(quadsData, 10 * ColorQuad.COLOR_QUAD_FLOATS, testRect);
			
			float interactionX = x;
			float interactionY = y;
			
			// the visual ray slope
			float[] eyePoint = interactionContext.eyePointStack.peek();
			float[] raySlope = Vector.aToB3(
					new float[4], 
					new float[]{x,y,z,1}, 
					eyePoint);
			
			// find the zero point: eyePoint + ß*raySlope =! (?,?,0)
			// <=> eyePoint.z + ß*raySlope.z = 0
			// <=> ß = -eyePoint.z / raySlope.z
			
			float gamma = -eyePoint[2] / raySlope[2];
			
			interactionX = eyePoint[0] + gamma * raySlope[0];
			interactionY = eyePoint[1] + gamma * raySlope[1];
			float interactionZ = eyePoint[2] + gamma * raySlope[2];
			
			ColorQuad.position(quadsData, 13 * ColorQuad.COLOR_QUAD_FLOATS, 
					interactionX, interactionY+12.5f, interactionZ,
					interactionX+12.5f, interactionY-12.5f, interactionZ);
			
			
			if (testRect.contains(interactionX, interactionY)){
				ColorQuad.color(quadsData, 10 * ColorQuad.COLOR_QUAD_FLOATS, 1, 1, 0, 1);
			} else {
				ColorQuad.color(quadsData, 10 * ColorQuad.COLOR_QUAD_FLOATS, 0.2f, 0.2f, 0.6f, 1);
			}
		}
		

		return false;
	}
	
	/**
	 * @param renderContext  
	 */
	private void recreateQuads(RenderContext renderContext){
		quadsData = ColorQuad.allocateColorQuads(8 + INTERACTION_QUADS);
		quadsIndices = ColorQuad.allocateQuadIndices(8 + INTERACTION_QUADS);
		
		float size = 3f;
		
//		float centerDistanceX = renderContext.surfaceWidth/4;
//		float centerDistanceY = renderContext.surfaceHeight/4;

		float centerDistanceX = 10;
		float centerDistanceY = 10;

		
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

		float[] resultPx = ObjectsStore.getVector();
		renderContext.worldToScreen(resultPx, 
				quadsData.get(13 * ColorQuad.COLOR_QUAD_FLOATS),
				quadsData.get(13 * ColorQuad.COLOR_QUAD_FLOATS + 1),
				0);
		KoLog.e(this, Vector.toString(resultPx, 3));
		ObjectsStore.recycleVector(resultPx);
		
		return true;
	}
}
