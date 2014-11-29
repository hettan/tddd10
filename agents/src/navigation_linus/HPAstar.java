package navigation_linus;

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

	/**
	 * Standard constructor
	 * @param modelIn
	 */
	public HPAstar(StandardWorldModel modelIn) {
		model = modelIn;
		mapLayer = new AbstractMapLayer(model);

		System.out.println("agent using HPAstar search");

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

	@SuppressWarnings("unchecked")
	private ArrayList<Area> Search(Area start, ArrayList<Area> goalAreas) {

	//	System.out.println("search for a path");
	//	System.out.println("nr of goals: " + goalAreas.size());
		Queue<Path> priorityQueue = new PriorityQueue<>(20, pathComparator);
		ArrayList<Area> checked = new ArrayList<Area>();
		Path initPath = new Path(start, start);
		initPath.heuristic = 0;//manhattanDistance(start, goalAreas);
		priorityQueue.add(initPath);
		checked.add(start);
		ArrayList<BorderNode> borderNodes = mapLayer.getBorderNodes();

		//Add startNode to abstractMap
		BorderNode startNode = null;
		if(!mapLayer.isBorderNode(start)){
			int cluster = mapLayer.getCluster(start);
			startNode = new BorderNode(cluster, start);
			mapLayer.CreateIntraEdge(startNode);
			mapLayer.addBorderNode(startNode);
		}

		//Add goalNodes to abstractMap
		ArrayList<BorderNode> goals = new ArrayList<BorderNode>();
		ArrayList<ArrayList<Path>> tempPaths = new ArrayList<ArrayList<Path>>();
		for(Area a : goalAreas){
			if(!mapLayer.isBorderNode(a)){
				int cluster = mapLayer.getCluster(a);
				BorderNode goalNode = new BorderNode(cluster, a);
				mapLayer.addBorderNode(goalNode);
				mapLayer.CreateIntraEdge(goalNode);
				goals.add(goalNode);

				for(BorderNode b : borderNodes){
					if(b.cluster == cluster){
						tempPaths.add(mapLayer.CreateIntraEdge(b, a));
					}
				}
			}
		}

		while(!priorityQueue.isEmpty()){

			Path cheapestPath = priorityQueue.poll();
			for(Area goal : goalAreas){
				if(cheapestPath.dest == goal){
					System.out.println("Found a path between " + cheapestPath.start.getID() 
							+ " and " + cheapestPath.dest.getID());

					removeTempBorders(startNode, goals, tempPaths, borderNodes);

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
					if(!checked.contains(p.dest)){
						checked.add(p.dest);				

						Path newPath = new Path(start, p.dest);
						newPath.path = (ArrayList<Area>) cheapestPath.path.clone();
						newPath.path.addAll(p.path);
						newPath.length = cheapestPath.length + p.length;
						newPath.heuristic = 0;
						priorityQueue.add(newPath);
					}
				}
			}
		}

		removeTempBorders(startNode, goals, tempPaths, borderNodes);
		/*	System.out.println("no path found");
		for(Area a : goalAreas){
			System.out.println("goal: " + a.getID());
		}
		*/
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

	@SuppressWarnings("unused")
	private int manhattanDistance(Area r1, Area r2){
		int dx = Math.abs(r1.getX()-r2.getX());
		int dy = Math.abs(r1.getY()-r2.getY());
		return (int) Math.pow((Math.pow(dx, 2) +  Math.pow(dy, 2)), 1/2);
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
		
		Area startArea = (Area)model.getEntity(start);

		ArrayList<Area> goalAreas = new ArrayList<Area>();
		for(int i = 0; i < goals.length; i++){
			goalAreas.add((Area) model.getEntity(goals[i]));
		}
		
	//	System.out.println("init nr of goals 2: " + goalAreas.size());
		
		ArrayList<Area> temp = Search(startArea, goalAreas);
		ArrayList<EntityID> returnArray = new ArrayList<EntityID>();
		if(temp != null && !temp.isEmpty()){
			for(int i = 0; i < temp.size(); i++){
				returnArray.add(temp.get(i).getID());
			}
		}
		return returnArray;
	}

	@Override
	public List<EntityID> performSearch(EntityID start,
			Collection<EntityID> goals) {
		//System.out.println("init nr of goals: " + goals.size());
		Area startArea = (Area)model.getEntity(start);

		ArrayList<Area> goalAreas = new ArrayList<Area>();
		for(EntityID e : goals){
			goalAreas.add((Area) model.getEntity(e));
		}
		ArrayList<Area> temp = Search(startArea, goalAreas);
		ArrayList<EntityID> returnArray = new ArrayList<EntityID>();
		if(temp != null && !temp.isEmpty()){
			for(int i = 0; i < temp.size(); i++){
				returnArray.add(temp.get(i).getID());
			}
		}

		return returnArray;
	}

	@Override
	public List<EntityID> getPriorityNodes() {
		ArrayList<BorderNode> borderNodes = mapLayer.getBorderNodes();
		ArrayList<EntityID> returnArray = new ArrayList<EntityID>();
		for(BorderNode b : borderNodes){
			returnArray.add(b.road.getID());
		}
		return null;
	}
}
