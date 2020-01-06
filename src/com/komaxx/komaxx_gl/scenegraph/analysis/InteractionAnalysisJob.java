package com.komaxx.komaxx_gl.scenegraph.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;

import com.komaxx.komaxx_gl.scenegraph.Node;
import com.komaxx.komaxx_gl.scenegraph.SceneGraph;
import com.komaxx.komaxx_gl.util.KoLog;

/**
 * Represents one (threaded) analysis execution.
 * 
 * @author Matthias Schicker
 */
public class InteractionAnalysisJob implements Runnable {
	private static final boolean DEBUG = false;

	/**
	 * With this value a path will most likely not be picked, it is too costly.
	 * Note: This also minimizes teh error when only invalid paths are available.
	 */
	private static final int INVALID_RESULT_PENALTY = 10000000;
	// TODO: These are all untested dummies... Write testers to get some more valid prices!
	private static final int TRANSFORM_UP_PRICE = 1;
	private static final int TRANSFORM_DOWN_PRICE = 5;
	
	// ///////////////////////////////////////////////////////
	// shared memory friendly key handling (avoiding boxing)
	protected static final int MAX_STORED_KEYS = 256;		// should suffice for most use cases. No crash, when more needed!
	protected static final Integer[] nodeKeys = new Integer[MAX_STORED_KEYS];
	static {
		for (int i = 0; i < MAX_STORED_KEYS; i++) nodeKeys[i] = Integer.valueOf(i);
	}

	
	private final int sceneGraphStateId;
	private final SceneGraphAnalysor jobIssuer;
	private final SceneGraph sceneGraph;
	
	private AnalysisNode rootNode;
	private ArrayList<AnalysisNode> interactionNodes = new ArrayList<AnalysisNode>();
	private ArrayList<Path> directPaths = new ArrayList<Path>();
	private Hashtable<Integer, ArrayList<Path>> pathSourcesTable = new Hashtable<Integer, ArrayList<Path>>();
	private Linearization bestLinearization = new Linearization();
	
	// ///////////////////////////////////////////////////////
	// memory friendly key handling (avoiding boxing)
	private int nextNodeId = 1;
	
	private boolean linearizationFound = false;
	
	
	private static Comparator<Path> priceComparator = new Comparator<Path>() {
		@Override
		public int compare(Path a, Path b) {
			return a.price<b.price ? -1 : (a.price>b.price ? 1 : 0);
		}
	};

	private long startTime = 0;

	/** 
	 * When true, terminates all computations.
	 */
	private boolean forceAbort;

	public InteractionAnalysisJob(int sceneGraphStateId, SceneGraphAnalysor jobIssuer, SceneGraph sceneGraph){
		this.sceneGraphStateId = sceneGraphStateId;
		this.jobIssuer = jobIssuer;
		this.sceneGraph = sceneGraph;
	}
	
	@Override
	public void run(){
		if (abort()) return;

		if (DEBUG){
			KoLog.v(this, "Analyzing scene graph for interaction ...");
			startTime = System.currentTimeMillis();
		}
		
		if (!createAnalysisNodes(sceneGraph)){
			if (DEBUG){
				KoLog.v(this, "No interaction nodes found.");
			}
			bestLinearization.reset();
			jobIssuer.interactionLinearizationFound(bestLinearization.clone());
			return;
		}
		if (abort()) return;
		
		if (DEBUG){
			KoLog.v(this, (System.currentTimeMillis() - startTime) + "| ... nodes: " + interactionNodes.size());
		}
		
		combineClusterNodes();
		if (abort()) return;
		
		if (DEBUG){
			KoLog.v(this, (System.currentTimeMillis() - startTime) + "| ... nodes after clustering: " + interactionNodes.size());
		}
		
		computePricedDirectPaths();
		if (abort()) return;
		
		if (DEBUG){
			KoLog.v(this, (System.currentTimeMillis() - startTime) + "|... paths: " + directPaths.size());
		}
		
		sortSourceTables();		// for quick local heuristics
		if (abort()) return;
		
		if (DEBUG){
			KoLog.v(this, (System.currentTimeMillis() - startTime) + "|... sourceTables: " + pathSourcesTable.size());
		}
		
		buildLinearizations();
		if (abort()) return;
		
		if (!linearizationFound){
			KoLog.e(this, "NO LINEARIZATION FOUND!!");
		}
		
		if (DEBUG){
			long deltaTime = System.currentTimeMillis() - startTime;
			KoLog.v(this, "Analyzing scene graph for interaction... DONE! Took " + deltaTime + " millis.");
		}
	}

	private void combineClusterNodes() {
		AnalysisNode nowNode;
		AnalysisNode matchCandidate;
		short nowClusterIndex;
		ClusterNode nuClusterNode;
		for (int i = 0; i < interactionNodes.size(); i++){
			nowNode = interactionNodes.get(i);
			
			nowClusterIndex = nowNode.node.clusterIndex; 
			if (nowClusterIndex != Node.NO_CLUSTER_INDEX){
				nuClusterNode = new ClusterNode(getNodeKey(), nowNode);
				for (int j = interactionNodes.size()-1; j > i; j--){
					matchCandidate = interactionNodes.get(j);
					if (matchCandidate.node.clusterIndex == nowClusterIndex){
						nuClusterNode.add(matchCandidate);
						interactionNodes.remove(j);
					}
				}
				if (nuClusterNode.size() > 1){
					// hooray, this is actually a cluster!
					interactionNodes.set(i, nuClusterNode);
				}
			}
		}
	}

	private boolean abort() {
		return forceAbort || jobIssuer.getSceneGraphStateId() != this.sceneGraphStateId;
	}
	
