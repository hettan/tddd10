package navigation_emil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

/**
 * An A* algorithm that finds the shortest path between two Searchable<T> nodes.
 * @author emiol791
 *
 */
public class AStarAlgorithm {
	
	/**
	 * Uses simple A* to find the shortest path between start and end
	 * 
	 * @param start
	 * 		A searchable start node
	 * 
	 * @param end
	 * 		A searchable end node
	 * 
	 * @return
	 * 		A tuple of a path between start and end and the total length between them (not in number of nodes but actual distance)
	 */
	public static <T> PathLenTuple<T> PerformSearch(Searchable<T> start, Searchable<T> end) {
		PathLenTuple<T> result = new PathLenTuple<T>();
		
		//Same as goal?
		if(start.getUniqueId() == end.getUniqueId()) {
			result.addPathElement(end.getNodeObject());
			return result;
		}
		
		HashSet<Integer> open = new HashSet<Integer>();
		HashSet<Integer> closed = new HashSet<Integer>();
		PriorityQueue<Searchable<T>> frontier = new PriorityQueue<Searchable<T>>();
		
		frontier.add(start);
		open.add(start.getUniqueId());
		
		Searchable<T> current = null, child;
		while(!frontier.isEmpty()) {
			current = frontier.poll();
			if(closed.contains(current.getUniqueId())) continue;
			closed.add(current.getUniqueId());
			if(current != null && current.getUniqueId() == end.getUniqueId()) {
				break; // We found the end;
			}
			
			for(T childBase : current.getChildren()) {
				child = current.createNode(childBase, current, end);
				if(child != null && !open.contains(child.getUniqueId())) {
					frontier.add(child);
					open.add(child.getUniqueId());
				}
			}
		}
		
		if(current != null && current.getUniqueId() == end.getUniqueId()) {
			result.setLength(current.getDistance());
			while(current != null) {
				result.addPathElement(0, current.getNodeObject());
				current = current.getParent();
			}
		}
		return result;
	}
	
	/**
	 * A tuple containing a path of objects of type T and the length in actual distance between them
	 * 
	 * @author emiol791
	 *
	 * @param <T>
	 * 		This should be the same class that is used on the Searchable start and end nodes
	 */
	public static class PathLenTuple<T> {
		private List<T> path;
		private double length;
		
		/**
		 * Instantiate a new path & length tuple
		 */
		public PathLenTuple() {
			path = new ArrayList<T>();
		}
		
		/**
		 * Adds an element to the path. Length is not affected.
		 * @param element
		 */
		public void addPathElement(T element) {
			path.add(element);
		}
		
		/**
		 * Adds an element to the path at index atIndex. Length is not affected.
		 * @param element
		 */
		public void addPathElement(int atIndex, T element) {
			path.add(atIndex, element);
		}
		
		/**
		 * 
		 * @return
		 * 		The path contained in this tuple
		 */
		public List<T> getPath() {
			return path;
		}
		
		/**
		 * 
		 * @return
		 * 		The length of the path in this tuple.
		 */
		public double getLength() {
			return length;
		}
		
		/**
		 * Manually set the length of the path
		 * @param length
		 */
		public void setLength(double length) {
			this.length = length;
		}
	}
}
