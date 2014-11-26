package navigation_linus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class AStar {

	private StandardWorldModel model;
	private ArrayList<BorderNode> borderNodes;

	AStar(StandardWorldModel modelIn, ArrayList<BorderNode> borderNodesIn) {
		model = modelIn;
		borderNodes = borderNodesIn;
	}

	public ArrayList<Road> Search(Road start, Road goal) {

		Queue<Path> priorityQueue = new PriorityQueue<>(20, pathComparator);
		ArrayList<Road> checked = new ArrayList<Road>();
		Path initPath = new Path(start, start);
		initPath.heuristic = manhattanDistance(start, goal);
		priorityQueue.add(initPath);
		checked.add(start);


		while(!priorityQueue.isEmpty()){

			Path cheapestPath = priorityQueue.poll();
			if(!(cheapestPath.dest == goal)){

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
							newPath.path = (ArrayList<Road>) cheapestPath.path.clone();
							newPath.path.addAll(p.path);
							newPath.length = cheapestPath.length + p.length;
							newPath.heuristic = manhattanDistance(newPath.dest, goal);

							priorityQueue.add(newPath);
						}
					}
				}
			} else {
				System.out.println("Found a path");
				return cheapestPath.path;
			}
		}
		return null;
	}

	public static Comparator<Path> pathComparator = new Comparator<Path>(){

		@Override
		public int compare(Path p1, Path p2) {
			return (int) ((p1.length+p1.heuristic) - (p2.length+p2.heuristic));
		}
	};
	
	public int manhattanDistance(Road r1, Road r2){
		int dx = Math.abs(r1.getX()-r2.getX());
		int dy = Math.abs(r1.getY()-r2.getY());
		return (int) Math.pow((Math.pow(dx, 2) +  Math.pow(dy, 2)), 1/2);
	}
}
