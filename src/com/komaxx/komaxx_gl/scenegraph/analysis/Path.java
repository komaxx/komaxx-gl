package com.komaxx.komaxx_gl.scenegraph.analysis;

import java.util.ArrayList;

/**
 * Represents a priced traversal from one Analysis node to another 
 *  
 * @author Matthias Schicker
 */
public class Path {
	public int price;
	
	public AnalysisNode startNode;
	public AnalysisNode endNode;
	/**
	 * the complete path to the shared node including start and shared node.
	 */
	public ArrayList<AnalysisNode> pathUp = new ArrayList<AnalysisNode>(8);
	/**
	 * the complete path from the shared node including shared and end node.
	 */
	public ArrayList<AnalysisNode> pathDown = new ArrayList<AnalysisNode>(8);

	public Path(AnalysisNode start, AnalysisNode end) {
		startNode = start;
		endNode = end;
	}
	
	// profiling info
	public long maxRenderNanos = 0;
	
	public long renderTimeCumulator = 0;
	public int renderFrames = 0;
	
	
	/**
	 * returns false when there is no shared parent node.
	 */
	public boolean compute() {
		pathUp.clear();
		pathDown.clear();
		AnalysisNode currentNode = startNode;
		// got to first shared parent
		boolean found = false;
		while(!found){
			pathUp.add(currentNode);
			if (endNode.pathToRoot.contains(currentNode)){
				found = true;
			} else {
				currentNode = currentNode.pathToRoot.get(1);
			}
		}
		if (!found) return false;
		
		AnalysisNode sharedParent = currentNode;
		pathDown.add(sharedParent);
		// now, for the other half, go down the tree again
		currentNode = endNode;
		
		while (currentNode != sharedParent && 
				!(currentNode.isCluster() && ((ClusterNode)currentNode).contains(sharedParent))
				){
			pathDown.add(1, currentNode);
			currentNode = currentNode.pathToRoot.get(1);
		}

		return true;
	}
	
	@Override
	public String toString() {
		return startNode.node.getName() + " --" + (pathUp.size()+pathDown.size()-2) + "-> " + endNode.node.getName() + ": " + price;
	}

	public void clearProfilingData() {
		maxRenderNanos = 0;
		renderTimeCumulator = 0;
		renderFrames = 0;
	}

	public void updateRenderTime(long pathTimeNs) {
		renderFrames++;
		renderTimeCumulator += pathTimeNs;
		if (pathTimeNs > maxRenderNanos) maxRenderNanos = pathTimeNs;
	}
}