	private void sortSourceTables() {
		// now, sort them for quicker future computation:
		Enumeration<ArrayList<Path>> elements = pathSourcesTable.elements();
		while (elements.hasMoreElements()){
			ArrayList<Path> currentTable = elements.nextElement();
			Collections.sort(currentTable, priceComparator);
		}
	}

	private void buildLinearizations() {
		bestLinearization.price = Integer.MAX_VALUE;
		Linearization currentLinearization = new Linearization();
		
		// all paths must start with the root!
		AnalysisNode startNode = interactionNodes.get(0);
		buildLinearization(startNode, currentLinearization, interactionNodes.size());
	}

	private void buildLinearization(AnalysisNode lastNode, 
			Linearization currentLinearization, int nodesLeft) {
		
		lastNode.setVisited(true);
		nodesLeft--;
		
		if (nodesLeft <= 0){
			// alright, the linearization is complete!
			// now let's check if this linearization is better than the last one
			if (currentLinearization.price < bestLinearization.price){
				linearizationFound = true;
				
				if (DEBUG){
					if (bestLinearization.price == Integer.MAX_VALUE){
							long deltaTime = System.currentTimeMillis() - startTime;
							KoLog.v(this, "FIRST interaction linearization after " + deltaTime + " millis, cost: " + currentLinearization.price);
					} else {
						KoLog.d(this, "NEW better interaction linearization: " + currentLinearization.price);
					}
				}
				// use this linearization!
				jobIssuer.interactionLinearizationFound(currentLinearization.clone());
				
				// yay, better -> take it as "best"
				bestLinearization.set(currentLinearization);
			}
		} else {
			ArrayList<Path> nextPaths = pathSourcesTable.get(lastNode.key);
			if (nextPaths != null){
				int l = nextPaths.size();
				Path nowPath;
				for(int i = 0; i < l; i++){
					nowPath = nextPaths.get(i);
					
					if (currentLinearization.price + nowPath.price >= bestLinearization.price){
						// already too expensive -> abort!
						break;
					} else if (nowPath.endNode.visited()){
						// this path can be ignored, the node was already visited
					} else {
						currentLinearization.paths.add(nowPath);
						currentLinearization.price += nowPath.price;
						
						// proceed in tree
						buildLinearization(nowPath.endNode, currentLinearization, nodesLeft);
						
						// prepare for the next
						currentLinearization.paths.remove(nowPath);
						currentLinearization.price -= nowPath.price;
					}
				}
			}
		}
		if (abort()) return;
		
		// and back up!
		lastNode.setVisited(false);
	}

	private void computePathPrice(Path path) {
		int price = 0;
		
		if (path.startNode==rootNode){
			path.price = -path.endNode.node.zLevel;
		} else {
			// compute up-price
			ArrayList<AnalysisNode> up = path.pathUp;
			int l = up.size()-1;
			Node nowNode;
			for (int i = 0; i < l; i++){
				nowNode = up.get(i).node;
				if (nowNode.transforms) price += TRANSFORM_UP_PRICE;
			}
			
			// compute down price
			ArrayList<AnalysisNode> down = path.pathDown;
			l = down.size()-1;
			for (int i = 1; i < l; i++){
				nowNode = down.get(i).node;
				if (nowNode.transforms) price += TRANSFORM_DOWN_PRICE;
			}
			
			Node s = path.startNode.node;
			Node d = path.endNode.node;
			
			// compute prices based on z-sorting
			if (s.zLevel > d.zLevel){
				// source is further in the scene than target -> zSorting violated!
				price += INVALID_RESULT_PENALTY;
			}
			
			path.price = price;
		}
	}

	private void computePricedDirectPaths() {
		directPaths.clear();
		pathSourcesTable.clear();
		int l = interactionNodes.size();
		AnalysisNode sourceNode;
		for (int i = 0; i < l; i++){
			sourceNode = interactionNodes.get(i);
			for (int j = 1; j < l; j++){	// we don't need paths to the root!
				if (i==j) continue;
				Path nuPath = new Path(sourceNode, interactionNodes.get(j));
				if (!nuPath.compute()) continue;
				computePathPrice(nuPath);
				if (nuPath.price >= INVALID_RESULT_PENALTY){
					// invalid path! do not add to paths.
				} else {
					directPaths.add(nuPath);
					// also: put it to hashtable for quick linearization.
					if (!pathSourcesTable.containsKey(sourceNode.key)){
						pathSourcesTable.put(sourceNode.key, new ArrayList<Path>());
					}
					ArrayList<Path> sourcesList = pathSourcesTable.get(sourceNode.key);
					sourcesList.add(nuPath);
				}
			}
		}
	}

	private boolean createAnalysisNodes(SceneGraph sceneGraph) {
		interactionNodes.clear();
		Node root = sceneGraph.getRoot();
		createAnalysisNode(root, null);
		
		if (interactionNodes.size() < 1) return false;		// no interacting nodes found!
		
		rootNode = interactionNodes.get(0).pathToRoot.get(interactionNodes.get(0).pathToRoot.size() - 1);
		interactionNodes.add(0, rootNode);
		
		return true;
	}

	private void createAnalysisNode(Node node, AnalysisNode parent) {
		AnalysisNode nuNode = new AnalysisNode(getNodeKey(), node, parent);
		if (node.handlesInteraction) interactionNodes.add(nuNode);
		ArrayList<Node> children = node.getChildren();
		int l = children.size();
		for (int i = 0; i < l; i++) createAnalysisNode(children.get(i), nuNode);
	}
	
	private Integer getNodeKey() {
		int key = nextNodeId++;
		if (key >= MAX_STORED_KEYS) return Integer.valueOf(key);
		return nodeKeys[key];
	}
}
