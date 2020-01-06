package com.komaxx.komaxx_gl.scenegraph.analysis;

import com.komaxx.komaxx_gl.scenegraph.SceneGraph;

public interface ISceneGraphAnalysor {
	/**
	 * Delivers a linearization containing all drawing nodes and the paths between them.
	 */
	Linearization getRenderLinearization(SceneGraph sceneGraph);

	void setDirty();

	void onResume();

	void onPause();

	/**
	 * Delivers a linearization containing all nodes that handle interaction 
	 * and the paths between them.
	 */
	Linearization getInteractionLinearization(SceneGraph sceneGraph);

	/**
	 * Called, when the owning SceneGraph is definitely no longer used.
	 */
	void onDestroy();
}
