package com.komaxx.komaxx_gl.renderprograms;

import com.komaxx.komaxx_gl.RenderProgram;
import com.komaxx.komaxx_gl.scenegraph.ARenderProgramStore;

/**
 * A render-program store that contains all (and only) the inbuilt
 * RenderPrograms of the API.
 *  
 * @author Matthias Schicker
 */
public class DefaultRenderProgramStore extends ARenderProgramStore {
	@Override
	protected RenderProgram buildRenderProgram(int i) {
		// no additional programs!
		return null;
	}

	@Override
	protected int getAdditionalProgramsCount() {
		return 0;
	}
}