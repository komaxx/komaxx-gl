package com.komaxx.komaxx_gl.traversing;

import java.util.ArrayList;

import com.komaxx.komaxx_gl.scenegraph.Node;

/**
 * A SceneGraphTraverser is able to run through the whole scene graph
 * and execute the given ISceneGraphVisitor on it. 
 *  
 * @author Matthias Schicker
 */
public class SceneGraphBroadFirstTraverser {
	private ArrayList<Node> toDoThisCycle = new ArrayList<Node>(50);
	private ArrayList<Node> toDoNextCycle = new ArrayList<Node>(50);
	
	/**
	 * Must be executed in the same thread as all addChild / removeChild calls
	 * to nodes!
	 */
	public void traverse(Node root, ISceneGraphVisitor visitor){
		boolean proceed = visitor.visitNode(root);
		if (proceed){
			toDoThisCycle.addAll(root.getChildren());
			
			ArrayList<Node> swap = null;
			while (toDoThisCycle.size() > 0){
				Node currentNode;
				for (int i = toDoThisCycle.size() - 1; i >= 0; --i){
					currentNode = toDoThisCycle.get(i);
					if (visitor.visitNode(currentNode)){
						toDoNextCycle.addAll(currentNode.getChildren());
					}
				}
				
				swap = toDoNextCycle;
				toDoNextCycle = toDoThisCycle;
				toDoThisCycle = swap;
				
				toDoNextCycle.clear();
			}
		}
		toDoThisCycle.clear();
		toDoNextCycle.clear();
	}
}
