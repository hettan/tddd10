package navigation_emil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

public class AStarAlgorithm {
	
	public static <T> List<T> PerformSearch(Searchable<T> start, Searchable<T> end) {
		List<T> result = new ArrayList<T>();
		
		//Same as goal?
		if(start.getUniqueId() == end.getUniqueId()) {
			result.add(end.getNodeObject());
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
			closed.add(current.getUniqueId());
			if(open.contains(current.getUniqueId())) continue;
			if(current != null && current.getUniqueId() == end.getUniqueId())
				break; // We found the end;
			
			for(T childBase : current.getChildren()) {
				child = current.createNode(childBase, current, end);
				if(child != null && !closed.contains(child.getUniqueId())) {
					frontier.add(child);
				}
			}
		}
		
		if(current != null && current.getUniqueId() == end.getUniqueId()) {
			while(current != null) {
				result.add(0, current.getNodeObject());
				current = current.getParent();
			}
		}
		
		return result;
	}
}
