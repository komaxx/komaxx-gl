package com.komaxx.komaxx_gl.traversing;

import java.util.ArrayList;

import com.komaxx.komaxx_gl.scenegraph.Node;

/**
 * A SceneGraphTraverser is able to run through the whole scene graph
 * and execute the given ISceneGraphVisitor on it. Depth first is a little
 * more expensive than broad first but ensures linearity in the execution
 * of the visitations. 
 *  
 * @author Matthias Schicker
 */
public class SceneGraphDepthFirstTraverser {
	public void traverse(Node root, ISceneGraphVisitor visitor){
		boolean proceed = visitor.visitNode(root);
		if (proceed){
			ArrayList<Node> children = root.getChildren();
			int l = children.size();
			for (int i = 0; i < l; i++){
				traverse(children.get(i), visitor);
			}
		}
	}
}
