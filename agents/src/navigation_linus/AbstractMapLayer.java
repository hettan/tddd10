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
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import navigation_emil.GridBasedSearch;
import navigation_emil.SimpleTimer;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.StandardViewLayer; 
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import sample.SampleSearch;
import sample.SearchAlgorithm;



public class AbstractMapLayer extends StandardViewLayer{

	private StandardWorldModel model;
	private ArrayList<Path> interEdges;
	private ArrayList<Rectangle2D> meshArrayRectangles;
	private ScreenTransform screenTransform;
	private Graphics2D graphics2d;
	private ArrayList<BorderNode> borderNodes;

	public AbstractMapLayer(StandardWorldModel model, SearchAlgorithm search) {
		this.model = model;
		init();
		/*
		Collection<StandardEntity> entities = model.getAllEntities();
		StandardEntity[] eArray = new StandardEntity[entities.size()];
		eArray = entities.toArray(eArray);
		int totLen = 0;
		int notFound = 0;
		//SearchAlgorithm search = new HPAstar(model);
		Random rnd = new Random();
		for(int i = 0; i < 50; i++) {

			EntityID eid1 = eArray[rnd.nextInt(entities.size())].getID();
			EntityID eid2 = eArray[rnd.nextInt(entities.size())].getID();
			List<EntityID> resultArray =  search.performSearch(eid1,eid2);
			if(resultArray != null){
				int temp = 0;
				EntityID prev = null;
				for(EntityID e : resultArray){
					temp += model.getDistance(e, prev);
					prev = e;
				}
				totLen += temp;
			} else {
				notFound++;
				//System.out.println("First: " + p.first() + " second: " + p.second());
			}
		}

		SimpleTimer.printTime();
		System.out.println("HPAstar längd konstruktor: " + totLen + " notFound : " + notFound);*/
	}
	
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
		//	EntityID e1  = new EntityID(33329);

/*
		EntityID e1  = new EntityID(32737);
		//System.out.println("cluster in beginning: " + getCluster((Area)model.getEntity(e1)));
		//EntityID e2  = new EntityID(15338);
		EntityID e2  = new EntityID(3714);

		g.setColor(Color.blue);
		Ellipse2D.Double currentDot = new Ellipse2D.Double(arg1.xToScreen(
				((Area)model.getEntity(e1)).getX()), arg1.yToScreen(((Area)model.getEntity(e1)).getY()), 10, 10);
		g.fill(currentDot);
		g.setColor(Color.red);
		Ellipse2D.Double currentDot2 = new Ellipse2D.Double(arg1.xToScreen(
				((Area)model.getEntity(e2)).getX()), arg1.yToScreen(((Area)model.getEntity(e2)).getY()), 10, 10);
		g.fill(currentDot2);

		g.setColor(Color.black);
		for(BorderNode b : borderNodes){
			if(b.cluster == 9){
				Ellipse2D.Double currentDot21 = new Ellipse2D.Double(arg1.xToScreen(
						b.road.getX()), arg1.yToScreen(b.road.getY()), 10, 10);
				g.fill(currentDot21);
				objects.add(new RenderedObject(null, currentDot21)); 
			}
		}
		g.setColor(Color.pink);
		for(BorderNode b : borderNodes){
			if(b.cluster == 7){
				Ellipse2D.Double currentDot21 = new Ellipse2D.Double(arg1.xToScreen(
						b.road.getX()), arg1.yToScreen(b.road.getY()), 10, 10);
				g.fill(currentDot21);
				objects.add(new RenderedObject(null, currentDot21)); 
			}
		}

		g.setColor(Color.orange);
		for(BorderNode b : borderNodes){
			if(b.cluster == 14){
				Ellipse2D.Double currentDot21 = new Ellipse2D.Double(arg1.xToScreen(
						b.road.getX()), arg1.yToScreen(b.road.getY()), 10, 10);
				g.fill(currentDot21);
				objects.add(new RenderedObject(null, currentDot21)); 
			}
		}

		g.setColor(Color.yellow);
		for(BorderNode b : borderNodes){
			if(b.cluster == 15){
				Ellipse2D.Double currentDot21 = new Ellipse2D.Double(arg1.xToScreen(
						b.road.getX()), arg1.yToScreen(b.road.getY()), 10, 10);
				g.fill(currentDot21);
				objects.add(new RenderedObject(null, currentDot21)); 
			}
		}

/*


		System.out.println("before search");
		HPAstar astar = new HPAstar(model);
		ArrayList<EntityID> path = (ArrayList<EntityID>) astar.performSearch(e1, 
				e2);
		System.out.println("After search");
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
			CreateIntraEdge(borderNode, borderNodes);
		}
	}

	/**
	 * Create all intra-edges for one single bordernode
	 * @param borderNode
	 * @return
	 */
	public ArrayList<Path> CreateIntraEdge(BorderNode borderNode, ArrayList<BorderNode> borderNodesIn){

		ArrayList<Path>  pArray = breathFirstSearch(borderNode.road, borderNode.cluster, null, borderNodesIn);
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
	public ArrayList<Path> CreateIntraEdgeConcerningBlockades(BorderNode borderNode,
			ArrayList<BorderNode> borderNodesIn){
		//System.out.println("cluster in end " + borderNode.cluster);
		ArrayList<Path>  pArray = breathFirstSearch(borderNode.road, borderNode.cluster, null,borderNodesIn);
		if(!pArray.isEmpty()){ //Means that at least one path is found
			//System.out.println("parray size : " + pArray.size());

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

				p.length = p.length + (largestBlockade-1)*10000;
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
	public ArrayList<Path> CreateIntraEdge(BorderNode borderNode, Area a, ArrayList<BorderNode> borderNodesIn){

		ArrayList<Path>  pArray = breathFirstSearch(borderNode.road, borderNode.cluster, a, borderNodesIn);
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
	private ArrayList<Path> breathFirstSearch(Area start, int cluster, Area goal, 
			ArrayList<BorderNode> borderNodesIn) {

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
				for(int k = 0; k < borderNodesIn.size(); k++){
					if(cheapestPath.dest == borderNodesIn.get(k).road &&
							cluster == borderNodesIn.get(k).cluster){

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

					if(cluster == getCluster(neighbor)){

						int length = cheapestPath.length + 
								model.getDistance(cheapestPath.dest.getID(), e);

						for(int i = 0; i < checked.size(); i++){
							if(checked.get(i).area == e && checked.get(i).length > length){
								checked.remove(i);

								Path newPath = new Path(start, neighbor);
								newPath.path = (ArrayList<Area>) cheapestPath.path.clone();
								newPath.path.add(neighbor);
								newPath.length = length;

								checked.add(new CheckedArea(length,e));
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

		if(path != null && !path.isEmpty()){
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


		for(int x = left; x < right; x += width/4 +1){
			Line2D.Double line = new Line2D.Double(x, top, x, bottom);
			lineList.add(line);
		}
		for(int y = bottom; y < top; y += height/4 +1){
			Line2D.Double line = new Line2D.Double(right, y, left, y);
			lineList.add(line);
		}

		//Create interedges
		for (Entity entity : model.getAllEntities()) {
			if(entity instanceof Area){
				for(EntityID neighborId : ((Area) entity).getNeighbours()){
					if(model.getEntity(neighborId) instanceof Area){
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
			for(Path p : interEdges){
				if(p.start == a){
					newBorderNode.neighbors.add(p);
				}
			}
			borderNodes.add(newBorderNode);
		}

		CreateIntraEdges();
	}
}