package navigation_emil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

public class AStarAlgorithm {
	
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
	
	public static class PathLenTuple<T> {
		private List<T> path;
		private double length;
		public PathLenTuple() {
			path = new ArrayList<T>();
		}
		public void addPathElement(T element) {
			path.add(element);
		}
		public void addPathElement(int atIndex, T element) {
			path.add(atIndex, element);
		}
		public List<T> getPath() {
			return path;
		}
		public double getLength() {
			return length;
		}
		public void setLength(double length) {
			this.length = length;
		}
	}
}
