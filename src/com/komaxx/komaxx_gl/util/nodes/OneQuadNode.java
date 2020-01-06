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
 * A node contains just one colored quad and places it randomly
 * in a square field. Useful for benchmarks.
 * 
 * @author Matthias Schicker
 */
public class OneQuadNode extends Node {
		private FloatBuffer quadsData;
		private ShortBuffer quadsIndices;
		
		private final float size;
		
		public OneQuadNode(float size){
			this.size = size;
			this.draws = true;
			this.renderProgramIndex = ARenderProgramStore.SIMPLE_COLORED;
			this.blending = DEACTIVATE;
			this.transforms = false;
			this.useVboPainting = false;
			
			this.clusterIndex = 5;
		}
		
		@Override
		public void onSurfaceChanged(RenderContext renderContext) {
			recreateQuad();
		}
		
		private void recreateQuad(){
			quadsData = ColorQuad.allocateColorQuads(1);
			quadsIndices = ColorQuad.allocateQuadIndices(1);
			

			float x = (float) (Math.random() * 100f * size);
			float y = (float) (Math.random() * 100f * size);
			
			ColorQuad.position(quadsData, 0, x, y, 0, x+size, y-size, 0);
			
			int randomColor = RenderUtil.getRandomColor();
			float[] color = RenderUtil.color2floatsRGBA(null, randomColor);
			
			ColorQuad.color(quadsData, 0, color[0], color[1], color[2], color[3]);
		}
		
		@Override
		public boolean onRender(RenderContext renderContext) {
			RenderProgram currentRenderProgram = renderContext.currentRenderProgram;

	        ColorQuad.renderColored(currentRenderProgram, 0, 1, quadsData, quadsIndices);

			return true;
		}
	}
