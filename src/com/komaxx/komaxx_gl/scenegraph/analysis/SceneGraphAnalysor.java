package com.komaxx.komaxx_gl.scenegraph.analysis;

import com.komaxx.komaxx_gl.scenegraph.SceneGraph;
import com.komaxx.komaxx_gl.util.KoLog;
import com.komaxx.komaxx_gl.util.Semaphor;

/**
 * A SceneGraphAnalysis is created before rendering. The Analysis
 * contains somewhat static information on how the graph is to be rendered. E.g.,
 * it says, how many passes are necessary to render. 
 * 
 * It is recreated whenever anything in the graph changes in a way that the analysis
 * seems dirty. 
 *  
 * @author Matthias Schicker
 */
public class SceneGraphAnalysor implements ISceneGraphAnalysor {
	private static final boolean DEBUG = false;
	
	private int currentAnalysisID = 0;		// will be counted up when the scene changes and thus aborts older analysis 
	
	private int lastRenderJobIssuedID = -50;
	private int lastInteractionJobIssuedID = -50;
	
	private Semaphor noValidRenderLinearizationLock = new Semaphor();
	private Semaphor noValidInteractionLinearizationLock = new Semaphor();

	private Linearization renderLinearization;
	private Linearization interactionLinearization;
	
	@Override
	public Linearization getRenderLinearization(SceneGraph graph){
		synchronized (noValidRenderLinearizationLock) {
			if (currentAnalysisID < 0) return null;		// aborted/paused!

			long start = DEBUG ? System.currentTimeMillis() : 0;
			boolean reLinearized = false;
			
			while (lastRenderJobIssuedID != currentAnalysisID){
				if (DEBUG){
					reLinearized = true;
					KoLog.d(this, "No render linearization available. Firing new one for: " + currentAnalysisID);
				}
				
				// fire up a new analyzor!
				new Thread(
						new RenderAnalysisJob(currentAnalysisID, this, graph), "renderAnalysis_"+currentAnalysisID)
					.start();
				lastRenderJobIssuedID = currentAnalysisID;

				while (lastRenderJobIssuedID==currentAnalysisID && renderLinearization == null && currentAnalysisID>0){
					try {
						if (DEBUG){
							KoLog.d(this, "Waiting for linearization available: " + currentAnalysisID);
						}
						noValidRenderLinearizationLock.wait(250);
					} catch (InterruptedException e) {
						// don't care
					}
				}
			}
			
			if (reLinearized){
				KoLog.d(this, "Render Linearization complete, time: "+ (System.currentTimeMillis()-start)+" ms");
			}
			
			clearLinearizationForProfiling(renderLinearization);
			
			return renderLinearization;
		}
	}
	
	private static void clearLinearizationForProfiling(Linearization lin) {
		if (lin==null) return;
		int l = lin.paths.size();
		for (int i = 0; i < l; i++) lin.paths.get(i).clearProfilingData();
	}

	@Override
	public Linearization getInteractionLinearization(SceneGraph graph) {
		long start = DEBUG ? System.currentTimeMillis() : 0;
		boolean reLinearized = false;
		
		synchronized (noValidInteractionLinearizationLock) {
			if (lastInteractionJobIssuedID != currentAnalysisID){
				if (DEBUG) reLinearized = true;
				
				// fire up a new analyzor!
				new Thread(
						new InteractionAnalysisJob(currentAnalysisID, this, graph), "interactionAnalysis_"+currentAnalysisID)
				.start();
				lastInteractionJobIssuedID = currentAnalysisID;
			}
			
			while (interactionLinearization == null && currentAnalysisID > -1){
				try {
					noValidInteractionLinearizationLock.wait();
				} catch (InterruptedException e) {
					// don't care
				}
			}
			
			if (reLinearized){
				KoLog.d(this, "Interaction Linearization complete, time: "+ (System.currentTimeMillis()-start)+" ms");
			}

			return interactionLinearization;
		}
	}
	

	/**
	 * Makes the current analysis invalid, thus triggers a re-analysis in the
	 * next rendering cycle.
	 */
	@Override
	public void setDirty() {
		synchronized (noValidRenderLinearizationLock) {
			synchronized (noValidInteractionLinearizationLock) {
				currentAnalysisID++;
				renderLinearization = null;
				interactionLinearization = null;
			}
		}
	}

	public int getSceneGraphStateId() {
		return currentAnalysisID;
	}

	/**
	 * Called by one of the render analysis jobs when a realization was found
	 */
	public void renderLinearizationFound(Linearization nuLinearizationCopy) {
		nuLinearizationCopy.uncluster();
		
		synchronized (noValidRenderLinearizationLock) {
			renderLinearization = nuLinearizationCopy;
			noValidRenderLinearizationLock.notifyAll();
		}
	}
	
	/**
	 * Called by one of the interaction analysis jobs when a linearization was found
	 */
	public void interactionLinearizationFound(Linearization nuLinearizationCopy) {
		nuLinearizationCopy.uncluster();
		
		synchronized (noValidInteractionLinearizationLock) {
			interactionLinearization = nuLinearizationCopy;
			noValidInteractionLinearizationLock.notifyAll();
		}
	}
	
	@Override
	public void onPause(){
		currentAnalysisID = -10;
		lastRenderJobIssuedID = -5;
		lastInteractionJobIssuedID = -2;
	}
	
	@Override
	public void onResume(){
		currentAnalysisID = 1;
	}

	@Override
	public void onDestroy() {
		currentAnalysisID = -1000;
	}
}
