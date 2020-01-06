package com.komaxx.komaxx_gl.scenegraph.analysis;

import java.util.ArrayList;

import com.komaxx.komaxx_gl.scenegraph.Node;

/**
 * Wrapper for a scene graph node to include additional fields necessary for analysis.
 * 
 * @author Matthias Schicker
 *
 */
public class AnalysisNode {
	public final Integer key;
	
	public final Node node;
	public ArrayList<AnalysisNode> pathToRoot;
	
	private boolean visited = false;
	
	protected AnalysisNode(Integer key, Node n){
		this.key = key;
		node = n;
	}
	
	public AnalysisNode(Integer key, Node node, AnalysisNode parent){
		this.key = key;
		this.node = node;
		pathToRoot = new ArrayList<AnalysisNode>();
		if (parent != null){
			pathToRoot.addAll(parent.pathToRoot);
		}
		pathToRoot.add(0, this);
	}
	
	@Override
	public String toString() {
		return node.toString();
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	public boolean visited() {
		return visited;
	}
	
	public boolean isCluster(){
		return false;
	}
}
