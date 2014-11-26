package navigation_linus;

import java.awt.Color; 
import java.awt.Graphics2D; 
import java.awt.Polygon; 
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection; 
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import rescuecore.view.Line;
import rescuecore2.components.Component;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.StandardViewLayer; 
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;


public class AbstractMapLayer extends StandardViewLayer{

	private StandardWorldModel model;

	//Array of all inter edges
	private ArrayList<Path> interEdges;

	private ArrayList<Rectangle2D> meshArrayRectangles;

	private ScreenTransform screenTransform;
	private Graphics2D graphics2d;

	private ArrayList<BorderNode> borderNodes;



	public AbstractMapLayer(StandardWorldModel model) {
		this.model = model;
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
		ArrayList<Line2D.Double> lineList = new ArrayList<Line2D.Double>();
		meshArrayRectangles = new ArrayList<Rectangle2D>();
		ArrayList<Line2D.Double> borderLines = new ArrayList<Line2D.Double>();
		ArrayList<Road> borderRoads = new ArrayList<Road>();

		borderNodes = new ArrayList<BorderNode>();
		interEdges= new ArrayList<Path>();

		//Calculate the size of the map
		g.setColor(Color.cyan);
		int left = Integer.MAX_VALUE;
		int right = 0;
		int top = 0;
		int bottom = Integer.MAX_VALUE;
		Area leftArea = null;
		Area rightArea = null;
		Area topArea = null;
		Area bottomArea = null;
		for (Entity entity : model.getAllEntities()) {
			if(entity instanceof Area){
				Area area = (Area) entity;
				if(area.getX() < left){
					left = area.getX();
					leftArea = area;
				}
				if(area.getX() > right){
					right = area.getX();
					rightArea = area;
				}
				if(area.getY() < bottom){
					bottom = area.getY();
					bottomArea = area;
				}
				if(area.getY() > top){
					top = area.getY();
					topArea = area;
				}
			}
		}
		int width = arg1.xToScreen(rightArea.getX())-arg1.xToScreen(leftArea.getX());
		int height = arg1.yToScreen(bottomArea.getY())-arg1.yToScreen(topArea.getY());

		for(int x = arg1.xToScreen(leftArea.getX()); x < arg1.xToScreen(rightArea.getX()); x += width/4){
			Line2D.Double line = new Line2D.Double(x, arg1.yToScreen(topArea.getY()), x, arg1.yToScreen(height));
			objects.add(new RenderedObject(null, line));
			lineList.add(line);
		}

		for(int y = arg1.yToScreen(topArea.getY()); y < arg1.yToScreen(bottomArea.getY()); y += height/4){
			Line2D.Double line = new Line2D.Double(arg1.xToScreen(rightArea.getX()), y, arg1.xToScreen(width), y);
			objects.add(new RenderedObject(null, line));
			lineList.add(line);
		}

		//Mark every road with a line
		g.setColor(Color.green);
		for (Entity entity : model.getAllEntities()) {
			if(entity instanceof Road){
				for(EntityID neighborId : ((Road) entity).getNeighbours()){
					if(model.getEntity(neighborId) instanceof Road){
						Road neighborRoad = (Road) model.getEntity(neighborId);
						Road currentRoad = (Road) entity;

						//Create line between roads
						Line2D.Double line2 = new Line2D.Double(arg1.xToScreen(currentRoad.getX()),
								arg1.yToScreen(currentRoad.getY()),
								arg1.xToScreen(neighborRoad.getX()),
								arg1.yToScreen(neighborRoad.getY()));

						for(int i = 0; i < lineList.size();i++){
							if(line2.intersectsLine(lineList.get(i))){
								//Draw line between roads
								g.draw(line2);
								objects.add(new RenderedObject(null, line2)); 
								borderLines.add(line2);

								if(!borderRoads.contains(currentRoad)){
									//Draw current road
									Ellipse2D.Double currentDot = new Ellipse2D.Double(arg1.xToScreen(
											currentRoad.getX()), arg1.yToScreen(currentRoad.getY()), 10, 10);
									g.fill(currentDot);
									objects.add(new RenderedObject(null, currentDot)); 
									borderRoads.add(currentRoad);
								}

								if(!borderRoads.contains(neighborRoad)){
									//Draw neighbor road
									Ellipse2D.Double neighborDot = new Ellipse2D.Double(arg1.xToScreen(
											neighborRoad.getX()), arg1.yToScreen(neighborRoad.getY()), 10, 10);
									g.fill(neighborDot);
									objects.add(new RenderedObject(null, neighborDot)); 
									borderRoads.add(neighborRoad);
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

		System.out.println("Bordernodes: " + borderRoads.size());
		System.out.println("borderLines: " + borderLines.size());

		//Create meshArray Rectangles
		for(int x = arg1.xToScreen(leftArea.getX()); x < arg1.xToScreen(rightArea.getX()); x += width/4 +1 ){
			for(int y = arg1.yToScreen(topArea.getY()); y < arg1.yToScreen(bottomArea.getY()); y += height/4 +1){
				g.setColor(Color.yellow);
				Rectangle2D.Double rectangle = new Rectangle2D.Double(x, 
						y, 
						width/4, 
						height/4);
				g.draw(rectangle);		
				meshArrayRectangles.add(rectangle);

			}
		}

		int cluster = 0;
		for(Road r : borderRoads){
			for(Rectangle2D rectangle : meshArrayRectangles){
				if(rectangle.contains(arg1.xToScreen(r.getX()), arg1.yToScreen(r.getY()))){
					BorderNode newBorderNode = new BorderNode(cluster, r);
					borderNodes.add(newBorderNode);
					for(Path p : interEdges){
						if(p.start == r){
							newBorderNode.neighbors.add(p);
						}
					}
					cluster = 0;
					break;
				}
				cluster++;
			}
			cluster = 0;
		}

		CreateIntraEdges();

		g.setColor(Color.ORANGE);
		for(int i = 0; i < borderNodes.size(); i++){

			if(!borderNodes.get(i).neighbors.isEmpty()){

				for(Path p : borderNodes.get(i).neighbors){

					if(p.path.size() != 0){

						Road previous = null;
						for(int k = 1; k < p.path.size(); k++){
							Road r = p.path.get(k);
							previous = p.path.get(k-1);
							Line2D.Double line2 = new Line2D.Double(arg1.xToScreen(r.getX()),
									arg1.yToScreen(r.getY()),
									arg1.xToScreen(previous.getX()),
									arg1.yToScreen(previous.getY()));

							g.draw(line2);
						}
					}
				}
			}
		}
		g.setColor(Color.blue);
		AStar astar = new AStar(model, borderNodes);

		ArrayList<Road> path = astar.Search(borderNodes.get(4).road, borderNodes.get(borderNodes.size()-9).road);
		if(path != null){

			//Draw current road
			Ellipse2D.Double currentDot = new Ellipse2D.Double(arg1.xToScreen(
					path.get(0).getX()), arg1.yToScreen(path.get(0).getY()), 10, 10);
			g.fill(currentDot);
			
			//Draw current road
			Ellipse2D.Double currentDot2 = new Ellipse2D.Double(arg1.xToScreen(
					path.get(path.size()-1).getX()), arg1.yToScreen(path.get(path.size()-1).getY()), 10, 10);
			g.fill(currentDot2);
			
			Road previous = null;
			for(int k = 1; k < path.size(); k++){
				Road r = path.get(k);
				previous = path.get(k-1);
				Line2D.Double line2 = new Line2D.Double(arg1.xToScreen(r.getX()),
						arg1.yToScreen(r.getY()),
						arg1.xToScreen(previous.getX()),
						arg1.yToScreen(previous.getY()));

				g.draw(line2);
			}
		}
		
		return objects;
	}

	private void CreateIntraEdges(){
		for(int i = 0; i < borderNodes.size(); i++){//Loop through all bordernodes

			BorderNode borderNode = borderNodes.get(i);	

			ArrayList<Path>  pArray = breathFirstSearch(borderNode.road, borderNode.cluster);
			if(!pArray.isEmpty()){ //Means that at least one path is found
				borderNode.neighbors.addAll(pArray);
			}
		}
	}

	private ArrayList<Path> breathFirstSearch(Road start, int cluster) {

		Queue<Path> priorityQueue = new PriorityQueue<>(20, pathComparator);
		ArrayList<EntityID> checked = new ArrayList<EntityID>();
		Path initPath = new Path(start, start);
		priorityQueue.add(initPath);
		checked.add(start.getID());
		ArrayList<Path> returnArray = new ArrayList<Path>();

		while(!priorityQueue.isEmpty()){

			Path cheapestPath = priorityQueue.poll();

			//Check if current node is a bordernode
			for(int k = 0; k < borderNodes.size(); k++){
				if(cheapestPath.dest == borderNodes.get(k).road){
					returnArray.add(cheapestPath);
					break;
				}
			}

			//Iterate through neighbors
			for(EntityID e : cheapestPath.dest.getNeighbours()){
				if(!checked.contains(e)){
					checked.add(e);
					if(model.getEntity(e) instanceof Road){
						Road neighbor = (Road) model.getEntity(e);					

						if(meshArrayRectangles.get(cluster).contains(
								screenTransform.xToScreen(neighbor.getX()), 
								screenTransform.yToScreen(neighbor.getY()))){

							Path newPath = new Path(start, neighbor);
							newPath.path = (ArrayList<Road>) cheapestPath.path.clone();
							newPath.path.add(neighbor);
							newPath.length = cheapestPath.length + 
									model.getDistance(cheapestPath.dest.getID(), newPath.dest.getID());

							priorityQueue.add(newPath);
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

}