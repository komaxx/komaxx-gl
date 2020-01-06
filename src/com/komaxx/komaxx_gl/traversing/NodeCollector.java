package com.komaxx.komaxx_gl.traversing;

import java.util.ArrayList;

import com.komaxx.komaxx_gl.scenegraph.Node;

/**
 * Collects all Nodes in the SceneGraph
 *  
 * @author Matthias Schicker
 */
public class NodeCollector implements ISceneGraphVisitor{
	private ArrayList<Node> collector = new ArrayList<Node>();
	
	public void reset(){
		collector.clear();
	}

	@Override
	public boolean visitNode(Node node) {
		collector.add(node);
		return true;
	}

	public ArrayList<Node> getNodes(){
		return collector;
	}
	
	public Node[] getNodesArray(Node[] bufferArray) {
		return collector.toArray(bufferArray);
	}
}
