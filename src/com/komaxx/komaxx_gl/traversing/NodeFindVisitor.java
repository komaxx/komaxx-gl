package com.komaxx.komaxx_gl.traversing;

import com.komaxx.komaxx_gl.scenegraph.Node;

/**
 * Visitor used to find the first node with a given name. Which one
 * that is depends on whether the graph is traversed depth-first
 * or broad-first.
 * 
 * @author Matthias Schicker
 */
public class NodeFindVisitor implements ISceneGraphVisitor {
	private final String nameToFind;

	private Node foundNode = null;

	public NodeFindVisitor(String name) {
		this.nameToFind = name;
	}

	@Override
	public boolean visitNode(Node node) {
		if (nameToFind.equals(node.getName())){
			foundNode = node;
			return false;
		}
		return true;
	}
	
	public Node getFoundNode() {
		return foundNode;
	}
}
