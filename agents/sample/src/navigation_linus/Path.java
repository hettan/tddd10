package navigation_linus;

import java.util.ArrayList;
import rescuecore2.standard.entities.Road;

public class Path {

	public Road start;
	public Road dest;
	public int length;
	public int heuristic;
	public ArrayList<Road> path;
	
	Path(Road startIn, Road destIn) {
		length = 0;
		start = startIn;
		dest = destIn;
		path = new ArrayList<Road>();
		path.add(start);
		heuristic = 0;
	}
}
