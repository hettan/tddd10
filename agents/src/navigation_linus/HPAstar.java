package navigation_linus;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import rescuecore2.misc.collections.LazyMap;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.components.StandardViewer;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import sample.SearchAlgorithm;

public class HPAstar extends StandardViewer implements SearchAlgorithm{

	private StandardWorldModel model;
	private AbstractMapLayer mapLayer;
	private Map<EntityID, Set<EntityID>> graph;
	private ScreenTransform arg1;
	private Graphics2D g;

	/**
	 * Standard constructor
	 * @param modelIn
	 * @param g 
	 * @param arg1 
	 * @param abstractMapLayer 
	 * @param borderNodes 
	 */
	public HPAstar(StandardWorldModel modelIn) {
		model = modelIn;
		mapLayer = new AbstractMapLayer(model,this);
	//	this.g = g;
	//	this.arg1 = arg1;

		//System.out.println("agent using HPAstar search");

		//Create the graph in the same way as sampleSearch
		Map<EntityID, Set<EntityID>> neighbours = new LazyMap<EntityID, Set<EntityID>>() {
			@Override
			public Set<EntityID> createValue() {
				return new HashSet<EntityID>();
			}
		};
		for (Entity next : model) {
			if (next instanceof Area) {
				Collection<EntityID> areaNeighbours = ((Area)next).getNeighbours();
				neighbours.get(next.getID()).addAll(areaNeighbours);
			}
		}
		setGraph(neighbours);
	}

	/**
	 * Test constructor to be called from AbstractMapLayer
	 * @param modelIn
	 * @param borderNodesIn
	 * @param mapLayerIn
	 */
	public HPAstar(StandardWorldModel modelIn, ArrayList<BorderNode> borderNodesIn, AbstractMapLayer mapLayerIn) {
		model = modelIn;
		mapLayer = mapLayerIn;
	}

	//@SuppressWarnings("unchecked")
	private ArrayList<Area> Search(Area start, ArrayList<Area> goalAreas) {

		//	System.out.println("search for a path with hpa*, nr of goals " + goalAreas.size() + 
		//			"starts at " + start.getID());

		Queue<Path> priorityQueue = new PriorityQueue<>(20, pathComparator);
		ArrayList<CheckedArea> checked = new ArrayList<CheckedArea>();
		Path initPath = new Path(start, start);
		initPath.heuristic = manhattanDistance(start, goalAreas);
		priorityQueue.add(initPath);
		checked.add(new CheckedArea(0,start.getID()));
		ArrayList<BorderNode> borderNodes = (ArrayList<BorderNode>) mapLayer.getBorderNodes().clone();

		//Add startNode to abstractMap
		BorderNode startNode = null;
		if(!mapLayer.isBorderNode(start)){
			int cluster = mapLayer.getCluster(start);
			startNode = new BorderNode(cluster, start);

			ArrayList<Path> temp = mapLayer.CreateIntraEdgeConcerningBlockades(startNode,borderNodes);

			for(Path p : temp){
				/*
				g.setColor(Color.green);
				Ellipse2D.Double currentDot21 = new Ellipse2D.Double(arg1.xToScreen(
						p.dest.getX()), arg1.yToScreen(p.dest.getY()), 10, 10);
				g.fill(currentDot21);*/
			}

			//mapLayer.addBorderNode(startNode);
			borderNodes.add(startNode);
		}

		//Add goalNodes to abstractMap
		ArrayList<BorderNode> goals = new ArrayList<BorderNode>();
		ArrayList<ArrayList<Path>> tempPaths = new ArrayList<ArrayList<Path>>();
		for(Area a : goalAreas){
			if(!mapLayer.isBorderNode(a)){
			//	System.out.println("start is not bordernode");
				int cluster = mapLayer.getCluster(a);
				BorderNode goalNode = new BorderNode(cluster, a);
				//	mapLayer.addBorderNode(goalNode);
				mapLayer.CreateIntraEdge(goalNode,borderNodes);
				borderNodes.add(goalNode);

				goals.add(goalNode);

				for(BorderNode b : borderNodes){
					if(b.cluster == cluster){
						tempPaths.add(mapLayer.CreateIntraEdge(b, a, borderNodes));
					}
				}
			}
		}

		//borderNodes = mapLayer.getBorderNodes();

		while(!priorityQueue.isEmpty()){

			Path cheapestPath = priorityQueue.poll();

			//Check if a a path to a goalnode is found
			for(Area goal : goalAreas){
				if(cheapestPath.dest == goal){

					removeTempBorders(startNode, goals, tempPaths, borderNodes);
					//	System.out.println("path is found!");
					return cheapestPath.path;
				}
			}

			//Get bordernode from cheapestpath
			BorderNode cheapestNode = null;
			for(BorderNode temp : borderNodes){
				if(temp.road == cheapestPath.dest){
					cheapestNode = temp;
					break;
				}
			}

			if(cheapestNode != null){
				for(Path p : cheapestNode.neighbors){
					/*	g.setColor(Color.green);
					Ellipse2D.Double currentDot21 = new Ellipse2D.Double(arg1.xToScreen(
							p.dest.getX()), arg1.yToScreen(p.dest.getY()), 10, 10);
					g.fill(currentDot21);*/

					int length = cheapestPath.length + p.length;

					boolean shallExpand = true;
					for(CheckedArea a : checked){
						if(a.area == p.dest.getID() && length >= a.length){
							shallExpand = false;
						}
					}

					if(shallExpand){

						//Remove old value
						for(int i = 0; i < checked.size(); i++){
							if(checked.get(i).area == p.dest.getID()){
								checked.remove(i);
								break;
							}
						}

						//Add new..
						checked.add(new CheckedArea(length, p.dest.getID()));				

						Path newPath = new Path(start, p.dest);
						newPath.path = (ArrayList<Area>) cheapestPath.path.clone();
						newPath.path.addAll(p.path);
						newPath.length = length;
						newPath.heuristic = manhattanDistance(p.dest,goalAreas);

						//Remove longer paths with the same dest
						ArrayList<Path> tempArray = new ArrayList<Path>();
						for(Path path : priorityQueue){
							if(path.dest == p.dest){
								tempArray.add(path);
							}
						}
						for(Path path : tempArray){
							priorityQueue.remove(path);
						}

						//Add new path
						priorityQueue.add(newPath);			
					}
				}
			}
		}

		//removeTempBorders(startNode, goals, tempPaths, borderNodes);
		//System.out.println("path is not found!");

		return null;
	}

