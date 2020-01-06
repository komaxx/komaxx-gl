package com.komaxx.komaxx_gl.scenegraph.analysis;

import java.util.ArrayList;

/**
 * Represents one path through the graph of all possible node traversals.
 *   
 * @author Matthias Schicker
 */
public class Linearization {
	public ArrayList<Path> paths = new ArrayList<Path>();
	public int price;
	
	@Override
	public Linearization clone(){
		Linearization ret = new Linearization();
		ret.price = this.price;
		ret.paths.addAll(this.paths);
		return ret;
	}
	
	public void set(Linearization from){
		paths.clear();
		paths.addAll(from.paths);
		price = from.price;
	}

	public void reset() {
		paths.clear();
		price = 1000;
	}

	public void uncluster() {
		ArrayList<Path> unclusteredPaths = new ArrayList<Path>();
		Path nowPath;
		ClusterNode nowCluster;
		AnalysisNode nowStartNode;
		for (int i = 0; i < paths.size(); i++){
			nowPath = paths.get(i);

			boolean changed = false;
			
			if (nowPath.startNode.isCluster()){
				ArrayList<AnalysisNode> nodes = ((ClusterNode)nowPath.startNode).nodes;
				nowStartNode = nodes.get(nodes.size()-1);
				changed = true;
			} else {
				nowStartNode = nowPath.startNode;
			}
			
			if (nowPath.endNode.isCluster()){
				nowCluster = (ClusterNode) nowPath.endNode;
				
				unclusteredPaths.add(buildPath( nowStartNode, nowCluster.nodes.get(0) ));

				for (int j = 0; j < nowCluster.nodes.size()-1; j++){
					unclusteredPaths.add( buildPath(nowCluster.nodes.get(j), nowCluster.nodes.get(j+1)) );
				}
			} else {
				if (changed){
					nowPath.startNode = nowStartNode;		// replace cluster with single node (save memory)
					nowPath.compute();
				}
				unclusteredPaths.add(nowPath);
			}
		}
		
		paths = unclusteredPaths;
	}
	
	private static Path buildPath(AnalysisNode a, AnalysisNode b) {
		Path ret = new Path(a, b);
		ret.compute();
		return ret;
	}
}
