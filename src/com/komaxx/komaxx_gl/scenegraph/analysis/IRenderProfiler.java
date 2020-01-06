package com.komaxx.komaxx_gl.scenegraph.analysis;

import com.komaxx.komaxx_gl.RenderContext;

public interface IRenderProfiler {

	void frameStart();

	void frameDone(RenderContext renderContext);

	void globalRunnablesStart();

	void globalRunnablesDone();

	void startPath(Path path);

	void pathDone(Path path);

}