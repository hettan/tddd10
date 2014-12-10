package navigation_linus;

import java.awt.Color; 
import java.awt.Graphics2D; 
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection; 
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.StandardViewLayer; 
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;


public class AbstractMapLayer extends StandardViewLayer{

	private StandardWorldModel model;
	private ArrayList<Path> interEdges;
	private ArrayList<Rectangle2D> meshArrayRectangles;
	private ScreenTransform screenTransform;
	private Graphics2D graphics2d;
	private ArrayList<BorderNode> borderNodes;

	public AbstractMapLayer(StandardWorldModel model) {
		this.model = model;
		init();
	}

	public ArrayList<BorderNode> getMap(){
		return borderNodes;
	}

	@Override
	public String getName() { return "CustomLayer"; } 

	@Override
	public Collection<RenderedObject> render(Graphics2D g,
			ScreenTransform arg1, int arg2, int arg3) { 
		screenTransform = arg1;
		graphics2d = g;

		Collection<RenderedObject> objects = new HashSet<RenderedObject>(); 
		/*
		g.setColor(Color.green);
		for(BorderNode b : borderNodes){
			Ellipse2D.Double currentDot = new Ellipse2D.Double(arg1.xToScreen(
					b.road.getX()), arg1.yToScreen(b.road.getY()), 10, 10);
			g.fill(currentDot);
			objects.add(new RenderedObject(null, currentDot)); 
		}

		EntityID eId = new EntityID(31109);
		EntityID goalID = new EntityID(35294);

		HPAstar astar = new HPAstar(model, borderNodes, this);
		ArrayList<EntityID> path = (ArrayList<EntityID>) astar.performSearch(eId, 
				goalID);
		printPath(path);
		 */
		return objects;
	}

	/**
	 * Create intra-edges for every bordernode
	 */
	public void CreateIntraEdges(){
		for(int i = 0; i < borderNodes.size(); i++){//Loop through all bordernodes

			BorderNode borderNode = borderNodes.get(i);	
			CreateIntraEdge(borderNode);
		}
	}

	/**
	 * Create all intra-edges for one single bordernode
	 * @param borderNode
	 * @return
	 */
	public ArrayList<Path> CreateIntraEdge(BorderNode borderNode){

		ArrayList<Path>  pArray = breathFirstSearch(borderNode.road, borderNode.cluster, null);
		if(!pArray.isEmpty()){ //Means that at least one path is found
			borderNode.neighbors.addAll(pArray);
		}

		return pArray;
	}

	/**
	 * Create all intra-edges for one single bordernode with blockades in considerations
	 * @param borderNode
	 * @return
	 */
	public ArrayList<Path> CreateIntraEdgeConcerningBlockades(BorderNode borderNode){

		ArrayList<Path>  pArray = breathFirstSearch(borderNode.road, borderNode.cluster, null);
		if(!pArray.isEmpty()){ //Means that at least one path is found

			for(Path p : pArray){
				int largestBlockade = 1;
				for(Area a : p.path){
					int currentBlockade = 1;
					if(a.getBlockades() != null){
						for(EntityID e : a.getBlockades()){
							Blockade b = (Blockade) model.getEntity(e);
							currentBlockade += b.getRepairCost();
						}
					}
					if(largestBlockade < currentBlockade){
						largestBlockade = currentBlockade;
					}
				}

				//Length multiplied by the cost to clear the most expensive road on this path
				p.length = p.length*largestBlockade;
			}

			borderNode.neighbors.addAll(pArray);
		}

		return pArray;
	}

	/**
	 * Create one intra-edge to a specific bordernode 
	 * @param borderNode
	 * @return
	 */
	public ArrayList<Path> CreateIntraEdge(BorderNode borderNode, Area a){

		ArrayList<Path>  pArray = breathFirstSearch(borderNode.road, borderNode.cluster, a);
		if(!pArray.isEmpty()){ //Means that at least one path is found
			borderNode.neighbors.addAll(pArray);
		}
		return pArray;
	}

	/**
	 * Remove all neighbors for b that are listed in list
	 * @param b
	 * @param list
	 */
	public void removeIntraEdges(BorderNode b, ArrayList<Path> list){
		for(Path p : list){
			b.neighbors.remove(p);
		}
	}