	/**
	 * Removes all newly created borders and paths
	 * @param startNode
	 * @param goals
	 * @param tempPaths
	 * @param borderNodes
	 */
	private void removeTempBorders(BorderNode startNode, ArrayList<BorderNode> goals,
			ArrayList<ArrayList<Path>> tempPaths, ArrayList<BorderNode> borderNodes){
		if(startNode != null){
			mapLayer.removeBorderNode(startNode);
		}

		for(int i = goals.size()-1; i > 0; i--){
			mapLayer.removeBorderNode(goals.get(i));
		}

		for(int k = 0; k < goals.size(); k++){
			for(int i = 0; i < borderNodes.size(); i++){
				BorderNode b = borderNodes.get(i);
				if(goals.get(k).cluster == b.cluster){
					mapLayer.removeIntraEdges(b, tempPaths.get(k));
				}
			}
		}
	}

	private static Comparator<Path> pathComparator = new Comparator<Path>(){

		@Override
		public int compare(Path p1, Path p2) {
			return (int) ((p1.length+p1.heuristic) - (p2.length+p2.heuristic));
		}
	};

	private int manhattanDistance(Area r1, ArrayList<Area> goals){

		int returnValue = Integer.MAX_VALUE;
		for(Area a : goals){
			int dx = Math.abs(r1.getX()-a.getX());
			int dy = Math.abs(r1.getY()-a.getY());
			int temp = (int) Math.pow((Math.pow(dx, 2) +  Math.pow(dy, 2)), 1/2);
			if(temp < returnValue){
				returnValue = temp;
			}
		}
		return returnValue;
	}

	@Override
	public void setGraph(Map<EntityID, Set<EntityID>> newGraph) {
		graph = newGraph;

	}

	@Override
	public Map<EntityID, Set<EntityID>> getGraph() {
		return graph;
	}

	@Override
	public List<EntityID> performSearch(EntityID start, EntityID... goals) {

		if(!(model.getEntity(start) instanceof Area) || goals.length == 0){
			return null;
		}
		Area startArea = (Area)model.getEntity(start);

		ArrayList<Area> goalAreas = new ArrayList<Area>();
		for(int i = 0; i < goals.length; i++){
			if(model.getEntity(goals[i]) instanceof Area){
				goalAreas.add((Area) model.getEntity(goals[i]));
			}
		}

		ArrayList<Area> temp = Search(startArea, goalAreas);
		ArrayList<EntityID> returnArray = new ArrayList<EntityID>();
		if(temp != null && !temp.isEmpty()){
			for(int i = 0; i < temp.size(); i++){
				returnArray.add(temp.get(i).getID());
			}
		}

		if(returnArray.isEmpty()){
			return null;
		}

		//	System.out.println("Made HPAsearch, length: " + returnArray.size());
		return returnArray;
	}

	@Override
	public List<EntityID> performSearch(EntityID start,
			Collection<EntityID> goals) {

		EntityID[] arrGoals = new EntityID[goals.size()];

		return performSearch(start, goals.toArray(arrGoals));
	}

	@Override
	public List<EntityID> getPriorityNodes() {
		ArrayList<BorderNode> borderNodes = mapLayer.getBorderNodes();
		ArrayList<EntityID> returnArray = new ArrayList<EntityID>();
		for(BorderNode b : borderNodes){
			returnArray.add(b.road.getID());
		}
		return returnArray;
	}

	/**
	 * Sorry I had to edit in your file. But I felt i had to do this so I could keep the master branch in a working state ;)
	 * Regards,
	 * Emil
	 */
	@Override
	public List<EntityID> getRemainingPath(List<EntityID> path,
			EntityID currentArea) {
		List<EntityID> result = new ArrayList<EntityID>();
		boolean passedCurrent = false;
		for(int i = 0; i < path.size(); i++) {
			if(path.get(i).getValue() == currentArea.getValue())
				passedCurrent = true;
			if(passedCurrent)
				result.add(path.get(i));
		}
		return result;
	}
}