	boolean isBorderNode(Area a){
		for(BorderNode b : borderNodes){
			if(b.road == a){
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Path> breathFirstSearch(Area start, int cluster, Area goal) {

		Queue<Path> priorityQueue = new PriorityQueue<>(20, pathComparator);
		ArrayList<CheckedArea> checked = new ArrayList<CheckedArea>();
		Path initPath = new Path(start, start);
		priorityQueue.add(initPath);
		checked.add(new CheckedArea(0, start.getID()));
		ArrayList<Path> returnArray = new ArrayList<Path>();

		while(!priorityQueue.isEmpty()){

			Path cheapestPath = priorityQueue.poll();

			//Check if current node is a bordernode
			if(goal != null){
				if(cheapestPath.dest == goal){

					Path newPath = new Path(cheapestPath.start, cheapestPath.dest);
					newPath.path = (ArrayList<Area>) cheapestPath.path.clone();
					newPath.length = cheapestPath.length;

					returnArray.add(newPath);
					return returnArray;
				}
			} else {
				for(int k = 0; k < borderNodes.size(); k++){
					if(cheapestPath.dest == borderNodes.get(k).road){

						Path newPath = new Path(cheapestPath.start, cheapestPath.dest);
						newPath.path = (ArrayList<Area>) cheapestPath.path.clone();
						newPath.length = cheapestPath.length;

						returnArray.add(newPath);
						break;
					}
				}
			}

			//Iterate through neighbors
			for(EntityID e : cheapestPath.dest.getNeighbours()){

				boolean doesNotExist = true;
				for(int i = 0; i < checked.size(); i++){
					if(checked.get(i).area == e){
						doesNotExist = false;
						break;
					}
				}
				if(doesNotExist){
					checked.add(new CheckedArea(Integer.MAX_VALUE,e));
				}

				if(model.getEntity(e) instanceof Area){
					Area neighbor = (Area) model.getEntity(e);					

					if(meshArrayRectangles.get(cluster).contains(
							neighbor.getX(), 
							neighbor.getY())){

						int length = cheapestPath.length + 
								model.getDistance(cheapestPath.dest.getID(), e);

						for(int i = 0; i < checked.size(); i++){
							if(checked.get(i).area == e && checked.get(i).length > length){
								checked.remove(i);

								Path newPath = new Path(start, neighbor);
								newPath.path = (ArrayList<Area>) cheapestPath.path.clone();
								newPath.path.add(neighbor);
								newPath.length = length;

								checked.add(new CheckedArea(newPath.length,e));
								priorityQueue.add(newPath);
								break;
							}
						}
					}
				}
			}
		}
		return returnArray;
	}

	public static Comparator<Path> pathComparator = new Comparator<Path>(){

		@Override
		public int compare(Path p1, Path p2) {
			return (int) (p1.length - p2.length);
		}
	};

	public int getCluster(Area a){
		int cluster = 0;
		for(Rectangle2D rectangle : meshArrayRectangles){
			if(rectangle.contains(a.getX(), a.getY())){
				return cluster;
			}
			cluster++;
		}
		return 0;
	}

	public ArrayList<BorderNode> getBorderNodes(){
		return borderNodes;
	}

	public void addBorderNode(BorderNode b){
		borderNodes.add(b);
	}

	public void removeBorderNode(BorderNode b){
		borderNodes.remove(b);
	}

	public void printPath(ArrayList<EntityID> path){
		if(!path.isEmpty()){
			graphics2d.setColor(Color.blue);
			Area startRoad = (Area) model.getEntity(path.get(0));
			Area goalRoad = (Area) model.getEntity(path.get(path.size()-1));

			//Draw current road
			Ellipse2D.Double currentDot = new Ellipse2D.Double(screenTransform.xToScreen(
					startRoad.getX()), screenTransform.yToScreen(startRoad.getY()), 10, 10);
			graphics2d.fill(currentDot);

			//Draw current road
			Ellipse2D.Double currentDot2 = new Ellipse2D.Double(screenTransform.xToScreen(
					goalRoad.getX()), screenTransform.yToScreen(goalRoad.getY()), 10, 10);
			graphics2d.fill(currentDot2);

			Area previous = null;
			for(int k = 1; k < path.size(); k++){
				Area r = (Area) model.getEntity(path.get(k));
				previous = (Area) model.getEntity(path.get(k-1));
				Line2D.Double line2 = new Line2D.Double(screenTransform.xToScreen(r.getX()),
						screenTransform.yToScreen(r.getY()),
						screenTransform.xToScreen(previous.getX()),
						screenTransform.yToScreen(previous.getY()));

				graphics2d.draw(line2);
			}
		} else {
			System.out.println("path not found");
		}


	}


	private void init(){

		ArrayList<Line2D.Double> lineList = new ArrayList<Line2D.Double>();
		ArrayList<Line2D.Double> borderLines = new ArrayList<Line2D.Double>();
		ArrayList<Area> borderAreas = new ArrayList<Area>();

		meshArrayRectangles = new ArrayList<Rectangle2D>();
		borderNodes = new ArrayList<BorderNode>();
		interEdges= new ArrayList<Path>();

		//Calculate the size of the map
		int left = Integer.MAX_VALUE;
		int right = 0;
		int top = 0;
		int bottom = Integer.MAX_VALUE;

		for (Entity entity : model.getAllEntities()) {
			if(entity instanceof Area){
				Area area = (Area) entity;
				if(area.getX() < left){
					left = area.getX();
				}
				if(area.getX() > right){
					right = area.getX();
				}
				if(area.getY() < bottom){
					bottom = area.getY();
				}
				if(area.getY() > top){
					top = area.getY();
				}
			}
		}

		int width = right-left;
		int height = top - bottom;

		for(int x = left; x < right; x += width/4){
			Line2D.Double line = new Line2D.Double(x, top, x, bottom);
			lineList.add(line);
		}
		for(int y = bottom; y < top; y += height/4){
			Line2D.Double line = new Line2D.Double(right, y, left, y);
			lineList.add(line);
		}

		//Create interedges
		for (Entity entity : model.getAllEntities()) {
			if(entity instanceof Area){
				for(EntityID neighborId : ((Area) entity).getNeighbours()){
					if(model.getEntity(neighborId) instanceof Road){
						Area neighborRoad = (Area) model.getEntity(neighborId);
						Area currentRoad = (Area) entity;

						//Create line between roads
						Line2D.Double line2 = new Line2D.Double(currentRoad.getX(),
								currentRoad.getY(),
								neighborRoad.getX(),
								neighborRoad.getY());

						for(int i = 0; i < lineList.size();i++){
							if(line2.intersectsLine(lineList.get(i))){
								borderLines.add(line2);
								if(!borderAreas.contains(currentRoad)){
									borderAreas.add(currentRoad);
								}

								if(!borderAreas.contains(neighborRoad)){
									borderAreas.add(neighborRoad);
								}

								int length = model.getDistance(currentRoad.getID(), neighborRoad.getID());
								Path newPath1 = new Path(currentRoad, neighborRoad);
								newPath1.path.add(neighborRoad);
								newPath1.length = length;

								Path newPath2 = new Path(neighborRoad, currentRoad);
								newPath2.path.add(currentRoad);
								newPath2.length = length;

								interEdges.add(newPath1);
								interEdges.add(newPath2);
							}
						}
					}
				}
			}
		}

		//Create meshArray Rectangles
		for(int x = left; x < right; x += width/4 +1 ){
			for(int y = bottom; y < top; y += height/4 +1){
				Rectangle2D.Double rectangle = new Rectangle2D.Double(x, 
						y, 
						width/4, 
						height/4);	
				meshArrayRectangles.add(rectangle);
			}
		}

		int cluster = 0;
		for(Area a : borderAreas){
			cluster = getCluster(a);
			BorderNode newBorderNode = new BorderNode(cluster, a);
			borderNodes.add(newBorderNode);
			for(Path p : interEdges){
				if(p.start == a){
					newBorderNode.neighbors.add(p);
				}
			}
		}

		CreateIntraEdges();
	}
}